package com.ggalmazor.ltdownsampling;

/**
 * Basic {@link Point} implementation backed by {@code double} values, for use with
 * {@link LTThreeBuckets#sorted}.
 */
public class DoublePoint implements Point {
  private final double x;
  private final double y;

  /**
   * Creates a new {@link DoublePoint} with the provided {@code x} and {@code y} values.
   *
   * @param x the double value of the point in the horizontal dimension (time)
   * @param y the double value of the point in the vertical dimension
   */
  public DoublePoint(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Utility factory for {@link DoublePoint} that accepts abstract {@link Number} values.
   *
   * @param x the value of the point in the horizontal dimension (time)
   * @param y the value of the point in the vertical dimension
   * @return the new {@link DoublePoint} instance
   */
  public static DoublePoint of(Number x, Number y) {
    return new DoublePoint(x.doubleValue(), y.doubleValue());
  }

  /**
   * Returns the x horizontal (time) value of this point.
   *
   * @return the x horizontal (time) value of this point
   */
  @Override
  public double getX() {
    return x;
  }

  /**
   * Returns the y vertical value of this point.
   *
   * @return the y vertical value of this point
   */
  @Override
  public double getY() {
    return y;
  }

  /**
   * Returns the text representation of this point.
   *
   * @return the text representation of this point
   */
  @Override
  public String toString() {
    return "DoublePoint{"
        + "x=" + x
        + ", y=" + y
        + '}';
  }
}
