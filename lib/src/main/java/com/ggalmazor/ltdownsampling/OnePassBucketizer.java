package com.ggalmazor.ltdownsampling;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that divides the input list of {@link Point} points into {@link Bucket} buckets as per the LTTB algorithm.
 * <p>
 * In LTTB, the first and last buckets always include a single point corresponding to the first and last points of the input series.
 */
class OnePassBucketizer {

  /**
   * Returns the list of {@link Bucket} buckets from the <code>input</code> list of points and using the provided <code>inputSize</code> and <code>desiredBuckets</code> params
   *
   * @param input the input list of points
   * @param inputSize the size of the input list of points
   * @param desiredBuckets the desired bucket size
   * @return the list of buckets
   * @param <T> the type of the {@link Point} points in the input list
   */
  static <T extends Point> List<Bucket<T>> bucketize(List<T> input, int inputSize, int desiredBuckets) {
    int middleSize = inputSize - 2;
    int bucketSize = middleSize / desiredBuckets;
    int remainingElements = middleSize % desiredBuckets;

    if (bucketSize == 0)
      throw new IllegalArgumentException("Can't produce " + desiredBuckets + " buckets from an input series of " + (middleSize + 2) + " elements");

    List<Bucket<T>> buckets = new ArrayList<>(desiredBuckets + 2);

    // Add first point as the only point in the first bucket
    buckets.add(Bucket.of(input.get(0)));

    // Add middle buckets using index-based iteration to avoid subList operations
    int currentIndex = 1; // Start after the first element
    for (int bucketIndex = 0; bucketIndex < desiredBuckets; bucketIndex++) {
      int currentBucketSize = bucketIndex < remainingElements ? bucketSize + 1 : bucketSize;

      List<T> bucketData = new ArrayList<>(currentBucketSize);
      for (int i = 0; i < currentBucketSize; i++) {
        bucketData.add(input.get(currentIndex + i));
      }

      buckets.add(Bucket.of(bucketData));
      currentIndex += currentBucketSize;
    }

    // Add last point as the only point in the last bucket
    buckets.add(Bucket.of(input.get(input.size() - 1)));

    return buckets;
  }
}
