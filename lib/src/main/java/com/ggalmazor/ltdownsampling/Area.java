package com.ggalmazor.ltdownsampling;

import static java.lang.Math.abs;

/**
 * Utility class for the triangle area computations.
 *
 * <p>It doubles as a DTO representing the area of the triangles involved in the algorithm.
 *
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
   * Returns an instance of {@link Area} defined by three {@link Point} points.
   *
   * @param a   first point of the triangle for the {@link Area} getting built
   * @param b   second point of the triangle for the {@link Area} getting built
   * @param c   third point of the triangle for the {@link Area} getting built
   * @param <U> type of the {@link Point} points defining the triangle
   * @return the {@link Area} instance
   */
  static <U extends Point> Area<U> ofTriangle(Point a, U b, Point c) {
    // area of a triangle = |[Ax(By - Cy) + Bx(Cy - Ay) + Cx(Ay - By)] / 2|
    double sum = a.getX() * (b.getY() - c.getY())
        + b.getX() * (c.getY() - a.getY())
        + c.getX() * (a.getY() - b.getY());
    double areaValue = abs(sum / 2);
    return new Area<>(b, areaValue);
  }

  /**
   * Returns the generator {@link Point} for this area.
   *
   * <p>In the LTTB algorithm, the generator is the point at the middle in the time dimension,
   * since it's the only point that belongs to the bucket being downsampled.
   *
   * @return the generator {@link Point} point
   */
  T getGenerator() {
    return generator;
  }

  /**
   * Returns the area value of this {@link Area} instance.
   *
   * @return the area of this {@link Area} instance
   */
  public double getValue() {
    return value;
  }

  /**
   * Computes the triangle area scalar for the three provided points without allocating an
   * {@link Area} record.
   *
   * <p>Used in the hot inner loop of {@link Triangle#getResult()} to avoid per-candidate
   * allocations.
   *
   * @param a first point of the triangle
   * @param b second (middle) point of the triangle
   * @param c third point of the triangle
   * @return the area of the triangle as a scalar double value
   */
  static double rawArea(Point a, Point b, Point c) {
    // area of a triangle = |[Ax(By - Cy) + Bx(Cy - Ay) + Cx(Ay - By)] / 2|
    double sum = a.getX() * (b.getY() - c.getY())
        + b.getX() * (c.getY() - a.getY())
        + c.getX() * (a.getY() - b.getY());
    return abs(sum / 2);
  }
}
