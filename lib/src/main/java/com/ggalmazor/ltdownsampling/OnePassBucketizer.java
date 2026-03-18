package com.ggalmazor.ltdownsampling;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that divides the input list of {@link Point} points into {@link Bucket} buckets
 * as per the LTTB algorithm.
 *
 * <p>In LTTB, the first and last buckets always include a single point corresponding to the
 * first and last points of the input series.
 *
 * <p>Two strategies are supported; see {@link BucketizationStrategy}.
 */
class OnePassBucketizer {

  /**
   * Returns the list of {@link Bucket} buckets from the {@code input} list of points using the
   * count-based strategy.
   *
   * @param input          the input list of points
   * @param inputSize      the size of the input list of points
   * @param desiredBuckets the desired bucket count
   * @param <T>            the type of the {@link Point} points in the input list
   * @return the list of buckets
   */
  static <T extends Point> List<Bucket<T>> bucketize(List<T> input, int inputSize, int desiredBuckets) {
    return bucketize(input, inputSize, desiredBuckets, BucketizationStrategy.DYNAMIC);
  }

  /**
   * Returns the list of {@link Bucket} buckets from the {@code input} list of points using the
   * specified {@link BucketizationStrategy}.
   *
   * @param input          the input list of points
   * @param inputSize      the size of the input list of points
   * @param desiredBuckets the desired bucket count
   * @param strategy       the bucketization strategy to use
   * @param <T>            the type of the {@link Point} points in the input list
   * @return the list of buckets
   */
  static <T extends Point> List<Bucket<T>> bucketize(
      List<T> input, int inputSize, int desiredBuckets, BucketizationStrategy strategy) {
    return switch (strategy) {
      case DYNAMIC -> bucketizeByCount(input, inputSize, desiredBuckets);
      case FIXED -> bucketizeByFixedSpan(input, desiredBuckets);
    };
  }

  /**
   * Divides the input into buckets of equal point count.
   *
   * <p>Bucket boundaries are computed as {@code floor(middleSize / desiredBuckets)}, with
   * the remainder distributed one extra point at a time across the first buckets.
   * Middle buckets are represented as {@code subList} views — no element copying occurs.
   */
  private static <T extends Point> List<Bucket<T>> bucketizeByCount(
      List<T> input, int inputSize, int desiredBuckets) {
    int middleSize = inputSize - 2;
    int bucketSize = middleSize / desiredBuckets;
    int remainingElements = middleSize % desiredBuckets;

    if (bucketSize == 0) {
      throw new IllegalArgumentException(
          "Can't produce " + desiredBuckets + " buckets from an input series of "
              + (middleSize + 2) + " elements");
    }

    List<Bucket<T>> buckets = new ArrayList<>(desiredBuckets + 2);

    // First point in its own bucket
    buckets.add(Bucket.of(input.get(0)));

    // Middle buckets as subList views — O(1) per bucket, no element copying
    int currentIndex = 1;
    for (int bucketIndex = 0; bucketIndex < desiredBuckets; bucketIndex++) {
      int currentBucketSize = bucketIndex < remainingElements ? bucketSize + 1 : bucketSize;
      int end = currentIndex + currentBucketSize;
      buckets.add(Bucket.of(input.subList(currentIndex, end)));
      currentIndex = end;
    }

    // Last point in its own bucket
    buckets.add(Bucket.of(input.get(input.size() - 1)));

    return buckets;
  }

  /**
   * Divides the input into buckets of equal x-span.
   *
   * <p>The total x range {@code [x_first, x_last]} is split into {@code desiredBuckets}
   * equal-width intervals. Each point (excluding the first and last) is assigned to the
   * interval that contains its {@link Point#x()} value. Empty intervals are silently skipped,
   * so the returned list may contain fewer than {@code desiredBuckets + 2} buckets.
   *
   * <p>Requires that {@code Point#x()} is monotonically non-decreasing.
   */
  private static <T extends Point> List<Bucket<T>> bucketizeByFixedSpan(
      List<T> input, int desiredBuckets) {
    if (input.size() < 2) {
      throw new IllegalArgumentException(
          "Fixed-span bucketization requires at least 2 points");
    }

    double x0 = input.get(0).getX();
    double x1 = input.get(input.size() - 1).getX();
    double bucketWidth = (x1 - x0) / desiredBuckets;

    if (bucketWidth == 0) {
      throw new IllegalArgumentException(
          "Fixed-span bucketization requires points with distinct x() values");
    }

    // Group middle points (excluding first and last) into x-span windows
    List<List<T>> windows = new ArrayList<>(desiredBuckets);
    for (int i = 0; i < desiredBuckets; i++) {
      windows.add(new ArrayList<>());
    }

    int lastBucketIndex = desiredBuckets - 1;
    for (int i = 1; i < input.size() - 1; i++) {
      T point = input.get(i);
      int bucketIndex = (int) ((point.getX() - x0) / bucketWidth);
      // Clamp to last bucket to handle floating-point edge cases at x1
      windows.get(Math.min(bucketIndex, lastBucketIndex)).add(point);
    }

    // Build bucket list, skipping empty windows
    List<Bucket<T>> buckets = new ArrayList<>(desiredBuckets + 2);
    buckets.add(Bucket.of(input.get(0)));

    for (List<T> window : windows) {
      if (!window.isEmpty()) {
        buckets.add(Bucket.of(window));
      }
    }

    buckets.add(Bucket.of(input.get(input.size() - 1)));

    return buckets;
  }
}
