package strikt.internal.reporting

/**
 * Line separator for non-JVM targets. Neither JS nor Kotlin/Native exposes a
 * platform line-separator property; `"\n"` is correct for browsers, Node, and
 * POSIX. (A Windows/mingw native target could override this in a narrower
 * source set if `\r\n` is ever required.)
 */
internal actual val EOL: String = "\n"

/**
 * A `String.format`-style substitution covering the conversions Strikt uses in
 * assertion descriptions (`%s`, `%d`) plus the common flags/width/precision
 * syntax, so user-supplied descriptions behave the same off-JVM.
 *
 * Grammar handled per directive: `%[flags][width][.precision]conversion`
 *   - flags: `-` (left-justify), `0` (zero-pad), `+`, space, `,` (grouping)
 *   - conversions: `s`/`S`, `d`, `x`/`X`, `o`, `b`/`B`, `c`, `f`, `%`, `n`
 *
 * Arguments are consumed positionally in order (Strikt never uses argument
 * indexes like `%1$s`). Anything not recognised is emitted verbatim so a stray
 * `%` in a description is harmless rather than throwing.
 */
internal actual fun String.formatDescription(vararg args: Any?): String {
  val out = StringBuilder(length + 16)
  var argIndex = 0
  var i = 0
  while (i < length) {
    val c = this[i]
    if (c != '%') {
      out.append(c)
      i++
      continue
    }

    // Parse a directive: %[flags][width][.precision]conversion
    val start = i
    i++ // consume '%'
    if (i >= length) {
      out.append('%')
      break
    }

    // flags
    var leftJustify = false
    var zeroPad = false
    var plusSign = false
    var spaceSign = false
    var grouping = false
    loop@ while (i < length) {
      when (this[i]) {
        '-' -> leftJustify = true
        '0' -> zeroPad = true
        '+' -> plusSign = true
        ' ' -> spaceSign = true
        ',' -> grouping = true
        else -> break@loop
      }
      i++
    }

    // width
    var width = 0
    var hasWidth = false
    while (i < length && this[i].isDigit()) {
      hasWidth = true
      width = width * 10 + (this[i] - '0')
      i++
    }

    // precision
    var precision = -1
    if (i < length && this[i] == '.') {
      i++
      precision = 0
      while (i < length && this[i].isDigit()) {
        precision = precision * 10 + (this[i] - '0')
        i++
      }
    }

    if (i >= length) {
      // Malformed trailing directive; emit verbatim.
      out.append(this, start, length)
      break
    }

    val conversion = this[i]
    i++

    if (conversion == '%') {
      out.append('%')
      continue
    }
    if (conversion == 'n') {
      out.append(EOL)
      continue
    }

    val arg = if (argIndex < args.size) args[argIndex++] else null

    var body: String =
      when (conversion) {
        's', 'S' -> {
          var s = arg.toString()
          if (precision >= 0 && s.length > precision) s = s.substring(0, precision)
          if (conversion == 'S') s = s.uppercase()
          s
        }
        'd' -> formatInteger(arg, radix = 10, grouping = grouping, plusSign = plusSign, spaceSign = spaceSign)
        'x' -> formatInteger(arg, radix = 16, grouping = false, plusSign = false, spaceSign = false)
        'X' -> formatInteger(arg, radix = 16, grouping = false, plusSign = false, spaceSign = false).uppercase()
        'o' -> formatInteger(arg, radix = 8, grouping = false, plusSign = false, spaceSign = false)
        'b', 'B' -> {
          val b = when (arg) {
            null -> false
            is Boolean -> arg
            else -> true
          }.toString()
          if (conversion == 'B') b.uppercase() else b
        }
        'c' -> when (arg) {
          is Char -> arg.toString()
          is Number -> arg.toInt().toChar().toString()
          else -> arg.toString()
        }
        'f' -> formatFloat(arg, if (precision >= 0) precision else 6, plusSign, spaceSign)
        else -> {
          // Unknown conversion: emit the directive verbatim and don't consume
          // the argument we speculatively took.
          if (arg != null || argIndex > 0) argIndex--
          out.append(this, start, i)
          continue
        }
      }

    // Apply width padding.
    if (hasWidth && body.length < width) {
      val pad = width - body.length
      body = when {
        leftJustify -> body + " ".repeat(pad)
        zeroPad && conversion in "dxXof" -> {
          // Zero-pad after any leading sign.
          val signLen = if (body.isNotEmpty() && (body[0] == '-' || body[0] == '+' || body[0] == ' ')) 1 else 0
          body.substring(0, signLen) + "0".repeat(pad) + body.substring(signLen)
        }
        else -> " ".repeat(pad) + body
      }
    }

    out.append(body)
  }
  return out.toString()
}

private fun formatInteger(
  arg: Any?,
  radix: Int,
  grouping: Boolean,
  plusSign: Boolean,
  spaceSign: Boolean
): String {
  val value: Long =
    when (arg) {
      null -> 0L
      is Long -> arg
      is Int -> arg.toLong()
      is Short -> arg.toLong()
      is Byte -> arg.toLong()
      is Number -> arg.toLong()
      else -> return arg.toString()
    }
  val negative = value < 0
  var digits = if (radix == 10) {
    // Use kotlin.math.abs-free handling of Long.MIN_VALUE via toString.
    if (negative) value.toString().substring(1) else value.toString()
  } else {
    value.toString(radix).let { if (negative) it.substring(1) else it }
  }
  if (grouping && radix == 10) {
    digits = groupThousands(digits)
  }
  val prefix = when {
    negative -> "-"
    plusSign -> "+"
    spaceSign -> " "
    else -> ""
  }
  return prefix + digits
}

private fun groupThousands(digits: String): String {
  if (digits.length <= 3) return digits
  val sb = StringBuilder()
  val firstGroup = digits.length % 3
  var idx = 0
  if (firstGroup > 0) {
    sb.append(digits, 0, firstGroup)
    idx = firstGroup
  }
  while (idx < digits.length) {
    if (sb.isNotEmpty()) sb.append(',')
    sb.append(digits, idx, idx + 3)
    idx += 3
  }
  return sb.toString()
}

private fun formatFloat(
  arg: Any?,
  precision: Int,
  plusSign: Boolean,
  spaceSign: Boolean
): String {
  val value: Double =
    when (arg) {
      null -> 0.0
      is Number -> arg.toDouble()
      else -> return arg.toString()
    }
  val negative = value < 0 || (value == 0.0 && 1.0 / value < 0)
  val magnitude = if (negative) -value else value
  val rounded = roundToDecimals(magnitude, precision)
  val prefix = when {
    negative -> "-"
    plusSign -> "+"
    spaceSign -> " "
    else -> ""
  }
  return prefix + rounded
}

private fun roundToDecimals(value: Double, decimals: Int): String {
  if (value.isNaN()) return "NaN"
  if (value.isInfinite()) return "Infinity"
  var factor = 1.0
  repeat(decimals) { factor *= 10 }
  val scaled = kotlin.math.round(value * factor)
  val whole = (scaled / factor)
  if (decimals == 0) return whole.toLong().toString()
  // Build fixed-decimal representation without relying on JVM formatting.
  val total = scaled.toLong()
  val intPart = total / factor.toLong()
  val fracPart = total % factor.toLong()
  val fracStr = fracPart.toString().padStart(decimals, '0')
  return "$intPart.$fracStr"
}
