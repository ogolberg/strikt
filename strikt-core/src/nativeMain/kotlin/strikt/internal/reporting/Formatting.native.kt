package strikt.internal.reporting

/**
 * Kotlin/Native exposes `KClass.qualifiedName`, so throwables can be rendered
 * with their fully-qualified type name (closest to the JVM's `javaClass.name`).
 * It may be null for local/anonymous classes, in which case we fall back to the
 * simple name.
 */
internal actual fun Any.qualifiedTypeName(): String =
  this::class.qualifiedName ?: this::class.simpleName ?: "<unknown>"
