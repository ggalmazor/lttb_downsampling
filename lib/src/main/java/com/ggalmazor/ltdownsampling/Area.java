package com.ggalmazor.ltdownsampling;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Utility class for the triangle area computations. It also doubles as a DTO representing the area of the triangles involved in the algorithm
 * @param <T> type of the {@link Point} points defining instances of this class
 */
class Area<T extends Point> {
  private final T generator;
  private final double value;

  private Area(T generator, double value) {
    this.generator = generator;
    this.value = value;
  }

  /**
   * Returns an instance of {@link Area} defined by three {@link Point} points
   *
   * @param a first point of the triangle for the {@link Area} getting built
   * @param b second point of the triangle for the {@link Area} getting built
   * @param c third point of the triangle for the {@link Area} getting built
   * @return the {@link Area} instance
   * @param <U> type of the {@link Point} points defining the triangle
   */
  static <U extends Point> Area<U> ofTriangle(Point a, U b, Point c) {
    // area of a triangle = |[Ax(By - Cy) + Bx(Cy - Ay) + Cx(Ay - By)] / 2|
    double sum = a.getX() * (b.getY() - c.getY()) +
                 b.getX() * (c.getY() - a.getY()) +
                 c.getX() * (a.getY() - b.getY());
    double value = abs(sum / 2);
    return new Area<>(b, value);
  }

  /**
   * In the LTTB algorithm, the generator {@link Point} is the one at the middle in the time dimension, since it's the only point that belongs to the bucket being downsampled.
   *
   * @return the generator {@link Point} point
   */
  T getGenerator() {
    return generator;
  }

  /**
   * @return the area of this {@link Area} instance
   */
  public double getValue() {
    return value;
  }
}
