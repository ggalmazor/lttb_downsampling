plugins {
  `java-library`
  `maven-publish`
  id("me.champeau.jmh") version "0.7.2"
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(libs.junit.jupiter)
  testImplementation("org.hamcrest:hamcrest:3.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  jmh("org.openjdk.jmh:jmh-core:1.37")
  jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}

tasks.javadoc {
  source = sourceSets.main.get().allJava
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "com.ggalmazor"
      artifactId = "lttb_downsampling"
      version = "1.0.2"

      from(components["java"])
    }
  }
}

jmh {
  iterations = 5
  warmupIterations = 3
  fork = 2
  benchmarkMode = listOf("avgt")
  timeUnit = "ms"
}
