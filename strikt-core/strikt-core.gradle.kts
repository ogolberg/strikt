import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0

plugins {
  kotlin("multiplatform")
  id("org.jmailen.kotlinter")
}

description = "The core API for Strikt."

kotlin {
  applyDefaultHierarchyTemplate()

  jvm {
    // The existing JVM test suite includes a Java source (PersonJava) used by
    // reflection-based assertion tests, so the JVM target compiles Java too.
    withJava()
    compilerOptions {
      jvmTarget.set(JVM_17)
      javaParameters = true
      freeCompilerArgs.add("-Xjvm-default=all")
    }
    // Test with JUnit 5 on the JVM (the existing test suite is JVM-only for now).
    testRuns["test"].executionTask.configure {
      systemProperty("junit.jupiter.execution.parallel.enabled", "false")
      useJUnitPlatform {
        includeEngines("junit-jupiter", "failgood")
      }
    }
  }

  // filepeek (used to auto-describe `get { ... }` lambda mappings) reconstructs
  // a `.kt` source path from the compiled class's output directory by replacing
  // the fixed segment `build/classes/kotlin/test` with a source root. The
  // multiplatform plugin emits jvm test classes to
  // `build/classes/kotlin/jvm/test` (extra `jvm` segment), which filepeek does
  // not recognise, so it can't find the source and mapping descriptions fall
  // back to "%s". Redirect the jvm test output to the flat layout filepeek
  // expects, and add the multiplatform source roots to FilePeek's config.
  tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileTestKotlinJvm") {
    destinationDirectory.set(layout.buildDirectory.dir("classes/kotlin/test"))
  }

  js(IR) {
    nodejs()
    browser()
  }

  // Representative native targets. Others (mingwX64, iosX64, ...) can be added
  // the same way; the non-JVM `actual`s live in nonJvmMain + nativeMain.
  macosArm64()
  linuxX64()

  compilerOptions {
    languageVersion.set(KOTLIN_2_0)
    allWarningsAsErrors.set(true)
  }

  sourceSets {
    // A source set shared by every non-JVM target. The JVM has first-class
    // support (opentest4j, filepeek, JDK Formatter, full reflection); JS and
    // Native share reflection-free implementations of the same seams, so those
    // live here rather than being duplicated per platform.
    val nonJvmMain by creating {
      dependsOn(commonMain.get())
    }
    jsMain.get().dependsOn(nonJvmMain)
    nativeMain.get().dependsOn(nonJvmMain)

    commonMain.dependencies {
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("versions.kotlinx-coroutines")}")
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
    }

    jvmMain.dependencies {
      api("org.opentest4j:opentest4j:${property("versions.opentest4j")}")
      implementation("com.christophsturm:filepeek:${property("versions.filepeek")}")
    }

    jvmTest.dependencies {
      implementation(platform("org.junit:junit-bom:${property("versions.junit")}"))
      implementation("org.junit.jupiter:junit-jupiter-api")
      runtimeOnly("org.junit.jupiter:junit-jupiter-engine")
      implementation("dev.failgood:failgood:${property("versions.failgood")}")
      implementation("dev.minutest:minutest:${property("versions.minutest")}")
    }
  }
}
