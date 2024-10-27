package com.ggalmazor.ltdownsampling.tools;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.lang.Integer.max;
import static java.util.stream.Collectors.toList;

/**
 * This class implements a sliding window collector for {@link java.util.stream.Stream} streams
 * <p>
 * Copied from <a href="http://www.nurkiewicz.com/2014/07/grouping-sampling-and-batching-custom.html">...</a>
 */
public class SlidingCollector<T> implements Collector<T, List<List<T>>, List<List<T>>> {

  private final int size;
  private final int step;
  private final int window;
  private final Queue<T> buffer = new ArrayDeque<>();
  private int totalIn = 0;

  /**
   * Creates a new {@link SlidingCollector} instance with the provided configuration
   *
   * @param size the number of elements on each list element in the output list
   * @param step the number of input elements to skip after each list element in the output list
   */
  public SlidingCollector(int size, int step) {
    this.size = size;
    this.step = step;
    this.window = max(size, step);
  }

  /**
   * A function that creates and returns a new mutable result container.
   *
   * @return a function which returns a new, mutable result container
   */
  @Override
  public Supplier<List<List<T>>> supplier() {
    return ArrayList::new;
  }

  /**
   * A function that folds a value into a mutable result container.
   *
   * @return a function which folds a value into a mutable result container
   */
  @Override
  public BiConsumer<List<List<T>>, T> accumulator() {
    return (lists, t) -> {
      buffer.offer(t);
      ++totalIn;
      if (buffer.size() == window) {
        dumpCurrent(lists);
        shiftBy(step);
      }
    };
  }

  /**
   * Perform the final transformation from the intermediate accumulation type
   * {@code A} to the final result type {@code R}.
   *
   * <p>If the characteristic {@code IDENTITY_FINISH} is
   * set, this function may be presumed to be an identity transform with an
   * unchecked cast from {@code A} to {@code R}.
   *
   * @return a function which transforms the intermediate result to the final
   * result
   */
  @Override
  public Function<List<List<T>>, List<List<T>>> finisher() {
    return lists -> {
      if (!buffer.isEmpty()) {
        final int totalOut = estimateTotalOut();
        if (totalOut > lists.size()) {
          dumpCurrent(lists);
        }
      }
      return lists;
    };
  }

  private int estimateTotalOut() {
    return max(0, (totalIn + step - size - 1) / step) + 1;
  }

  private void dumpCurrent(List<List<T>> lists) {
    final List<T> batch = buffer.stream().limit(size).collect(toList());
    lists.add(batch);
  }

  private void shiftBy(int by) {
    for (int i = 0; i < by; i++) {
      buffer.remove();
    }
  }

  /**
   * A function that accepts two partial results and merges them.  The
   * combiner function may fold state from one argument into the other and
   * return that, or may return a new result container.
   *
   * @return a function which combines two partial results into a combined
   * result
   */
  @Override
  public BinaryOperator<List<List<T>>> combiner() {
    return (l1, l2) -> {
      throw new UnsupportedOperationException("Combining not possible");
    };
  }

  /**
   * Returns a {@code Set} of {@code Collector.Characteristics} indicating
   * the characteristics of this Collector.  This set should be immutable.
   *
   * @return an immutable set of collector characteristics
   */
  @Override
  public Set<Characteristics> characteristics() {
    return EnumSet.noneOf(Characteristics.class);
  }

}
