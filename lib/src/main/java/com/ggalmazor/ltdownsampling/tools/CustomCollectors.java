package com.ggalmazor.ltdownsampling.tools;

import java.util.List;
import java.util.stream.Collector;

/**
 * Utility class to wire in the sliding window collector defined by {@link SlidingCollector}
 */
public class CustomCollectors {

  /**
   * Collects a {@link java.util.stream.Stream} into lists of consecutive elements in groups of the provided <code>size</code> without overlaps or gaps
   *
   * @param size the size of the output lists
   * @param <T>  the type of elements in the {@link java.util.stream.Stream} being collected
   * @return a list of lists
   */
  public static <T> Collector<T, ?, List<List<T>>> sliding(int size) {
    return new SlidingCollector<>(size, 1);
  }

  /**
   * Collects a {@link java.util.stream.Stream} into lists of consecutive elements in groups of the provided <code>size</code>, advancing the provided <code>step</code> elements between each group.
   * <p>
   * Depending on the value of <code>step</code>, the resulting lists can have overlaps, or gaps:
   * <ul>
   * <li><code>sliding(2,1)</code> will produce overlapping lists (every input element is present in two output elements except for the first and the last input elements)</li>
   * <li><code>sliding(2,3)</code> will produce a list with gaps (every third input element is skipped)</li>
   * </ul>
   *
   * @param size the number of elements on each list element in the output list
   * @param step the number of input elements to skip after each list element in the output list
   * @param <T>  the type of elements in the {@link java.util.stream.Stream} being collected
   * @return a list of lists
   */
  public static <T> Collector<T, ?, List<List<T>>> sliding(int size, int step) {
    return new SlidingCollector<>(size, step);
  }

}
