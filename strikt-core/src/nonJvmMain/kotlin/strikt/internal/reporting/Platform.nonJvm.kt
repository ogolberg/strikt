package strikt.internal.reporting

/**
 * Line separator for non-JVM targets. Neither JS nor Kotlin/Native exposes a
 * platform line-separator property; `"\n"` is correct for browsers, Node, and
 * POSIX. (A Windows/mingw native target could override this in a narrower
 * source set if `\r\n` is ever required.)
 */
internal actual val EOL: String = "\n"
