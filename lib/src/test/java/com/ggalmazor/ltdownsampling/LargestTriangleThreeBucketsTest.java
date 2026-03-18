package com.ggalmazor.ltdownsampling;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static com.ggalmazor.ltdownsampling.PointMatcher.pointAt;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LargestTriangleThreeBucketsTest {
  @Test
  public void one_bucket_with_one_point_produces_that_point() {
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(2, 0)
    );

    List<DoublePoint> output = LTThreeBuckets.sorted(input, input.size(), 1);

    assertThat(output, contains(
      pointAt(0, 0),
      pointAt(1, 1),
      pointAt(2, 0)
    ));
  }

  @Test
  public void one_bucket_with_two_points_with_same_area_produces_the_first_point() {
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(2, -1),
      DoublePoint.of(3, 0)
    );

    List<DoublePoint> output = LTThreeBuckets.sorted(input, input.size(), 1);

    assertThat(output, contains(
      pointAt(0, 0),
      pointAt(1, 1),
      pointAt(3, 0)
    ));
  }

  @Test
  public void one_bucket_with_two_points_with_different_area_produces_the_point_that_generates_max_area() {
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(2, 2),
      DoublePoint.of(3, 0)
    );

    List<DoublePoint> actualOutput = LTThreeBuckets.sorted(input, input.size(), 1);

    assertThat(actualOutput, contains(
      pointAt(0, 0),
      pointAt(2, 2),
      pointAt(3, 0)
    ));
  }

  @Test
  public void two_buckets_with_one_point_each() {
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(2, 2),
      DoublePoint.of(3, 0)
    );

    List<DoublePoint> output = LTThreeBuckets.sorted(input, input.size(), 2);

    assertThat(output, equalTo(input));
  }

  @Test
  public void two_buckets_non_full_middle_buckets() {
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(2, 2),
      DoublePoint.of(3, 1),
      DoublePoint.of(4, 5)
    );

    List<DoublePoint> output = LTThreeBuckets.sorted(input, input.size(), 2);

    assertThat(output, contains(
      pointAt(0, 0),
      pointAt(2, 2),
      pointAt(3, 1),
      pointAt(4, 5)
    ));
  }

  @Test
  public void two_buckets_full_middle_buckets() {
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(2, 3),
      DoublePoint.of(3, 1),
      DoublePoint.of(4, 3),
      DoublePoint.of(5, 2),
      DoublePoint.of(6, 0)
    );

    List<DoublePoint> actualOutput = LTThreeBuckets.sorted(input, input.size(), 2);

    assertThat(actualOutput, contains(
      pointAt(0, 0),
      pointAt(2, 3),
      pointAt(4, 3),
      pointAt(6, 0)
    ));
  }

  @Test
  public void throws_when_more_buckets_than_posible() {
    assertThrows(IllegalArgumentException.class, () ->
      LTThreeBuckets.sorted(emptyList(), 3, 2)
    );
  }

  // ---- FIXED bucketization ----

  @Test
  public void fixed_evenly_spaced_matches_dynamic() {
    // When points are evenly spaced, FIXED and DYNAMIC should select the same points
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(2, 3),
      DoublePoint.of(3, 1),
      DoublePoint.of(4, 3),
      DoublePoint.of(5, 2),
      DoublePoint.of(6, 0)
    );

    List<DoublePoint> dynamic = LTThreeBuckets.sorted(input, input.size(), 2);
    List<DoublePoint> fixed = LTThreeBuckets.sorted(input, 2, BucketizationStrategy.FIXED);

    assertThat(fixed, equalTo(dynamic));
  }

  @Test
  public void fixed_skips_empty_buckets_in_gappy_series() {
    // Dense cluster 0..3, gap, single point at 10
    // With 4 buckets over [0, 10]: windows are [0,2.5), [2.5,5), [5,7.5), [7.5,10]
    // Window 1: x=1, x=2 — Window 2: x=3 — Window 3: empty — Window 4: x=9
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 5),
      DoublePoint.of(2, 3),
      DoublePoint.of(3, 4),
      DoublePoint.of(9, 1),
      DoublePoint.of(10, 0)
    );

    List<DoublePoint> output = LTThreeBuckets.sorted(input, 4, BucketizationStrategy.FIXED);

    // 3 non-empty middle buckets + first + last = 5 points (not 6)
    assertThat(output.size(), equalTo(5));
    assertThat(output.get(0), equalTo(DoublePoint.of(0, 0)));
    assertThat(output.get(output.size() - 1), equalTo(DoublePoint.of(10, 0)));
  }

  @Test
  public void fixed_dense_cluster_not_overrepresented() {
    // DYNAMIC assigns 1 bucket to the dense cluster and 1 to the sparse stretch.
    // FIXED assigns buckets proportionally to x-span, so the sparse stretch with a single
    // high-value point gets its own bucket and is preserved in the output.
    //
    // Points: dense at x=0..3 (low value ~1), then sparse at x=8,9 (high value ~10)
    // DYNAMIC with 2 buckets: bucket1=[x1,x2,x3], bucket2=[x8] — x8 may or may not win
    // FIXED with 2 buckets over [0,9]: window1=[0,4.5)=[x1,x2,x3], window2=[4.5,9]=[x8]
    List<DoublePoint> input = asList(
      DoublePoint.of(0, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(2, 1),
      DoublePoint.of(3, 1),
      DoublePoint.of(8, 10),
      DoublePoint.of(9, 0)
    );

    List<DoublePoint> output = LTThreeBuckets.sorted(input, 2, BucketizationStrategy.FIXED);

    // First and last always included; the high-value sparse point must be selected
    assertThat(output.get(0), equalTo(DoublePoint.of(0, 0)));
    assertThat(output.get(output.size() - 1), equalTo(DoublePoint.of(9, 0)));
    assertThat(output.stream().anyMatch(p -> p.x() == 8.0), equalTo(true));
  }

  @Test
  public void fixed_throws_when_all_x_values_are_equal() {
    List<DoublePoint> input = asList(
      DoublePoint.of(1, 0),
      DoublePoint.of(1, 1),
      DoublePoint.of(1, 2)
    );

    assertThrows(IllegalArgumentException.class, () ->
      LTThreeBuckets.sorted(input, 2, BucketizationStrategy.FIXED)
    );
  }

  @SuppressWarnings({"DataFlowIssue", "resource"})
  @Test
  public void complex_downsampling_scenario() throws URISyntaxException, IOException {
    URI uri = LTThreeBuckets.class.getResource("/daily-foreign-exchange-rates-31-.csv").toURI();
    List<DateSeriesPoint> series = Files.lines(Paths.get(uri))
      .map(line -> line.split(";"))
      .map(cols -> {
        LocalDate date = LocalDate.parse(cols[0]);
        double value = Double.parseDouble(cols[1]);
        return new DateSeriesPoint(date, value);
      })
      .sorted(comparing(Point::x))
      .collect(toList());

    List<DateSeriesPoint> output = LTThreeBuckets.sorted(series, 10);
    List<LocalDate> selectedDatesInOutput = output.stream().map(DateSeriesPoint::getDate).collect(toList());
    assertThat(selectedDatesInOutput, contains(
      LocalDate.of(1979, 12, 31),
      LocalDate.of(1981, 8, 10),
      LocalDate.of(1982, 11, 8),
      LocalDate.of(1985, 2, 25),
      LocalDate.of(1985, 9, 18),
      LocalDate.of(1987, 12, 31),
      LocalDate.of(1991, 2, 11),
      LocalDate.of(1992, 9, 2),
      LocalDate.of(1995, 3, 7),
      LocalDate.of(1995, 4, 19),
      LocalDate.of(1997, 8, 5),
      LocalDate.of(1998, 12, 31)
    ));
  }
}
