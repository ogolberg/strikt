import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA_PARALLEL
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.codearte.gradle.nexus.NexusStagingExtension
import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.KotlinterExtension
import kotlin.text.RegexOption.IGNORE_CASE

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.plugin.spring) apply false
  alias(libs.plugins.nexus.staging)
  alias(libs.plugins.kotlinter) apply false
  alias(libs.plugins.test.logger) apply false
  alias(libs.plugins.ben.manes.versions)
  // dokka is brought onto the classpath by the `published` convention plugin
  // (buildSrc), so it is applied here without a version to avoid a
  // "plugin already on the classpath with an unknown version" clash.
  id("org.jetbrains.dokka")
  alias(libs.plugins.kover)
}

repositories {
  mavenCentral()
  // needed for dokka plugin, feels like this belongs in published.gradle.kts but it doesn't work there
  maven {
    url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
  }
}

allprojects {
  group = "io.strikt"
}

subprojects {
  repositories {
    mavenCentral()
    // needed for dokka plugin, feels like this belongs in published.gradle.kts but it doesn't work there
    maven {
      url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    }
  }

  afterEvaluate {
    plugins.withId("kotlin") {
      configure<JavaPluginExtension> {
        sourceCompatibility = VERSION_17
      }

      tasks.withType<KotlinCompile> {
        compilerOptions {
          jvmTarget.set(JVM_17)
          languageVersion.set(KOTLIN_2_2)
          javaParameters = true
          jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
          allWarningsAsErrors = true
        }
      }

      dependencies {
        "implementation"(platform(libs.kotlin.bom))
        "implementation"(platform(libs.kotlinx.coroutines.bom))

        "testImplementation"(platform(libs.junit.bom))
        "testImplementation"(libs.junit.jupiter.api)
        "testRuntimeOnly"(libs.junit.jupiter.engine)
        // Gradle 9 no longer adds the JUnit Platform launcher to the test
        // runtime classpath automatically; declare it explicitly.
        "testRuntimeOnly"(libs.junit.platform.launcher)
      }

      // Test with JUnit 5
      tasks.withType<Test> {
        systemProperty("junit.jupiter.execution.parallel.enabled", "false")
        useJUnitPlatform {
          includeEngines("junit-jupiter", "failgood")
        }
      }

      // Lint Kotlin code
      apply(plugin = "org.jmailen.kotlinter")
      configure<KotlinterExtension> {
        ignoreLintFailures = true
        ignoreFormatFailures = true
        reporters = arrayOf("html", "plain")
      }
    }
  }

  apply(plugin = "com.adarshr.test-logger")
  configure<TestLoggerExtension> {
    theme = MOCHA_PARALLEL
    showSimpleNames = true
  }
}

configure<NexusStagingExtension> {
  stagingProfileId = "3fc70880a122f"
}

// Dependency updates configuration
fun ModuleComponentIdentifier.isNonStable() =
  version.contains(Regex("""-(m|eap|rc|alpha|beta|b)([-\.]?[\d-]+)?""", IGNORE_CASE))

tasks.withType<DependencyUpdatesTask> {
  revision = "release"
  checkConstraints = true
  gradleReleaseChannel = "current"
  checkForGradleUpdate = true
  rejectVersionIf {
    candidate.isNonStable()
  }
}
