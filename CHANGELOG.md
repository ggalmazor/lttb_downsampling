# Changelog

## `main`

## Release 21.0.0

- Raised Java baseline to 21
- `DoublePoint` converted to a `record` (breaking: `getX()`/`getY()` replaced by `x()`/`y()`)
- `Area<T>` converted to a `record` (internal, no API impact)
- `Triangle` fields split to one per line (style)
- `Bucket.of()` uses `getFirst()`/`getLast()` (Java 21 `SequencedCollection`)
- `SlidingCollector.combiner()` uses unnamed variables `(_, _)`
- CI matrix: Java 21, 25

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
