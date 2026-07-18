package strikt.internal

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/**
 * JS implementation of the suspend bridge.
 *
 * Kotlin/JS has no `runBlocking` — the single-threaded JS event loop cannot
 * block a stack frame waiting for a coroutine to resume on a later tick. Strikt
 * only ever passes *synchronous* work to its `expect*` entry points (assertion
 * blocks that call `pass`/`fail` directly and never actually suspend), so we run
 * the block with [startCoroutine] and read back the result the instant it
 * completes.
 *
 * If a caller passes a block that genuinely suspends (e.g. a real `delay` or an
 * awaited network call), the coroutine will not have finished by the time
 * control returns here and we throw a clear error rather than silently returning
 * a wrong/empty result. Such callers must use a `suspend`/`Promise`-based API on
 * JS instead.
 */
internal actual fun <T> runBlockingCompat(block: suspend () -> T): T {
  var completed: Result<T>? = null
  block.startCoroutine(
    object : Continuation<T> {
      override val context: CoroutineContext = EmptyCoroutineContext

      // Parameter name must match the supertype ('result') to avoid a warning.
      override fun resumeWith(result: Result<T>) {
        completed = result
      }
    }
  )
  return (
    completed
      ?: throw IllegalStateException(
        "A Strikt assertion block suspended on Kotlin/JS, where blocking is not " +
          "possible. Use only synchronous assertions in expect { } / expectCatching { } " +
          "on JS, or adopt a suspend/Promise-based API."
      )
  ).getOrThrow()
}
