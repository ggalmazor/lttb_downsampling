package com.ggalmazor.ltdownsampling;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class LTThreeBucketsBenchmark {

  @Param({"1000000", "2000000", "5000000"})
  private int dataSize;

  @Param({"1000", "5000", "10000"})
  private int desiredBuckets;

  private List<DoublePoint> data;

  @Setup(Level.Trial)
  public void setup() {
    data = generateTestData(dataSize);
  }

  @Benchmark
  public void benchmarkLTThreeBuckets(Blackhole bh) {
    List<DoublePoint> result = LTThreeBuckets.sorted(data, desiredBuckets);
    bh.consume(result);
  }

  @Benchmark
  public void benchmarkLTThreeBucketsWithSize(Blackhole bh) {
    List<DoublePoint> result = LTThreeBuckets.sorted(data, data.size(), desiredBuckets);
    bh.consume(result);
  }

  private List<DoublePoint> generateTestData(int size) {
    List<DoublePoint> points = new ArrayList<>(size);

    // Generate realistic time series data with some noise
    double baseValue = 100.0;
    double trend = 0.001; // Small upward trend

    for (int i = 0; i < size; i++) {
      double x = i;
      double y = baseValue + (trend * i) + (Math.sin(i * 0.01) * 10) + (Math.random() * 5 - 2.5);
      points.add(new DoublePoint(x, y));
    }

    return points;
  }

  // Additional benchmark for measuring individual components
  @Benchmark
  public void benchmarkBucketization(Blackhole bh) {
    List<Bucket<DoublePoint>> buckets = OnePassBucketizer.bucketize(data, data.size(), desiredBuckets);
    bh.consume(buckets);
  }
}
