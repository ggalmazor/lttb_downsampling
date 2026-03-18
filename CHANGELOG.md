# Changelog

## `main`

## Release 17.1.0

- Performance: `Point.centerBetween()` reduced from 3 allocations and 10 arithmetic ops to 1
  allocation and 4 ops
- Performance: `OnePassBucketizer` now uses `subList` views instead of copying elements into a new
  `ArrayList` per bucket — eliminates `O(desiredBuckets)` intermediate list allocations
- Performance: `Triangle.getResult()` inlines the max-finding loop directly, eliminating the
  intermediate `List<Area>` and all per-candidate `Area` record allocations
- Performance: `LTThreeBuckets.sorted()` uses a struct-of-arrays fast path for `DoublePoint`
  inputs — coordinates are extracted into contiguous `double[]` arrays once before the
  selection loop, eliminating per-point pointer chasing in the hot path
- Performance: Triangle selection loop runs in parallel via `ForkJoinPool` when
  `desiredBuckets >= 512`, using disjoint array slots to avoid race conditions
- Cleanup: removed dead `Area.ofTriangle()`, `Triangle.getFirst/getLast/of(List)`,
  `Bucket.map()`, and the entire `tools` subpackage (`SlidingCollector`, `CustomCollectors`)

## Release 17.0.0

- Migrated to versioning scheme aligned with Java LTS versions (Java 17 → 17.x.x)
- Published to Maven Central (previously JitPack only)
- Added Maven Central publish workflow (`publish.yml`) triggered on semver tags
- Added Checkstyle (Google Java Style) enforced on `main` sources via `./gradlew checkstyleMain`
- CI matrix trimmed to LTS versions only: Java 17, 21, 25
- Removed unused `commons-math3` and `guava` entries from version catalog
- Fixed redundant cast `((Point) vector)` in `Point.centerBetween`
- Fixed braceless `if` in `OnePassBucketizer`
- Fixed `RuntimeException` in `Triangle.getResult()` replaced with `IllegalStateException`
- Added private constructor to `LTThreeBuckets`
- Fixed all Javadoc style issues across main sources
- Fixed import ordering across main sources (static imports first)

## Release 1.1.0

- Added benchmarks
- Refactored implementation to get a 3x to 4x performance boost

## Release 0.1.0

- This lib no longer relies on `BigDecimal` to improve its performance and memory usage
- `Point` has been divided into an interface called `Point` and an implementation called `DoublePoint`
  - Users can now implement their own `Point` class, or extend `DoublePoint` and interact with them with this library
  - Implementing `Point` only requires to provide a `Double getX()` and `Double getY()` methods, simplifying the
    adaptation work

## Release 0.0.7

- CHANGED: Distribute points uniformly into all middle buckets #2
