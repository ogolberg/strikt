package strikt.internal.reporting

/**
 * The platform line separator (`\n` on most platforms, `\r\n` on Windows JVMs).
 *
 * On the JVM this reads `System.getProperty("line.separator")`; other platforms
 * hard-code `"\n"`.
 */
internal expect val EOL: String

/**
 * Substitutes [args] into the `%s` / `%d` placeholders of this description
 * template.
 *
 * Strikt assertion descriptions such as `"has size %d"` or `"is equal to %s"`
 * are rendered through this function, consuming one pre-formatted value per
 * placeholder in order. `%%` renders a literal `%`. Anything else — an unknown
 * directive, a trailing `%`, or a placeholder with no argument left — is
 * emitted verbatim, so a stray `%` in a user-supplied description is harmless
 * rather than an error.
 *
 * This is the whole placeholder grammar Strikt supports; it is deliberately not
 * a full `printf`. Values are pre-rendered by
 * [strikt.internal.reporting.formatValue] before substitution, so `%s` and `%d`
 * behave identically on every platform.
 */
internal fun String.formatDescription(vararg args: Any?): String {
  val out = StringBuilder(length + 16)
  var argIndex = 0
  var i = 0
  while (i < length) {
    val c = this[i]
    if (c == '%' && i + 1 < length) {
      when (val directive = this[i + 1]) {
        's', 'd' -> {
          if (argIndex < args.size) {
            out.append(args[argIndex++])
          } else {
            out.append('%').append(directive)
          }
          i += 2
          continue
        }
        '%' -> {
          out.append('%')
          i += 2
          continue
        }
      }
    }
    out.append(c)
    i++
  }
  return out.toString()
}
