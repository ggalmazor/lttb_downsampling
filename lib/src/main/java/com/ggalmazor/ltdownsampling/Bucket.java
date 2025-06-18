package com.ggalmazor.ltdownsampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.ggalmazor.ltdownsampling.Point.centerBetween;

/**
 * This class represents a bucket of {@link Point} points being downsampled to a single point.
 *
 * @param <T> the type of the {@link Point} points in this bucket
 */
class Bucket<T extends Point> {
  private final List<T> data;
  private final T first;
  private final T last;
  private final Point center;
  private final T result;

  private Bucket(List<T> data, T first, T last, Point center, T result) {
    this.data = data;
    this.first = first;
    this.last = last;
    this.center = center;
    this.result = result;
  }

  /**
   * Utility factory that takes a list of {@link Point} points and returns a {@link Bucket}
   *
   * @param points the input list of points in the bucket being built
   * @param <U>    the type of the {@link Point} points in the buket being built
   * @return the bucket
   */
  static <U extends Point> Bucket<U> of(List<U> points) {
    U first = points.get(0);
    U last = points.get(points.size() - 1);
    DoublePoint center = centerBetween(first, last);
    return new Bucket<>(points, first, last, center, first);
  }

  /**
   * Utility factory that returns a {@link Bucket} bucket with a single {@link Point} point in it
   *
   * @param point the input point in the bucket being built
   * @param <U>   the type of the {@link Point} point in the buket being built
   * @return the bucket
   */
  static <U extends Point> Bucket<U> of(U point) {
    return new Bucket<>(Collections.singletonList(point), point, point, point, point);
  }

  /**
   * @return the resulting downsampled {@link Point} point for this bucket
   */
  T getResult() {
    return result;
  }

  /**
   * @return the first {@link Point} point in this bucket
   */
  T getFirst() {
    return first;
  }

  /**
   * @return the last {@link Point} point in this bucket
   */
  T getLast() {
    return last;
  }

  /**
   * @return the {@link Point} at the center of this bucket
   */
  Point getCenter() {
    return center;
  }

  /**
   * Utility function to {@link java.util.stream.Stream#map(Function)} over the points in this bucket
   *
   * @return the list of mapped points
   */
  <U> List<U> map(Function<T, U> mapper) {
    List<U> result = new ArrayList<>(data.size());
    for (T item : data) {
      result.add(mapper.apply(item));
    }
    return result;
  }
}
