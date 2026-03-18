package com.ggalmazor.ltdownsampling;

import static java.lang.Math.abs;

/**
 * Represents the area of a triangle in the LTTB algorithm, paired with the {@link Point} that
 * generated it.
 *
 * <p>The generator is the point at the middle bucket — the candidate point being evaluated.
 *
 * @param generator the {@link Point} that produced this area
 * @param value     the triangle area value
 * @param <T>       type of the generator {@link Point}
 */
record Area<T extends Point>(T generator, double value) {

  /**
   * Returns an instance of {@link Area} defined by three {@link Point} points.
   *
   * @param a   first point of the triangle
   * @param b   second (middle) point of the triangle — becomes the generator
   * @param c   third point of the triangle
   * @param <U> type of the generator {@link Point}
   * @return the {@link Area} instance
   */
  static <U extends Point> Area<U> ofTriangle(Point a, U b, Point c) {
    // area of a triangle = |[Ax(By - Cy) + Bx(Cy - Ay) + Cx(Ay - By)] / 2|
    double sum = a.x() * (b.y() - c.y())
        + b.x() * (c.y() - a.y())
        + c.x() * (a.y() - b.y());
    return new Area<>(b, abs(sum / 2));
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
    double sum = a.x() * (b.y() - c.y())
        + b.x() * (c.y() - a.y())
        + c.x() * (a.y() - b.y());
    return abs(sum / 2);
  }
}
