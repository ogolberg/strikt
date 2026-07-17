package strikt.internal

import strikt.internal.opentest4j.AssertionFailed
import strikt.internal.opentest4j.CompoundAssertionFailure
import strikt.internal.opentest4j.IncompleteAssertion
import strikt.internal.opentest4j.MappingFailed

/**
 * Failure-exception factories for non-JVM targets.
 *
 * The JVM `actual`s delegate to the `org.opentest4j` library so IDE/JUnit test
 * runners can render rich comparison diffs. opentest4j is JVM-only, so JS and
 * Native instead use Strikt's own [AssertionError] subclasses (declared in
 * `strikt.internal.opentest4j` in this source set) which carry the same
 * expected/actual metadata.
 *
 * Stack-trace trimming (removing Strikt's own frames) is intentionally NOT done
 * here: JS `Error.stack` is an opaque string and Kotlin/Native does not allow
 * reassigning a throwable's stack trace, so there is no portable equivalent of
 * the JVM behaviour. Full traces are kept as-is.
 */
internal actual fun createAssertionFailedError(
  message: String,
  comparison: ComparedValues?,
  cause: Throwable?
): Throwable =
  if (comparison != null) {
    AssertionFailed(message, comparison.expected, comparison.actual, cause)
  } else {
    AssertionFailed(message, cause)
  }

internal actual fun createCompoundAssertionFailure(
  message: String,
  errors: List<Throwable>
): Throwable = CompoundAssertionFailure(message, errors)

internal actual fun createIncompleteAssertionError(): Throwable = IncompleteAssertion()

internal actual fun createMappingFailedError(
  description: String,
  cause: Throwable
): Throwable = MappingFailed(description, cause)
