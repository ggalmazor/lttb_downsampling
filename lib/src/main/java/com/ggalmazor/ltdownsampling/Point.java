package com.ggalmazor.ltdownsampling;

/**
 * Defines the properties of points that can be processed by {@link LTThreeBuckets}.
 *
 * <p>Users of this library must use {@link DoublePoint} or implement their own subtypes of
 * this interface.
 */
public interface Point {
  /**
   * Computes the geometric center point of the segment linking the provided {@code a} and
   * {@code b} points.
   *
   * @param a the first point of the segment
   * @param b the second point of the segment
   * @return a {@link DoublePoint} in the geometric center between the two provided points
   */
  static DoublePoint centerBetween(Point a, Point b) {
    return new DoublePoint((a.x() + b.x()) / 2.0, (a.y() + b.y()) / 2.0);
  }

  /**
   * Returns the x (horizontal / time) value of this point.
   *
   * <p><strong>Contract:</strong> {@code x()} must be monotonically non-decreasing across a
   * sorted input list — i.e. for any two consecutive points {@code a} and {@code b} in the list
   * passed to {@link LTThreeBuckets#sorted}, {@code a.x() <= b.x()} must hold. The algorithm
   * does not verify this; violating it produces undefined output.
   *
   * <p>This contract is also required for fixed-size bucketization
   * ({@link BucketizationStrategy#FIXED}), where {@code x()} is used to compute equal-width
   * x-span intervals. Non-monotonic or unordered {@code x()} values will produce incorrect
   * bucket assignments.
   *
   * @return the x (horizontal / time) value of this point
   */
  double x();

  /**
   * Returns the y (vertical / value) value of this point.
   *
   * @return the y (vertical / value) value of this point
   */
  double y();
}
