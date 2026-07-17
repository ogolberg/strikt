package strikt.internal.reporting

import kotlin.reflect.KClass

/**
 * Value-formatting helpers shared by non-JVM targets.
 *
 * The JVM version renders `java.lang.Class` instances and Kotlin callable
 * references specially via full reflection. Off-JVM there is no `java.lang.Class`
 * and no portable way to introspect a callable reference's bound receiver, so we
 * render [KClass] values by name and fall back to `toString()` for everything
 * else.
 */
internal actual fun formatOther(value: Any): Any =
  when (value) {
    is KClass<*> -> value.simpleName ?: value.toString()
    else -> value.toString()
  }

internal actual fun Any.simpleTypeName(): String? = this::class.simpleName

/**
 * The JVM version checks (via reflection) whether an iterable's runtime class
 * declares its own `toString`. Off-JVM we cannot inspect declared methods, so we
 * conservatively return `false` and always expand collections element-by-element
 * (which produces a more useful diff than an opaque `toString` anyway).
 */
internal actual fun Iterable<*>.preferToString(): Boolean = false
