package com.ggalmazor.ltdownsampling;

/**
 * This class provides a basic {@link Point} implementation on {@link Double} values to be used with {@link LTThreeBuckets#sorted}.
 */
public class DoublePoint implements Point {
  private final double x;
  private final double y;

  /**
   * Returns a new {@link DoublePoint} with the provided <code>x</code>, and <code>y</code> Double values
   *
   * @param x the double value of the point in the horizontal dimension (time)
   * @param y the double value of the point in the vertical dimension
   */
  public DoublePoint(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Utility factory for {@link DoublePoint} that takes abstract {@link Number} x and y values
   *
   * @param x the value of the point in the horizontal dimension (time)
   * @param y the value of the point in the vertical dimension
   * @return the new {@link DoublePoint} instance
   */
  public static DoublePoint of(Number x, Number y) {
    return new DoublePoint(x.doubleValue(), y.doubleValue());
  }

  /**
   * @return the x horizontal (time) value of this point
   */
  @Override
  public double getX() {
    return x;
  }

  /**
   * @return the y vertical value of this point
   */
  @Override
  public double getY() {
    return y;
  }

  /**
   * @return the text representation of this point
   */
  @Override
  public String toString() {
    return "DoublePoint{" +
      "x=" + x +
      ", y=" + y +
      '}';
  }
}
