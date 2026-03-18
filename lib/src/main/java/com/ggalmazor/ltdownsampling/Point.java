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
    DoublePoint vector = new DoublePoint(b.x() - a.x(), b.y() - a.y());
    DoublePoint halfVector = new DoublePoint(vector.x() / 2, vector.y() / 2);
    return new DoublePoint(a.x() + halfVector.x(), a.y() + halfVector.y());
  }

  /**
   * Returns the x horizontal (time) value of this point.
   *
   * @return the x horizontal (time) value of this point
   */
  double x();

  /**
   * Returns the y vertical value of this point.
   *
   * @return the y vertical value of this point
   */
  double y();
}
