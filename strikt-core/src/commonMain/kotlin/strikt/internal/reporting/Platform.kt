package strikt.internal.reporting

/**
 * The platform line separator (`\n` on most platforms, `\r\n` on Windows JVMs).
 *
 * On the JVM this reads `System.getProperty("line.separator")`; other platforms
 * hard-code `"\n"`.
 */
internal expect val EOL: String

/**
 * Substitutes [args] into a `String.format`-style template ([this]) containing
 * `%s`/`%d`/etc. placeholders.
 *
 * Strikt assertion descriptions such as `"has size %d"` or `"is equal to %s"`
 * are rendered through this function. On the JVM it delegates to the JDK's
 * [java.util.Formatter] (via `kotlin.text.String.format`); other platforms need
 * a multiplatform `printf`-style implementation (see the JS/Native `actual`s).
 */
internal expect fun String.formatDescription(vararg args: Any?): String
