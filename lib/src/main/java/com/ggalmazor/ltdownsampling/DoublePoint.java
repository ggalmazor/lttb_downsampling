package com.ggalmazor.ltdownsampling;

/**
 * Basic {@link Point} implementation backed by {@code double} values, for use with
 * {@link LTThreeBuckets#sorted}.
 */
public record DoublePoint(double x, double y) implements Point {

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
}
