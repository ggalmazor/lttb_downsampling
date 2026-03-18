# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java implementation of the Largest-Triangle-Three-Buckets (LTTB) time-series downsampling algorithm. This is a library (not an application) published to Maven Central as `com.ggalmazor:lttb_downsampling`, targeting Java 17+.

The library version number reflects the minimum Java version required: `17.x.x` requires Java 17, `21.x.x` requires Java 21, etc.

JDK version is managed by [mise](https://mise.jdx.dev/). Run `mise exec -- ./gradlew` to ensure the correct JDK is used. Or fix `JAVA_HOME` in your shell profile with `export JAVA_HOME=$(mise where java)`.

## Build Commands

```bash
mise exec -- ./gradlew build             # Build + test + checkstyle
mise exec -- ./gradlew test              # Run tests only
mise exec -- ./gradlew checkstyleMain    # Run Checkstyle on main sources only
mise exec -- ./gradlew jmh               # Run JMH benchmarks
mise exec -- ./gradlew javadoc           # Generate Javadoc
mise exec -- ./gradlew publishToMavenLocal  # Publish to local Maven repo (dry-run)
```

The project uses Gradle with a single `lib` subproject. All source code lives under `lib/src/`.

Run a single test class:
```bash
mise exec -- ./gradlew test --tests "com.ggalmazor.ltdownsampling.LargestTriangleThreeBucketsTest"
```

## Code Style

This project enforces [Google Java Style](https://google.github.io/styleguide/javaguide.html) via Checkstyle 10.x. Configuration lives in `config/checkstyle/`:

- `checkstyle.xml` — Google checks with `LineLength` raised to 120
- `checkstyle-suppressions.xml` — currently empty; prefer inline `@SuppressWarnings("checkstyle:RuleName")` for targeted suppressions
- Checkstyle runs on `main` sources only; `test` and `jmh` source sets are excluded
- The only active suppression is `@SuppressWarnings("checkstyle:AbbreviationAsWordInName")` on `LTThreeBuckets` (LT is a domain abbreviation)
- Static imports must come before regular imports (Google style)

## Publishing

Publishing to Maven Central is triggered by pushing a `v*.*.*` tag. The workflow uses the `vanniktech/gradle-maven-publish-plugin` with in-memory GPG signing via GitHub secrets:

- `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD` — Sonatype portal token
- `GPG_SIGNING_KEY` — armored private key (`gpg --armor --export-secret-keys <KEY_ID>`)
- `GPG_SIGNING_PASSWORD` — key passphrase (empty string if none)

## Architecture

The algorithm flow: `LTThreeBuckets.sorted()` → `OnePassBucketizer.bucketize()` → `Triangle.of()` per sliding window of 3 buckets → select point with largest area from each middle bucket.

Key types:
- **`Point`** — interface with `getX()`/`getY()`. Users implement this for their domain types.
- **`DoublePoint`** — built-in `Point` implementation using doubles.
- **`LTThreeBuckets`** — public API entry point. Static methods only, immutable (never mutates input).
- **`Bucket<T>`** — groups points into equal-sized segments.
- **`Triangle<T>`** — operates on a 3-bucket sliding window to pick the point forming the largest triangle area.
- **`Area`** — triangle area computation.
- **`OnePassBucketizer`** — splits the input list into buckets in a single pass.

The `tools` subpackage contains `SlidingCollector` and `CustomCollectors` (stream collector utilities, retained as public API).

## Testing

Tests use JUnit 5 + Hamcrest matchers. The test directory includes `DateSeriesPoint` (a custom `Point` implementation for testing with dates) and `PointMatcher` (custom Hamcrest matcher).

CI tests against Java 17, 21, and 25 (LTS versions only).
