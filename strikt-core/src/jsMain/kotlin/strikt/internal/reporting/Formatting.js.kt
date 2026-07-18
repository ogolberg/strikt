package strikt.internal.reporting

/**
 * Kotlin/JS reflection does not expose `KClass.qualifiedName`, so throwables can
 * only be rendered by their simple type name (unlike the JVM's fully-qualified
 * name).
 */
internal actual fun Any.qualifiedTypeName(): String =
  this::class.simpleName ?: "<unknown>"
