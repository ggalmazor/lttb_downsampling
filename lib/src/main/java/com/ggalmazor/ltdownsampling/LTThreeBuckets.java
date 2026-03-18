package com.ggalmazor.ltdownsampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The LTThreeBuckets class is the main entry point to this library.
 *
 * <p>None of the methods in this class will mutate input lists or their elements.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName") // LT is a domain abbreviation (Largest Triangle)
public final class LTThreeBuckets {

  /**
   * Minimum number of buckets to engage the parallel triangle-selection path.
   *
   * <p>Below this threshold, thread-management overhead exceeds the work saved.
   */
  static final int PARALLEL_THRESHOLD = 512;

  private LTThreeBuckets() {}

  /**
   * Returns a downsampled version of the provided {@code input} list.
   *
   * <p>Notes:
   * <ul>
   * <li>The input list must be sorted.</li>
   * <li>This method doesn't mutate the input list or any of its elements.</li>
   * </ul>
   *
   * <p>The output list will have {@code desiredBuckets + 2} elements: one per bucket plus the
   * first and last points of the original series.
   *
   * @param input          the input list of {@link Point} points to downsample
   * @param desiredBuckets the desired number of buckets for the downsampled output list
   * @param <T>            the type of the {@link Point} elements in the input list
   * @return the downsampled output list
   */
  public static <T extends Point> List<T> sorted(List<T> input, int desiredBuckets) {
    return sorted(input, input.size(), desiredBuckets);
  }

  /**
   * Returns a downsampled version of the provided {@code input} list.
   *
   * <p>Notes:
   * <ul>
   * <li>The input list must be sorted.</li>
   * <li>This method doesn't mutate the input list or any of its elements.</li>
   * </ul>
   *
   * <p>The output list will have {@code desiredBuckets + 2} elements: one per bucket plus the
   * first and last points of the original series.
   *
   * <p>When the input contains {@link DoublePoint} instances, an optimised struct-of-arrays
   * path is used: x and y coordinates are extracted into contiguous {@code double[]} arrays
   * before the selection loop, eliminating per-point pointer chasing in the hot path.
   *
   * <p>When {@code desiredBuckets} exceeds {@value #PARALLEL_THRESHOLD}, the triangle-selection
   * loop runs in parallel using the common {@link java.util.concurrent.ForkJoinPool}.
   *
   * @param input          the input list of {@link Point} points to downsample
   * @param inputSize      the size of the input list
   * @param desiredBuckets the desired number of buckets for the downsampled output list
   * @param <T>            the type of the {@link Point} elements in the input list
   * @return the downsampled output list
   */
  public static <T extends Point> List<T> sorted(List<T> input, int inputSize, int desiredBuckets) {
    List<Bucket<T>> buckets = OnePassBucketizer.bucketize(input, inputSize, desiredBuckets);

    // Check whether we can use the DoublePoint struct-of-arrays fast path
    if (!input.isEmpty() && input.get(0) instanceof DoublePoint) {
      @SuppressWarnings("unchecked")
      List<DoublePoint> dpInput = (List<DoublePoint>) input;
      @SuppressWarnings("unchecked")
      List<Bucket<DoublePoint>> dpBuckets = (List<Bucket<DoublePoint>>) (List<?>) buckets;
      @SuppressWarnings("unchecked")
      List<T> result = (List<T>) sortedDoublePoint(dpInput, dpBuckets, desiredBuckets);
      return result;
    }

    return sortedGeneric(buckets, desiredBuckets);
  }

  /**
   * Struct-of-arrays fast path for {@link DoublePoint} inputs.
   *
   * <p>Extracts all coordinates into contiguous {@code double[]} arrays once, then the
   * inner selection loop operates on primitive arrays with no pointer chasing.
   */
  private static List<DoublePoint> sortedDoublePoint(
      List<DoublePoint> input,
      List<Bucket<DoublePoint>> buckets,
      int desiredBuckets) {
    // Extract coordinates into contiguous primitive arrays
    int size = input.size();
    double[] xs = new double[size];
    double[] ys = new double[size];
    for (int i = 0; i < size; i++) {
      DoublePoint p = input.get(i);
      xs[i] = p.getX();
      ys[i] = p.getY();
    }

    @SuppressWarnings("unchecked")
    DoublePoint[] middleResults = new DoublePoint[desiredBuckets];

    IntStream stream = IntStream.range(0, desiredBuckets);
    if (desiredBuckets >= PARALLEL_THRESHOLD) {
      stream = stream.parallel();
    }

    stream.forEach(i -> middleResults[i] = selectBestDoublePoint(buckets, xs, ys, i));

    List<DoublePoint> results = new ArrayList<>(desiredBuckets + 2);
    results.add(buckets.get(0).getFirst());
    results.addAll(Arrays.asList(middleResults));
    results.add(buckets.get(buckets.size() - 1).getLast());
    return results;
  }

  /**
   * Selects the {@link DoublePoint} from the center bucket at window {@code offset} that forms
   * the triangle with the largest area, operating entirely on primitive {@code double[]} arrays.
   */
  private static DoublePoint selectBestDoublePoint(
      List<Bucket<DoublePoint>> buckets,
      double[] xs,
      double[] ys,
      int offset) {
    Bucket<DoublePoint> left = buckets.get(offset);
    Bucket<DoublePoint> center = buckets.get(offset + 1);
    Bucket<DoublePoint> right = buckets.get(offset + 2);

    double lx = left.getResult().getX();
    double ly = left.getResult().getY();
    double rx = right.getCenter().getX();
    double ry = right.getCenter().getY();

    DoublePoint bestPoint = null;
    double bestArea = -1.0;

    for (DoublePoint candidate : center.points()) {
      double cx = candidate.getX();
      double cy = candidate.getY();
      // area of a triangle = |[Ax(By - Cy) + Bx(Cy - Ay) + Cx(Ay - By)] / 2|
      double area = Math.abs(lx * (cy - ry) + cx * (ry - ly) + rx * (ly - cy)) / 2.0;
      if (area > bestArea) {
        bestArea = area;
        bestPoint = candidate;
      }
    }

    return bestPoint;
  }

  /**
   * Generic path for arbitrary {@link Point} implementations.
   *
   * <p>Uses the index-based {@link Triangle#of(List, int)} factory to avoid allocating a
   * {@code subList} view on each iteration, and runs in parallel above
   * {@value #PARALLEL_THRESHOLD} buckets.
   */
  @SuppressWarnings("unchecked")
  private static <T extends Point> List<T> sortedGeneric(
      List<Bucket<T>> buckets,
      int desiredBuckets) {
    T[] middleResults = (T[]) new Point[desiredBuckets];

    IntStream stream = IntStream.range(0, desiredBuckets);
    if (desiredBuckets >= PARALLEL_THRESHOLD) {
      stream = stream.parallel();
    }

    stream.forEach(i -> middleResults[i] = Triangle.of(buckets, i).getResult());

    List<T> results = new ArrayList<>(desiredBuckets + 2);
    results.add(buckets.get(0).getFirst());
    results.addAll(Arrays.asList(middleResults));
    results.add(buckets.get(buckets.size() - 1).getLast());
    return results;
  }
}
