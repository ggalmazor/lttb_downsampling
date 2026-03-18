plugins {
  `java-library`
  checkstyle
  id("com.vanniktech.maven.publish") version "0.36.0"
  id("me.champeau.jmh") version "0.7.3"
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

mavenPublishing {
  publishToMavenCentral(automaticRelease = true)
  signAllPublications()

  coordinates("com.ggalmazor", "lttb_downsampling", "17.0.0")

  pom {
    name.set("lttb_downsampling")
    description.set("Largest-Triangle Three-Buckets time-series downsampling algorithm for modern Java")
    inceptionYear.set("2016")
    url.set("https://github.com/ggalmazor/lttb_downsampling")
    licenses {
      license {
        name.set("Apache-2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("repo")
      }
    }
    developers {
      developer {
        id.set("ggalmazor")
        name.set("Guillermo Gutierrez Almazor")
        url.set("https://github.com/ggalmazor/")
      }
    }
    scm {
      url.set("https://github.com/ggalmazor/lttb_downsampling/")
      connection.set("scm:git:git://github.com/ggalmazor/lttb_downsampling.git")
      developerConnection.set("scm:git:ssh://git@github.com/ggalmazor/lttb_downsampling.git")
    }
  }
}

checkstyle {
  toolVersion = "10.21.4"
  configFile = file("../config/checkstyle/checkstyle.xml")
  configProperties["org.checkstyle.google.suppressionfilter.config"] =
    file("../config/checkstyle/checkstyle-suppressions.xml").absolutePath
}

tasks.named<Checkstyle>("checkstyleTest") { isEnabled = false }
tasks.named<Checkstyle>("checkstyleJmh") { isEnabled = false }

jmh {
  iterations = 5
  warmupIterations = 3
  fork = 2
  benchmarkMode = listOf("avgt")
  timeUnit = "ms"
}
