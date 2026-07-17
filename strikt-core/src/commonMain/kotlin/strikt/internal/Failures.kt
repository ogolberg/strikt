package strikt.internal

/**
 * Platform-specific factory functions for the exceptions Strikt throws when an
 * assertion fails.
 *
 * On the JVM these delegate to the [org.opentest4j](https://github.com/ota4j-team/opentest4j)
 * exception hierarchy so that test runners (JUnit, etc.) can render rich
 * comparison failures and so the exception types remain part of Strikt's public
 * JVM API. Other platforms do not have opentest4j, so their `actual`
 * implementations provide plain [AssertionError]-based equivalents.
 *
 * These are `expect` declarations rather than an `expect class` hierarchy
 * because the JVM implementations must extend concrete opentest4j classes,
 * which cannot be expressed as `actual` supertypes in common code.
 */

/**
 * Create the exception thrown for a single failed assertion.
 *
 * The JVM implementation additionally trims Strikt's own frames from the stack
 * trace so the reported failure points at the caller's assertion. Non-JVM
 * platforms cannot rewrite stack traces and simply return the error as-is.
 *
 * @param message the fully-rendered, human-readable failure report.
 * @param comparison the expected/actual values, if this failure came from a
 * comparison assertion; used by test runners to show a diff.
 * @param cause the underlying cause, if any.
 */
internal expect fun createAssertionFailedError(
  message: String,
  comparison: ComparedValues?,
  cause: Throwable?
): Throwable

/**
 * Create the exception thrown when a group of assertions (an `expectThat`
 * block, or multiple `expect` subjects) contains one or more failures.
 *
 * @param message the fully-rendered report for the whole group.
 * @param errors one exception per failing leaf assertion.
 */
internal expect fun createCompoundAssertionFailure(
  message: String,
  errors: List<Throwable>
): Throwable

/**
 * Create the exception thrown when an assertion chain is left incomplete (an
 * empty block, or a mapping that was never terminated with an assertion).
 */
internal expect fun createIncompleteAssertionError(): Throwable

/**
 * Create the exception thrown when a mapping function (e.g. [strikt.api.Assertion.Builder.get]
 * or [strikt.api.Assertion.Builder.with]) throws.
 *
 * @param description the description of the mapping that failed.
 * @param cause the exception thrown by the mapping function.
 */
internal expect fun createMappingFailedError(
  description: String,
  cause: Throwable
): Throwable
