package com.ggalmazor.ltdownsampling;

import java.util.ArrayList;
import java.util.List;

/**
 * The LTThreeBuckets class is the main entry point to this library.
 *
 * <p>None of the methods in this class will mutate input lists or their elements.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName") // LT is a domain abbreviation (Largest Triangle)
public final class LTThreeBuckets {

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
   * @param input          the input list of {@link Point} points to downsample
   * @param inputSize      the size of the input list
   * @param desiredBuckets the desired number of buckets for the downsampled output list
   * @param <T>            the type of the {@link Point} elements in the input list
   * @return the downsampled output list
   */
  public static <T extends Point> List<T> sorted(List<T> input, int inputSize, int desiredBuckets) {
    List<T> results = new ArrayList<>(desiredBuckets);
    List<Bucket<T>> buckets = OnePassBucketizer.bucketize(input, inputSize, desiredBuckets);

    for (int i = 0; i <= buckets.size() - 3; i++) {
      Triangle<T> triangle = Triangle.of(buckets.subList(i, i + 3));

      if (results.isEmpty()) {
        results.add(triangle.getFirst());
      }

      results.add(triangle.getResult());

      if (results.size() == desiredBuckets + 1) {
        results.add(triangle.getLast());
        break;
      }
    }

    return results;
  }

}
