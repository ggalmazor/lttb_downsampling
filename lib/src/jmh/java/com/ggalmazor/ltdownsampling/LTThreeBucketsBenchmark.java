package com.ggalmazor.ltdownsampling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class LTThreeBucketsBenchmark {

  // Fixed seed for reproducible, cross-version comparable results
  private static final long SEED = 0xDEADBEEFL;

  @Param({"10000", "100000", "500000"})
  private int dataSize;

  @Param({"100", "1000", "5000"})
  private int desiredBuckets;

  private List<DoublePoint> data;

  @Setup(Level.Trial)
  public void setup() {
    data = generateTestData(dataSize);
  }

  @Benchmark
  public void downsample(Blackhole bh) {
    bh.consume(LTThreeBuckets.sorted(data, desiredBuckets));
  }

  @Benchmark
  public void bucketize(Blackhole bh) {
    bh.consume(OnePassBucketizer.bucketize(data, data.size(), desiredBuckets));
  }

  private List<DoublePoint> generateTestData(int size) {
    Random random = new Random(SEED);
    List<DoublePoint> points = new ArrayList<>(size);
    double baseValue = 100.0;
    double trend = 0.001;
    for (int i = 0; i < size; i++) {
      double x = i;
      double y = baseValue + (trend * i) + (Math.sin(i * 0.01) * 10) + (random.nextDouble() * 5 - 2.5);
      points.add(new DoublePoint(x, y));
    }
    return points;
  }
}
