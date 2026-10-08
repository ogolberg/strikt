import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URI

plugins {
  kotlin("jvm")
  id("published")
}

description = "Extensions for assertions and traversals on types Jackson's JsonNode and sub-types."

dependencies {
  api(project(":strikt-core"))

  compileOnly(platform(libs.jackson.bom))
  compileOnly("com.fasterxml.jackson.core:jackson-databind")

  testImplementation(platform(libs.jackson.bom))
  testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  testImplementation(libs.minutest)
}

tasks.withType<DokkaTaskPartial>().configureEach {
  dokkaSourceSets {
    configureEach {
    "https://fasterxml.github.io/jackson-databind/javadoc/2.12/".also {
      externalDocumentationLink {
        url.set(URI(it).toURL())
        packageListUrl.set(URI("${it}package-list").toURL())
      }
    }
    }
  }
}
