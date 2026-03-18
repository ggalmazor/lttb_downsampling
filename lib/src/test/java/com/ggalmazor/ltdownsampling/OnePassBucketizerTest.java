package com.ggalmazor.ltdownsampling;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OnePassBucketizerTest {

  // ---- DYNAMIC (count-based) ----

  @Test
  public void dynamic_divides_evenly() {
    // 8 middle points / 4 buckets = 2 points per bucket
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1), DoublePoint.of(2, 2),
      DoublePoint.of(3, 3), DoublePoint.of(4, 4),
      DoublePoint.of(5, 5), DoublePoint.of(6, 6),
      DoublePoint.of(7, 7), DoublePoint.of(8, 8),
      DoublePoint.of(9, 0)
    );

    List<Bucket<DoublePoint>> buckets =
        OnePassBucketizer.bucketize(input, input.size(), 4, BucketizationStrategy.DYNAMIC);

    assertThat(buckets, hasSize(6)); // 4 middle + first + last
    assertThat(buckets.get(1).points(), hasSize(2));
    assertThat(buckets.get(2).points(), hasSize(2));
    assertThat(buckets.get(3).points(), hasSize(2));
    assertThat(buckets.get(4).points(), hasSize(2));
  }

  @Test
  public void dynamic_distributes_remainder_across_first_buckets() {
    // 7 middle points / 3 buckets: first bucket gets 3 (7/3=2 remainder 1), rest get 2
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1), DoublePoint.of(2, 2), DoublePoint.of(3, 3),
      DoublePoint.of(4, 4), DoublePoint.of(5, 5),
      DoublePoint.of(6, 6), DoublePoint.of(7, 7),
      DoublePoint.of(8, 0)
    );

    List<Bucket<DoublePoint>> buckets =
        OnePassBucketizer.bucketize(input, input.size(), 3, BucketizationStrategy.DYNAMIC);

    assertThat(buckets, hasSize(5));
    assertThat(buckets.get(1).points(), hasSize(3));
    assertThat(buckets.get(2).points(), hasSize(2));
    assertThat(buckets.get(3).points(), hasSize(2));
  }

  @Test
  public void dynamic_throws_when_too_few_points() {
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(2, 0)
    );
    // 1 middle point, 2 buckets requested — bucket size would be 0
    assertThrows(IllegalArgumentException.class, () ->
        OnePassBucketizer.bucketize(input, input.size(), 2, BucketizationStrategy.DYNAMIC)
    );
  }

  // ---- FIXED (span-based) ----

  @Test
  public void fixed_assigns_points_to_correct_span_windows() {
    // x range [0, 10], 4 buckets → windows [0,2.5), [2.5,5), [5,7.5), [7.5,10]
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),  // window 0
      DoublePoint.of(2, 2),  // window 0
      DoublePoint.of(3, 3),  // window 1
      DoublePoint.of(4, 4),  // window 1
      DoublePoint.of(6, 6),  // window 2
      DoublePoint.of(8, 8),  // window 3
      DoublePoint.of(9, 9),  // window 3
      DoublePoint.of(10, 0)
    );

    List<Bucket<DoublePoint>> buckets =
        OnePassBucketizer.bucketize(input, input.size(), 4, BucketizationStrategy.FIXED);

    assertThat(buckets, hasSize(6)); // 4 non-empty windows + first + last
    assertThat(buckets.get(1).points(), hasSize(2)); // x=1, x=2
    assertThat(buckets.get(2).points(), hasSize(2)); // x=3, x=4
    assertThat(buckets.get(3).points(), hasSize(1)); // x=6
    assertThat(buckets.get(4).points(), hasSize(2)); // x=8, x=9
  }

  @Test
  public void fixed_skips_empty_windows() {
    // x range [0, 10], 4 buckets → windows [0,2.5), [2.5,5), [5,7.5), [7.5,10]
    // Window 2 ([5,7.5)) is empty — contains no points
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),  // window 0
      DoublePoint.of(3, 3),  // window 1
      DoublePoint.of(8, 8),  // window 3 (window 2 is empty)
      DoublePoint.of(10, 0)
    );

    List<Bucket<DoublePoint>> buckets =
        OnePassBucketizer.bucketize(input, input.size(), 4, BucketizationStrategy.FIXED);

    // 3 non-empty windows + first + last = 5, not desiredBuckets+2=6
    assertThat(buckets, hasSize(5));
  }

  @Test
  public void fixed_output_size_is_desiredBuckets_plus_2_minus_empty_windows() {
    // Explicitly verify: output size = desiredBuckets + 2 - emptyWindows
    // x range [0, 12], 6 buckets → windows of width 2: [0,2), [2,4), [4,6), [6,8), [8,10), [10,12]
    // Windows 2 ([4,6)) and 4 ([8,10)) are empty — 2 empty windows
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),   // window 0
      DoublePoint.of(3, 3),   // window 1
      // window 2 empty
      DoublePoint.of(7, 7),   // window 3
      // window 4 empty
      DoublePoint.of(11, 11), // window 5
      DoublePoint.of(12, 0)
    );

    List<Bucket<DoublePoint>> buckets =
        OnePassBucketizer.bucketize(input, input.size(), 6, BucketizationStrategy.FIXED);

    int desiredBuckets = 6;
    int emptyWindows = 2;
    // desiredBuckets + 2 - emptyWindows = 6 + 2 - 2 = 6
    assertThat(buckets, hasSize(desiredBuckets + 2 - emptyWindows));
  }

  @Test
  public void fixed_all_empty_windows_produces_only_first_and_last() {
    // Only first and last points — no middle points — all windows are empty
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(10, 0)
    );

    List<Bucket<DoublePoint>> buckets =
        OnePassBucketizer.bucketize(input, input.size(), 4, BucketizationStrategy.FIXED);

    // No middle points → all 4 windows are empty → only first + last buckets remain
    assertThat(buckets, hasSize(2));
  }

  @Test
  public void fixed_clamps_boundary_point_to_last_bucket() {
    // A point at x=10 in range [0,10] / 2 buckets: (10-0)/5 = index 2, which is out of bounds.
    // The clamp ensures it lands in bucket index 1 (the last valid window).
    // x=5 (the midpoint) computes index (5-0)/5 = 1, also landing in the second window.
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(3, 3),  // window 0: (3-0)/5 = 0
      DoublePoint.of(7, 7),  // window 1: (7-0)/5 = 1
      DoublePoint.of(10, 0)
    );

    List<Bucket<DoublePoint>> buckets =
        OnePassBucketizer.bucketize(input, input.size(), 2, BucketizationStrategy.FIXED);

    assertThat(buckets, hasSize(4)); // 2 non-empty windows + first + last
    assertThat(buckets.get(1).points().get(0).x(), equalTo(3.0)); // window 0
    assertThat(buckets.get(2).points().get(0).x(), equalTo(7.0)); // window 1
  }

  @Test
  public void fixed_throws_when_x_values_are_equal() {
    List<DoublePoint> input = asList(
      DoublePoint.of(1, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(1, 2)
    );
    assertThrows(IllegalArgumentException.class, () ->
        OnePassBucketizer.bucketize(input, input.size(), 2, BucketizationStrategy.FIXED)
    );
  }

  @Test
  public void fixed_throws_when_fewer_than_2_points() {
    List<DoublePoint> input = asList(DoublePoint.of(0, 0));
    assertThrows(IllegalArgumentException.class, () ->
        OnePassBucketizer.bucketize(input, input.size(), 1, BucketizationStrategy.FIXED)
    );
  }
}
