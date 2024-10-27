package com.ggalmazor.ltdownsampling;

/**
 * Defines the properties of points that can be processed by {@link LTThreeBuckets}. Users of this library must use {@link DoublePoint} or implement their own subtypes of this interface.
 */
public interface Point {
  /**
   * @return the x horizontal (time) value of this point
   */
  double getX();

  /**
   * @return the y vertical value of this point
   */
  double getY();

  /**
   * Utility method to compute the geometric center point of the segment linking the provided <code>a</code> and <code>b</code> points
   *
   * @param a the first point of the segment
   * @param b the second point of the segment
   * @return a {@link DoublePoint} in the geometric center between the two provided points
   */
  static DoublePoint centerBetween(Point a, Point b) {
    DoublePoint vector = new DoublePoint(b.getX() - a.getX(), b.getY() - a.getY());
    DoublePoint halfVector = new DoublePoint(((Point) vector).getX() / 2, ((Point) vector).getY() / 2);
    return new DoublePoint(a.getX() + halfVector.getX(), a.getY() + halfVector.getY());
  }
}
