plugins {
  `java-library`
  `maven-publish`
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(libs.junit.jupiter)
  testImplementation("org.hamcrest:hamcrest:3.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
