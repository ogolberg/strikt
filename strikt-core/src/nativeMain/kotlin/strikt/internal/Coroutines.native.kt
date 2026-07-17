package strikt.internal

import kotlinx.coroutines.runBlocking

/**
 * Native implementation of the suspend bridge.
 *
 * kotlinx-coroutines publishes `runBlocking` for Kotlin/Native, so this can use
 * it directly. NOTE(native): `runBlocking` on native must run on a thread with
 * no existing event loop; this should be fine for the synchronous
 * `expectThat`/`expectCatching` entry points but is worth revisiting if Strikt
 * ever needs to interoperate with a caller-supplied dispatcher.
 */
internal actual fun <T> runBlockingCompat(block: suspend () -> T): T = runBlocking { block() }
