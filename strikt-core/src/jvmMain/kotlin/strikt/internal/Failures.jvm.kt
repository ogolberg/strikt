package strikt.internal

import org.opentest4j.AssertionFailedError
import strikt.internal.opentest4j.AssertionFailed
import strikt.internal.opentest4j.CompoundAssertionFailure
import strikt.internal.opentest4j.IncompleteAssertion
import strikt.internal.opentest4j.MappingFailed

internal actual fun createAssertionFailedError(
  message: String,
  comparison: ComparedValues?,
  cause: Throwable?
): Throwable {
  val error =
    if (comparison != null) {
      AssertionFailed(
        message,
        comparison.expected,
        comparison.actual,
        cause
      )
    } else {
      AssertionFailed(
        message,
        cause
      )
    }

  // Trim Strikt's own frames from the stack trace so the reported failure
  // points at the caller's assertion, and stash the removed frames on a
  // suppressed exception for debugging.
  val stackTrace = error.stackTrace
  val lastIndex =
    stackTrace
      .indexOfLast { it.className.startsWith("strikt") }
  val suppressedElements = stackTrace.copyOfRange(0, lastIndex)
  val remainingElements = stackTrace.copyOfRange(lastIndex + 1, stackTrace.lastIndex)
  error.stackTrace = remainingElements
  val striktError = AssertionFailedError()
  striktError.stackTrace = suppressedElements
  error.addSuppressed(striktError)
  return error
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
