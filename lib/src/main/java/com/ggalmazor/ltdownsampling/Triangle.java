package com.ggalmazor.ltdownsampling;

import java.util.List;

import static java.util.Comparator.comparing;

/**
 * This class represents the maximum area triangle defined by three consecutive {@link Bucket} buckets of points.
 * <p>
 * The maximum area triangle is defined by:
 * <ul>
 * <li>The result of the bucket at the left</li>
 * <li>The center point of the bucket at the right</li>
 * <li>The point of the bucket at the center that produces the largest area. This point is considered the result of the bucket</li>
 * </ul>
 *
 * @param <T> the type of the {@link Point} points in the buckets of this {@link Triangle}
 */
class Triangle<T extends Point> {
  private final Bucket<T> left, center, right;

  private Triangle(Bucket<T> left, Bucket<T> center, Bucket<T> right) {
    this.left = left;
    this.center = center;
    this.right = right;
  }

  /**
   * Factory to build an instance of {@link Triangle} from a list of buckets. This factory only considers the first three buckets in the list.
   *
   * @param buckets the input list of buckets
   * @return the {@link Triangle} instance formed by the first three buckets in the input list
   * @param <U> the type of {@link Point} in the input buckets
   */
  static <U extends Point> Triangle<U> of(List<Bucket<U>> buckets) {
    return new Triangle<>(
      buckets.get(0),
      buckets.get(1),
      buckets.get(2)
    );
  }

  /**
   * @return the first point of the set of three buckets composing this {@link Triangle}
   */
  T getFirst() {
    return left.getFirst();
  }

  /**
   * @return the last point of the set of three buckets composing this {@link Triangle}
   */
  T getLast() {
    return right.getLast();
  }

  /**
   * @return the point of the middle bucket of this {@link Triangle} that produces the triangle with largest area
   */
  T getResult() {
    return center.map(b -> Area.ofTriangle(left.getResult(), b, right.getCenter()))
      .stream()
      .max(comparing(Area::getValue))
      .orElseThrow(() -> new RuntimeException("Can't obtain max area triangle"))
      .getGenerator();
  }
}
