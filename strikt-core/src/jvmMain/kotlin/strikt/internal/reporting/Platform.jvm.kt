package strikt.internal.reporting

internal actual val EOL: String = System.getProperty("line.separator")

internal actual fun String.formatDescription(vararg args: Any?): String = format(*args)
