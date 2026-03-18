package com.ggalmazor.ltdownsampling;

/**
 * Selects the bucket size strategy used to divide the input series before the
 * Largest-Triangle Three-Buckets selection step.
 *
 * <p>These strategies correspond directly to the dynamic and fixed bucket sizes described in
 * the original LTTB paper: <em>Downsampling Time Series for Visual Representation</em>
 * by Sveinn Steinarsson (2013).
 *
 * <ul>
 * <li>Use {@link #DYNAMIC} (the default) when samples are evenly distributed across the
 * x-axis. Each bucket contains the same number of points, so bucket x-span varies with
 * data density.</li>
 * <li>Use {@link #FIXED} when samples are unevenly distributed, or when there are gaps in
 * the series. Each bucket covers the same x-span, so bucket point count varies with data
 * density. Empty buckets (x-intervals with no points) are silently skipped; the output may
 * therefore have fewer than {@code desiredBuckets + 2} points.</li>
 * </ul>
 */
public enum BucketizationStrategy {

  /**
   * Dynamic bucket size: each bucket contains an equal number of points.
   *
   * <p>This is the default strategy, corresponding to the dynamic bucket size variant in the
   * original LTTB paper. It works well when samples are evenly distributed across the x-axis,
   * where equal point counts correspond to equal x-spans.
   *
   * <p>Requires at least {@code desiredBuckets + 2} points in the input. Throws
   * {@link IllegalArgumentException} if the input is too small.
   */
  DYNAMIC,

  /**
   * Fixed bucket size: each bucket covers an equal x-axis span.
   *
   * <p>Corresponds to the fixed bucket size variant in the original LTTB paper. The total
   * x range {@code [x_first, x_last]} is divided into {@code desiredBuckets} equal-width
   * intervals. Each point is assigned to the interval that contains its {@link Point#x()}
   * value. This works correctly whether {@code x()} represents timestamps, plain numeric
   * indices, or any other monotonically increasing dimension.
   *
   * <p>X-intervals that contain no points are silently skipped. As a result, the output
   * may contain fewer than {@code desiredBuckets + 2} points when the input has gaps or
   * highly uneven density.
   *
   * <p>{@link Point#x()} must be monotonically non-decreasing across the input list.
   */
  FIXED
}
