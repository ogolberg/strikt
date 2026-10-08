package strikt

import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSuccess
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Platform-agnostic assertions that exercise the multiplatform `commonMain` API
 * through `kotlin.test`. These run — and pass — on every target (JVM, JS,
 * Native), exercising failure rendering (the `%s`/`%d` placeholder substitution
 * in `formatDescription`), the failure exception factories, the
 * suspend `runBlockingCompat` bridge, and mapping-description derivation.
 */
class CommonAssertionsTest {
  @Test
  fun equalValuesPass() {
    expectThat(1 + 1).isEqualTo(2)
  }

  @Test
  fun comparableValuesPass() {
    expectThat(10).isGreaterThan(1)
  }

  @Test
  fun nullabilityAssertionsPass() {
    expectThat<String?>("strikt").isNotNull()
    expectThat<String?>(null).isNull()
  }

  /** Exercises the failure factory + `%s` substitution in `formatDescription`. */
  @Test
  fun failingAssertionReportsNicely() {
    val error =
      assertFailsWith<AssertionError> {
        expectThat("David").isEqualTo("Ziggy")
      }
    expectThat(error.message)
      .isNotNull()
      .contains("is equal to \"Ziggy\"")
  }

  /** Exercises `%d` substitution in `formatDescription` (via `hasSize`). */
  @Test
  fun numericPlaceholderIsRendered() {
    val error =
      assertFailsWith<AssertionError> {
        expectThat(listOf(1, 2, 3)).hasSize(5)
      }
    expectThat(error.message)
      .isNotNull()
      .contains("has size 5")
  }

  /**
   * A `%` in a description that is not a `%s`/`%d` placeholder is rendered
   * verbatim. (With the former JVM `String.format` rendering this threw
   * `UnknownFormatConversionException` while building the failure report.)
   */
  @Test
  fun strayPercentInDescriptionIsHarmless() {
    val error =
      assertFailsWith<AssertionError> {
        expectThat("partial").describedAs("has 100% coverage").isEqualTo("full")
      }
    expectThat(error.message)
      .isNotNull()
      .contains("has 100% coverage")
  }

  /** `%%` in a description renders as a literal `%`. */
  @Test
  fun escapedPercentRendersLiterally() {
    val error =
      assertFailsWith<AssertionError> {
        expectThat(0.99).describedAs("coverage of 99%% (%s)").isEqualTo(1.0)
      }
    expectThat(error.message)
      .isNotNull()
      .contains("coverage of 99% (0.99)")
  }

  /** Exercises the suspend `runBlockingCompat` bridge via `expectCatching`. */
  @Test
  fun catchingRunsSuspendingBlock() {
    expectCatching { 40 + 2 }
      .isSuccess()
      .isEqualTo(42)
  }

  /** Exercises the compound-failure factory for a multi-assertion block. */
  @Test
  fun blockCollectsMultipleFailures() {
    val error =
      assertFailsWith<AssertionError> {
        expectThat(5) {
          isEqualTo(6)
          isGreaterThan(10)
        }
      }
    val message = error.message
    assertTrue(message != null && message.contains("is equal to 6"))
    assertTrue(message.contains("is greater than 10"))
  }
}
