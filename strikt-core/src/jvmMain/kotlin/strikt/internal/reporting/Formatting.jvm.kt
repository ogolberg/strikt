package strikt.internal.reporting

import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass

internal actual fun formatOther(value: Any): Any =
  when (value) {
    is KClass<*> -> value.java.name
    is Class<*> -> value.name
    is CallableReference -> "${formatValue(value.boundReceiver)}::${value.name}"
    else -> value.toString()
  }

internal actual fun Any.simpleTypeName(): String? = javaClass.kotlin.simpleName

internal actual fun Any.qualifiedTypeName(): String = javaClass.name

internal actual fun Iterable<*>.preferToString(): Boolean =
  javaClass.getMethod("toString").declaringClass == javaClass
