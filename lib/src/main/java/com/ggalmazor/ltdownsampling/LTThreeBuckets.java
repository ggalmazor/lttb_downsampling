package com.ggalmazor.ltdownsampling;

import java.util.ArrayList;
import java.util.List;

import static com.ggalmazor.ltdownsampling.tools.CustomCollectors.sliding;

/**
 * The LTThreeBuckets class is the main entry point to this library.
 * <p>
 * None of the methods in this class will mutate input lists or their elements.
 */
public final class LTThreeBuckets {

  /**
   * Returns a new {@link java.util.List} of {@link Point} with a downsampled version of the provided <code>input</code> list. The output list will have as many elements as the provided <code>desiredBuckets</code> param.
   * <p>
   * Notes:
   * <ul>
   * <li>The input list must be sorted</li>
   * <li>This method doesn't mutate the input list or any of its elements.</li>
   * </ul>
   *
   * @param input the input list of {@link Point} points to downsample
   * @param desiredBuckets the desired number of elements for the downsampled output list
   * @return the downsampled output list
   * @param <T> the type of the {@link Point} elements in the input list
   */
  public static <T extends Point> List<T> sorted(List<T> input, int desiredBuckets) {
    return sorted(input, input.size(), desiredBuckets);
  }

  /**
   * Returns a new {@link java.util.List} of {@link Point} with a downsampled version of the provided <code>input</code> list. The output list will have as many elements as the provided <code>desiredBuckets</code> param.
   * <p>
   * Notes:
   * <ul>
   * <li>The input list must be sorted</li>
   * <li>This method doesn't mutate the input list or any of its elements.</li>
   * </ul>
   *
   * @param input the input list of {@link Point} points to downsample
   * @param inputSize the size of the input list
   * @param desiredBuckets the desired number of elements for the downsampled output list
   * @return the downsampled output list
   * @param <T> the type of the {@link Point} elements in the input list
   */
  public static <T extends Point> List<T> sorted(List<T> input, int inputSize, int desiredBuckets) {
    List<T> results = new ArrayList<>();

    OnePassBucketizer.bucketize(input, inputSize, desiredBuckets)
      .stream()
      .collect(sliding(3, 1))
      .stream()
      .map(Triangle::of)
      .forEach(triangle -> {
        if (results.isEmpty())
          results.add(triangle.getFirst());

        results.add(triangle.getResult());

        if (results.size() == desiredBuckets + 1)
          results.add(triangle.getLast());
      });

    return results;
  }

}
