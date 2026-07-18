package strikt.internal

/**
 * Runs [block] to completion, bridging Strikt's `suspend`-friendly entry points
 * (`expect`, `expectThat`, `expectCatching`) into synchronous test code.
 *
 * On the JVM and Native this delegates to `kotlinx.coroutines.runBlocking`.
 * The JS target has no blocking event loop, so there is no `runBlocking` there;
 * the JS `actual` documents the constraint (suspending assertions cannot be run
 * synchronously on JS and need a `suspend`/`Promise`-based API instead).
 */
internal expect fun <T> runBlockingCompat(block: suspend () -> T): T
