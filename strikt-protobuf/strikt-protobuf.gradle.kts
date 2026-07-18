@file:Suppress("KDocMissingDocumentation")

import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URI

plugins {
  kotlin("jvm")
  id("published")
  id("com.google.protobuf") version "0.9.4"
}

description = "Extensions for testing code that uses Protobuf / gRPC."

dependencies {
  api(project(":strikt-core"))

  compileOnly(libs.protobuf.java)
  testImplementation(libs.protobuf.java)
}

tasks.withType<DokkaTaskPartial>().configureEach {
  dokkaSourceSets {
    configureEach {
      "https://developers.google.com/protocol-buffers/docs/reference/java/".also {
        externalDocumentationLink {
          url.set(URI(it).toURL())
          packageListUrl.set(URI("${it}package-list").toURL())
        }
      }
    }
  }
}

protobuf {
  protoc {
    artifact = libs.protobuf.protoc.get().let { "${it.module.group}:${it.module.name}:${it.version}" }
  }
  generateProtoTasks {
    ofSourceSet("test")
  }
}

sourceSets {
  getByName("test") {
    kotlin.srcDirs(
      "src/test/kotlin",
      "$buildFile/generated/source/proto/test/java"
    )
  }
}
