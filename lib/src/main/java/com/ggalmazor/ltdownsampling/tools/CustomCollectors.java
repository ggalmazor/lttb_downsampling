package com.ggalmazor.ltdownsampling.tools;

import java.util.List;
import java.util.stream.Collector;

/**
 * Utility class to wire in the sliding window collector defined by {@link SlidingCollector}.
 */
public class CustomCollectors {

  /**
   * Collects a {@link java.util.stream.Stream} into lists of consecutive elements in groups of
   * the provided {@code size} without overlaps or gaps.
   *
   * @param size the size of the output lists
   * @param <T>  the type of elements in the {@link java.util.stream.Stream} being collected
   * @return a list of lists
   */
  public static <T> Collector<T, ?, List<List<T>>> sliding(int size) {
    return new SlidingCollector<>(size, 1);
  }

  /**
   * Collects a {@link java.util.stream.Stream} into lists of consecutive elements in groups of
   * the provided {@code size}, advancing {@code step} elements between each group.
   *
   * <p>Depending on the value of {@code step}, the resulting lists can have overlaps or gaps:
   * <ul>
   * <li>{@code sliding(2,1)} produces overlapping lists (every input element is present in two
   * output elements except for the first and last).</li>
   * <li>{@code sliding(2,3)} produces a list with gaps (every third input element is skipped).
   * </li>
   * </ul>
   *
   * @param size the number of elements in each list element of the output list
   * @param step the number of input elements to advance after each output list element
   * @param <T>  the type of elements in the {@link java.util.stream.Stream} being collected
   * @return a list of lists
   */
  public static <T> Collector<T, ?, List<List<T>>> sliding(int size, int step) {
    return new SlidingCollector<>(size, step);
  }

}
