import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2

plugins {
  kotlin("multiplatform")
  id("org.jmailen.kotlinter")
}

description = "The core API for Strikt."

// The root build applies this under `plugins.withId("kotlin")`, which does not
// match this module's multiplatform plugin id, so configure kotlinter here to
// keep the same non-failing lint behaviour as the rest of the project.
configure<org.jmailen.gradle.kotlinter.KotlinterExtension> {
  ignoreLintFailures = true
  ignoreFormatFailures = true
  reporters = arrayOf("html", "plain")
}

// Pin the Java compilations (e.g. the JVM test target's `compileJvmTestJava`,
// which builds the PersonJava fixture) to release 17 so they match Kotlin's
// jvmTarget of 17. Without this, on a runner with a newer JDK (e.g. CI's Java
// 20) the Java task targets that JDK's version while Kotlin targets 17, and
// Gradle 9 / Kotlin 2.4 hard-error on the inconsistency ("Inconsistent
// JVM-target compatibility"). Using `--release` (rather than a JVM toolchain)
// avoids requiring a separate JDK 17 to be installed on the runner.
tasks.withType<JavaCompile>().configureEach {
  options.release.set(17)
}

kotlin {
  applyDefaultHierarchyTemplate()

  jvm {
    // The JVM test suite includes a Java source (PersonJava) used by
    // reflection-based assertion tests; the multiplatform JVM target compiles
    // Java sources by default.
    compilerOptions {
      jvmTarget.set(JVM_17)
      javaParameters = true
      jvmDefault.set(org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode.NO_COMPATIBILITY)
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

  js {
    nodejs()
    browser()
  }

  // Representative native targets. Others (mingwX64, iosX64, ...) can be added
  // the same way; the non-JVM `actual`s live in nonJvmMain + nativeMain.
  macosArm64()
  linuxX64()

  compilerOptions {
    languageVersion.set(KOTLIN_2_2)
    allWarningsAsErrors.set(true)
  }

  // The *MainKotlinMetadata compilations emit a benign KLIB loader warning
  // ("the same 'unique_name=kotlin-stdlib-common' found in more than one
  // library") when an intermediate source set (nonJvmMain) sits between
  // commonMain and the leaf targets. It is a Kotlin 2.4 metadata-compiler quirk,
  // not a problem with our code, so don't let it trip -Werror.
  metadata {
    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions.allWarningsAsErrors.set(false)
      }
    }
  }

  sourceSets {
    // A source set shared by every non-JVM target. The JVM has first-class
    // support (opentest4j, filepeek, JDK Formatter, full reflection); JS and
    // Native share reflection-free implementations of the same seams, so those
    // live here rather than being duplicated per platform.
    val nonJvmMain = create("nonJvmMain") {
      dependsOn(commonMain.get())
    }
    jsMain.get().dependsOn(nonJvmMain)
    nativeMain.get().dependsOn(nonJvmMain)

    commonMain.dependencies {
      implementation(libs.kotlinx.coroutines.core)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
    }

    jvmMain.dependencies {
      api(libs.opentest4j)
      implementation(libs.filepeek)
    }

    jvmTest.dependencies {
      implementation(project.dependencies.platform(libs.junit.bom))
      implementation(libs.junit.jupiter.api)
      runtimeOnly(libs.junit.jupiter.engine)
      // Gradle 9 no longer adds the JUnit Platform launcher to the test runtime
      // classpath automatically; declare it explicitly.
      runtimeOnly(libs.junit.platform.launcher)
      implementation(libs.failgood)
      implementation(libs.minutest)
    }
  }
}
