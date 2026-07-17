package strikt.internal.reporting

import strikt.internal.ComparedValues
import kotlin.reflect.KClass

internal fun ComparedValues.formatValues(): ComparedValues {
  val e = formatValue(expected)
  val a = formatValue(actual)
  return if (e.toString() == a.toString()) {
    ComparedValues(e.withTypeSuffix(expected), a.withTypeSuffix(actual))
  } else {
    ComparedValues(e, a)
  }
}

private fun Any.withTypeSuffix(typeOf: Any?) =
  when (typeOf) {
    null -> this
    // A type token (e.g. the class captured by `isA`) already renders as a type
    // name; a "(type of the type)" suffix would just be noise like "(KClass)".
    is KClass<*> -> this
    else -> "$this (${typeOf.simpleTypeName()})"
  }

internal fun formatValue(value: Any?): Any =
  when (value) {
    null -> "null"
    is CharSequence -> "\"$value\""
    is Char -> "'$value'"
    is Iterable<*> -> if (value.preferToString()) value.toString() else value.map(::formatValue)
    // These must precede the `is Byte` case: on Kotlin/JS `is Byte` is a range
    // check, so a small `Int`/`Short`/`Long` (e.g. 5) would otherwise match it
    // and render as hex. Matching the wider integer types first keeps ordinary
    // numbers rendering as decimals on every platform. (A genuine `Byte` still
    // renders as hex on the JVM; on JS a `Byte` and an equal `Int` are the same
    // runtime number and both render as decimal.)
    is Int -> value
    is Long -> value
    is Short -> value
    is Byte -> "0x${value.toString(16)}"
    is ByteArray -> "0x${value.toHex()}".truncate()
    is CharArray -> formatValue(value.toList())
    is ShortArray -> formatValue(value.toList())
    is IntArray -> formatValue(value.toList())
    is LongArray -> formatValue(value.toList())
    is FloatArray -> formatValue(value.toList())
    is DoubleArray -> formatValue(value.toList())
    is Array<*> -> formatValue(value.toList())
    is Regex -> "/${value.pattern}/"
    is Throwable -> value.qualifiedTypeName()
    is Pair<*, *> -> "{${formatValue(value.first)}: ${formatValue(value.second)}}"
    is Map<*, *> -> value.map { (k, v) -> formatValue(k) to formatValue(v) }.toMap()
    is Number -> value
    else -> formatOther(value)
  }

/**
 * Renders a value that isn't one of the cases handled by [formatValue].
 *
 * On the JVM this handles `java.lang.Class` instances (rendered as their name)
 * and Kotlin callable references (rendered as `receiver::name`) via reflection,
 * falling back to `toString()`. Other platforms fall back to `toString()` only
 * (see the JS/Native `actual`s for what still needs implementing).
 */
internal expect fun formatOther(value: Any): Any

/**
 * The simple name of [this] value's runtime type (e.g. `"String"`), used to
 * disambiguate two values whose `toString()` renders identically.
 *
 * On the JVM this uses `javaClass.kotlin.simpleName`. Other platforms need a
 * reflection-free equivalent.
 */
internal expect fun Any.simpleTypeName(): String?

/**
 * The fully-qualified name of [this] value's runtime type (e.g.
 * `"java.lang.IllegalStateException"`), used when rendering [Throwable]s.
 */
internal expect fun Any.qualifiedTypeName(): String

/**
 * Whether [this] iterable's runtime type declares its own `toString()` (in
 * which case we render that rather than expanding the elements).
 *
 * On the JVM this is determined by reflection over the declaring class of the
 * `toString` method. Other platforms need an equivalent heuristic.
 */
internal expect fun Iterable<*>.preferToString(): Boolean

private val hexArray = "0123456789ABCDEF".toCharArray()

internal fun ByteArray.toHex(): String {
  val hexChars = CharArray(size * 2)
  for (j in indices) {
    val v: Int = this[j].toInt() and 0xFF
    hexChars[j * 2] = hexArray[v ushr 4]
    hexChars[j * 2 + 1] = hexArray[v and 0x0F]
  }
  return hexChars.concatToString()
}

internal const val FORMATTED_VALUE_MAX_LENGTH = 40

private fun CharSequence.truncate(maxLength: Int = FORMATTED_VALUE_MAX_LENGTH) =
  when (length) {
    in 0..maxLength -> this
    else -> substring(0, maxLength) + "…"
  }
