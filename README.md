# Largest-Triangle time-series downsampling algorithm implementation for modern Java
[![Maven Central](https://img.shields.io/maven-central/v/com.ggalmazor/lttb_downsampling.svg)](https://central.sonatype.com/artifact/com.ggalmazor/lttb_downsampling)
[![CI](https://github.com/ggalmazor/lttb_downsampling/actions/workflows/ci.yml/badge.svg)](https://github.com/ggalmazor/lttb_downsampling/actions/workflows/ci.yml)

These implementations are based on the paper *"Downsampling Time Series for Visual Representation"* by Sveinn Steinarsson from the Faculty of Industrial Engineering, Mechanical Engineering and Computer Science University of Iceland (2013). You can read the paper [here](http://skemman.is/stream/get/1946/15343/37285/3/SS_MSthesis.pdf)

The goal of Largest-Triangle downsampling algorithms for data visualization is to reduce the number of points in a number series without losing important visual features of the resulting graph. However, it is essential to know these algorithms are not numerically correct.

See how this algorithm compares to other algorithms designed to keep local extrema in the input series at [ggalmazor.com/blog/evaluating_downsampling_algorithms.html](https://ggalmazor.com/blog/evaluating_downsampling_algorithms.html)

Javadoc at [ggalmazor.com/lttb_downsampling](https://ggalmazor.com/lttb_downsampling)

## Java version support

| Version | Java baseline | Branch | Status                  |
|---------|---------------|--------|-------------------------|
| 17.x.x  | Java 17       | `v17`  | Active (bug fixes only) |
| 21.x.x  | Java 21       | `v21`  | Active                  |
| 25.x.x  | Java 25       | `main` | Active (cutting edge)   |

The library version number reflects the minimum Java version required to use it.

Each branch builds on the previous one, introducing idioms and optimisations that became available
in that Java release:

**17.x — Java 17 baseline.** The foundation. Uses classic Java idioms: mutable fields, `getX()`/
`getY()` accessors on `Point`, index-based list access. Recommended if your project targets Java 17
or you cannot upgrade.

**21.x — Java 21 idioms.** `DoublePoint` is a `record`, making it a true value type with correct
`equals`/`hashCode` and concise syntax. The `Point` interface uses `x()`/`y()` accessors to match
the record convention. Internal code uses `SequencedCollection.getFirst()`/`getLast()`. These
changes enable better JIT inlining, especially for the `DoublePoint` record in the hot path.
**Requires Java 21+.** Breaking change: `getX()`/`getY()` → `x()`/`y()`.

**25.x — Java 25 idioms.** Adds unnamed variables (`_`) where parameters are intentionally unused.
All performance optimisations from 21.x apply here too. The JDK 25 JIT continues to improve
handling of records and value-type-like classes. Future releases of this branch will adopt
Project Valhalla value classes once JEP 401 exits preview.
**Requires Java 25+.** No additional breaking changes beyond 21.x.

## Download

Latest version: 25.2.0

### Maven Central

Maven:

```xml
<dependency>
  <groupId>com.ggalmazor</groupId>
  <artifactId>lttb_downsampling</artifactId>
  <version>25.2.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("com.ggalmazor:lttb_downsampling:25.2.0")
```

### Migrating from 17.x to 21.x

`DoublePoint` is now a `record`. The accessor methods changed:

| Before (17.x) | After (21.x+) |
|---------------|---------------|
| `point.getX()` | `point.x()` |
| `point.getY()` | `point.y()` |

If you implement the `Point` interface directly, update your `getX()`/`getY()` method names to
`x()`/`y()` accordingly.

## Largest-Triangle Three-Buckets

This library implements the LTTB algorithm, which groups points into buckets and selects the point
from each bucket that forms the largest triangle with its neighbours. Two bucketization strategies
are available, corresponding directly to the dynamic and fixed bucket sizes described in the
original paper.

### `DYNAMIC` — equal point count per bucket (default)

Each bucket contains the same number of points. This is the right choice when samples are evenly
distributed across the x-axis.

```java
List<DoublePoint> input = List.of(...);

// Default: DYNAMIC strategy
List<DoublePoint> output = LTThreeBuckets.sorted(input, 200);
```

The first and last points of the input are always preserved. The output always contains exactly
`desiredBuckets + 2` points.

### `FIXED` — equal x-span per bucket

The x range `[x_first, x_last]` is divided into equal-width intervals. Each point is assigned to
the interval containing its `x()` value. This is the right choice for unevenly distributed data
or series with gaps — dense regions and sparse regions receive proportionally sized buckets
regardless of how many points they contain.

```java
import static com.ggalmazor.ltdownsampling.BucketizationStrategy.FIXED;

List<DoublePoint> output = LTThreeBuckets.sorted(input, 200, FIXED);
```

**Empty-interval behaviour:** x-intervals that contain no points are silently skipped. This means
the output may have **fewer than `desiredBuckets + 2` points** when the input has gaps. For
example, requesting 10 buckets on a series with 3 empty intervals will produce at most 9 output
points. Callers should not assume a fixed output size when using `FIXED`.

`x()` must be monotonically non-decreasing across the input list for both strategies.

## Notes on Point types

- The `Point` interface defines `x()` and `y()` (21.x+) or `getX()`/`getY()` (17.x).
- `DoublePoint` is the built-in implementation and is used by default.
- You can implement `Point` directly with your own domain type (e.g., a `DateSeriesPoint` backed
  by a timestamp). The `LTThreeBuckets.sorted()` signature is generic and preserves your type.
- When the input is a `List<DoublePoint>`, the library uses an optimised internal path that
  extracts coordinates into contiguous `double[]` arrays for cache-efficient processing.

## Benchmarks

Measured with [JMH](https://github.com/openjdk/jmh) on an Apple M3 Pro using OpenJDK 25.0.2,
with a fixed-seed synthetic time series (sinusoidal + noise + trend). All three versions ran on
the same machine sequentially without rebooting, using identical input data. Results show average
time per operation in milliseconds — lower is better. Error columns (±) show 99.9% confidence
intervals across 10 measurement iterations (2 forks × 5 iterations).

Run benchmarks locally with:

```bash
mise exec -- ./gradlew jmh
```

### `LTThreeBuckets.sorted` — full downsample (ms/op)

| Input size | Buckets | 17.1.0 | 21.1.0 | 25.1.0 |
|---:|---:|---:|---:|---:|
| 10,000 | 100 | 0.027 | 0.026 | 0.025 |
| 10,000 | 1,000 | 0.077 | 0.067 | 0.059 |
| 10,000 | 5,000 | 0.156 | 0.133 | 0.120 |
| 100,000 | 100 | 0.245 | 0.235 | 0.243 |
| 100,000 | 1,000 | 0.266 | 0.244 | 0.228 |
| 100,000 | 5,000 | 0.368 | 0.322 | 0.300 |
| 500,000 | 100 | 3.163 | 1.740 | 1.875 |
| 500,000 | 1,000 | 1.955 | 1.204 | 1.255 |
| 500,000 | 5,000 | 2.220 | 1.435 | 1.514 |

### `OnePassBucketizer.bucketize` — bucketization step only (ms/op)

| Input size | Buckets | 17.1.0 | 21.1.0 | 25.1.0 |
|---:|---:|---:|---:|---:|
| 10,000 | 100 | 0.001 | 0.001 | 0.001 |
| 10,000 | 1,000 | 0.010 | 0.009 | 0.009 |
| 10,000 | 5,000 | 0.047 | 0.041 | 0.042 |
| 100,000 | 100 | 0.001 | 0.001 | 0.001 |
| 100,000 | 1,000 | 0.011 | 0.011 | 0.010 |
| 100,000 | 5,000 | 0.053 | 0.045 | 0.047 |
| 500,000 | 100 | 0.001 | 0.001 | 0.001 |
| 500,000 | 1,000 | 0.013 | 0.012 | 0.010 |
| 500,000 | 5,000 | 0.073 | 0.065 | 0.053 |

### Reading these results

**Why Java 21 leads on large inputs.** The most significant gains appear at 500,000 points.
Java 17 takes 3.163 ms at 500K/100 buckets; Java 21 takes 1.740 ms — a **~45% reduction**.
The cause is `DoublePoint` becoming a `record`. Records enable the JIT to more aggressively
inline `x()` and `y()` accessors at call sites because the compiler can prove no subclassing
is possible. In the inner triangle-selection loop, which calls `x()` and `y()` on every
candidate point, this inlining eliminates virtual dispatch overhead and allows the CPU to keep
coordinates in registers rather than reloading them from memory.

**Why small inputs (10K) show little difference.** At 10,000 points the algorithm completes in
under 0.2 ms on all three versions. At that scale, JVM startup overhead, warmup effects, and
measurement noise dominate. The per-point optimisations only pay off when the number of
candidate evaluations is large enough to amortise the overhead. The crossover is roughly
50,000–100,000 points.

**Why Java 25 is not uniformly faster than Java 21.** Java 25 is a very recent release (GA
March 2025). Its JIT compiler is still being tuned for the record and value-class patterns that
Project Valhalla is introducing. In some scenarios (e.g. 500K/100 buckets: 1.875 ms vs 1.740
ms) Java 25 trails Java 21 slightly. This is expected to improve in subsequent JDK 25 updates
and in JDK 26+. The 25.x branch is best understood as forward-looking: it is ready to adopt
Valhalla value classes (`value record DoublePoint`) as soon as JEP 401 exits preview, which
would eliminate heap allocation for `DoublePoint` entirely and could yield another step-change
in performance.

**The `bucketize` benchmark isolates a different concern.** Bucketization (partitioning the
input into equal-sized windows) is now O(1) per bucket using `subList` views — it never copies
elements. The numbers in this table reflect only the cost of computing bucket boundaries and
center points, which is dominated by `Point.centerBetween()` arithmetic. All three versions
are essentially equivalent here; differences are within measurement noise.

## Contributing

This project enforces [Google Java Style](https://google.github.io/styleguide/javaguide.html) via Checkstyle. The configuration lives in `config/checkstyle/`. Run it with:

```bash
./gradlew checkstyleMain
```

Checkstyle runs automatically as part of `./gradlew build`. The `test` and `jmh` source sets are excluded from Checkstyle. The only active suppression is an inline `@SuppressWarnings` on `LTThreeBuckets` to allow the `LT` domain abbreviation.

The JDK version is managed by [mise](https://mise.jdx.dev/). Run `mise install` to get the correct JDK for this project.

## Other Java implementations you might want to check

 - [drcrane/downsample](https://github.com/drcrane/downsample)
 - [n52.io](http://www.programcreek.com/java-api-examples/index.php?source_dir=sensorweb-rest-api-master/timeseries-io/src/main/java/org/n52/io/generalize/LargestTriangleThreeBucketsGeneralizer.java)
