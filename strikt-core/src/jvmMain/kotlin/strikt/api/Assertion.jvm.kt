package strikt.api

import filepeek.LambdaBody
import strikt.internal.FilePeek
import java.util.Locale
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

internal actual fun <Receiver, Result> (Receiver.() -> Result).describe(): String =
  when (this) {
    is KProperty<*> ->
      "value of property $name"
    is KFunction<*> ->
      "return value of $name"
    is CallableReference -> "value of $propertyName"
    else -> {
      try {
        val line = FilePeek.filePeek.getCallerFileInfo().line
        LambdaBody("get", line).body.trim()
      } catch (e: Exception) {
        "%s"
      }
    }
  }

private val CallableReference.propertyName: String
  get() =
    "^get(.+)$".toRegex().find(name).let { match ->
      return when (match) {
        null -> name
        else -> match.groupValues[1].replaceFirstChar { it.lowercase(Locale.getDefault()) }
      }
    }
