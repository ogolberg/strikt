package strikt.api

import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * Non-JVM mapping-description derivation.
 *
 * The JVM version additionally decompiles plain `get { ... }` lambdas from source
 * via the `filepeek` library (which reads the `.kt` file off disk using the JVM
 * stack trace). There is no filepeek equivalent off-JVM, so plain lambdas fall
 * back to the `"%s"` placeholder — exactly what the JVM itself does when its
 * source lookup fails.
 *
 * Callable references (property and function references) DO carry usable
 * metadata on JS and Native, so those are described the same way as on the JVM
 * (`"value of property foo"` / `"return value of foo"`).
 */
internal actual fun <Receiver, Result> (Receiver.() -> Result).describe(): String =
  when (this) {
    is KProperty<*> -> "value of property $name"
    is KFunction<*> -> "return value of $name"
    else -> "%s"
  }
