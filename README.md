# Largest-Triangle time-series downsampling algorithm implementation for modern Java
[![Maven Central](https://img.shields.io/maven-central/v/com.ggalmazor/lttb_downsampling.svg)](https://central.sonatype.com/artifact/com.ggalmazor/lttb_downsampling)
[![CI](https://github.com/ggalmazor/lttb_downsampling/actions/workflows/ci.yml/badge.svg)](https://github.com/ggalmazor/lttb_downsampling/actions/workflows/ci.yml)

These implementations are based on the paper *"Downsampling Time Series for Visual Representation"* by Sveinn Steinarsson from the Faculty of Industrial Engineering, Mechanical Engineering and Computer Science University of Iceland (2013). You can read the paper [here](http://skemman.is/stream/get/1946/15343/37285/3/SS_MSthesis.pdf)

The goal of Largest-Triangle downsampling algorithms for data visualization is to reduce the number of points in a number series without losing important visual features of the resulting graph. However, it is essential to know these algorithms are not numerically correct.

See how this algorithm compares to other algorithms designed to keep local extrema in the input series at [ggalmazor.com/blog/evaluating_downsampling_algorithms.html](https://ggalmazor.com/blog/evaluating_downsampling_algorithms.html)

Javadoc at [ggalmazor.com/lttb_downsampling](https://ggalmazor.com/lttb_downsampling)

## Java version support

| Version | Java baseline | Status                  |
|---------|---------------|-------------------------|
| 17.x.x  | Java 17       | Active (bug fixes only) |
| 21.x.x  | Java 21       | Active                  |
| 25.x.x  | Java 25       | Active                  |

The library version number reflects the minimum Java version required to use it.

## Download

Latest version: 25.0.0

### Maven Central

Maven:

```xml
<dependency>
  <groupId>com.ggalmazor</groupId>
  <artifactId>lttb_downsampling</artifactId>
  <version>25.0.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("com.ggalmazor:lttb_downsampling:25.0.0")
```

### Migrating from 17.x

`DoublePoint` is now a `record`. The accessor methods changed:

| Before | After |
|--------|-------|
| `point.getX()` | `point.x()` |
| `point.getY()` | `point.y()` |

If you implement the `Point` interface directly, no change is needed.

## Largest-Triangle Three-Buckets

This version of the algorithm groups numbers in buckets of the same size and then selects the point that produces the largest area from each bucket with points in neighboring buckets.

You can produce a downsampled version of an input series with:

```java
List<Point> input = Arrays.asList(...);
int numberOfBuckets = 200;

List<Point> output = LTThreeBuckets.ofSorted(input, numberOfBuckets);
```

The first and last points of the original series are always in the output. The rest are grouped into the defined number of buckets, and the algorithm chooses the best point from each bucket, resulting in a list of 202 elements.

## Notes on Point types

- This library must provide lists of instances of the `Point` supertype.
- It also provides and uses internally the `DoublePoint` subtype, which can also be used to feed data to the library.
- However, users can create implementations of `Point` that best fit their Domain.

## Largest-Triangle Dynamic

Not yet implemented

## Example

This is how a raw time series with ~5000 data points and downsampled versions (2000, 500, and 250 buckets) look like (graphed by AirTable)
![image](https://user-images.githubusercontent.com/205913/202478853-180c56ff-41af-43b3-8830-6d51ac7cfbb3.png)
![image](https://user-images.githubusercontent.com/205913/202478930-dd482a9f-0da1-4e6b-8537-f7a2fbe68991.png)
![image](https://user-images.githubusercontent.com/205913/202478994-28ae49ff-6036-43d1-8000-6730a55f8a77.png)
![image](https://user-images.githubusercontent.com/205913/202480858-51ef82fc-6432-4447-942a-65edfa82a742.png)

These are close-ups for 250, 500, 1000, and 2000 buckets with raw data in the back:
![image](https://user-images.githubusercontent.com/205913/202486056-25a612b1-7294-4967-9714-000cfcd5177e.png)
![image](https://user-images.githubusercontent.com/205913/202486255-b42f7e90-29fc-45f9-be54-f30b4a6d1e07.png)
![image](https://user-images.githubusercontent.com/205913/202486337-b402dd24-44dd-4456-af3d-add931e7fbd7.png)
![image](https://user-images.githubusercontent.com/205913/202486396-ff3772d3-ef69-4c69-b56c-4ac16964ed04.png)


## Benchmarks

Measured with [JMH](https://github.com/openjdk/jmh) on an Apple M3 Pro, using a fixed-seed
synthetic time series (sinusoidal + noise + trend). All runs used the same input data.
Results are average time per operation in milliseconds (lower is better).

Run benchmarks locally with:

```bash
mise exec -- ./gradlew jmh
```

### `LTThreeBuckets.sorted` — full downsample

| Input size | Buckets | 17.x (Java 17) | 21.x (Java 21) | 25.x (Java 25) |
|---:|---:|---:|---:|---:|
| 10,000 | 100 | 0.122 ms | 0.122 ms | 0.142 ms |
| 10,000 | 1,000 | 0.134 ms | 0.131 ms | 0.154 ms |
| 10,000 | 5,000 | 0.261 ms | 0.227 ms | 0.216 ms |
| 100,000 | 100 | 1.307 ms | 1.266 ms | 1.475 ms |
| 100,000 | 1,000 | 1.265 ms | 1.261 ms | 1.436 ms |
| 100,000 | 5,000 | 1.259 ms | 1.265 ms | 1.411 ms |
| 500,000 | 100 | 7.752 ms | **6.605 ms** | 7.539 ms |
| 500,000 | 1,000 | 7.973 ms | **6.613 ms** | 8.591 ms |
| 500,000 | 5,000 | 8.192 ms | **6.810 ms** | 7.536 ms |

### `OnePassBucketizer.bucketize` — bucketization only

| Input size | Buckets | 17.x (Java 17) | 21.x (Java 21) | 25.x (Java 25) |
|---:|---:|---:|---:|---:|
| 10,000 | 100 | 0.038 ms | 0.039 ms | 0.039 ms |
| 10,000 | 1,000 | 0.054 ms | 0.054 ms | 0.049 ms |
| 10,000 | 5,000 | 0.080 ms | 0.086 ms | 0.072 ms |
| 100,000 | 100 | 0.362 ms | 0.387 ms | **0.269 ms** |
| 100,000 | 1,000 | 0.391 ms | 0.378 ms | 0.346 ms |
| 100,000 | 5,000 | 0.446 ms | 0.426 ms | 0.437 ms |
| 500,000 | 100 | 2.163 ms | **2.056 ms** | **1.581 ms** |
| 500,000 | 1,000 | 2.811 ms | **2.156 ms** | 2.408 ms |
| 500,000 | 5,000 | 3.009 ms | **2.276 ms** | 2.081 ms |

**Java 21 shows the most consistent gains** for large inputs (~15% faster than Java 17 at 500K
points in the full downsample path), driven by improved JIT inlining of the `DoublePoint` record.
Java 25 shows strong `bucketize` improvements at larger input sizes but higher variance in the full
downsample path, reflecting a still-maturing JIT for the newer JDK release.

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
