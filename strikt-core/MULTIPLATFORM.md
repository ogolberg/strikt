# strikt-core multiplatform

`strikt-core` is a Kotlin Multiplatform (KMP) module. The assertion API and all
assertion logic live in `commonMain` and compile for every target; the small set
of things that genuinely need a platform (exception types, source-aware lambda
descriptions, `printf`-style formatting, `runBlocking`, and a little reflection)
are isolated behind `expect`/`actual` declarations and **fully implemented on
every target**.

## Targets

Configured in `strikt-core.gradle.kts`:

- **jvm** — behaviourally identical to the previous single-target module (the
  entire original test suite passes unchanged, and the sibling `strikt-*`
  modules consume the JVM variant transparently).
- **js** (IR, nodejs + browser) — compiles and its tests pass.
- **macosArm64**, **linuxX64** — representative native targets. `linuxX64`
  compiles and links; running native *tests* additionally needs a native
  toolchain (on macOS the KGP native test tasks require Xcode).

## Source-set layout

```
src/
  commonMain/kotlin   ← the whole API + assertion logic + expect declarations
  jvmMain/kotlin      ← JVM actuals (opentest4j, filepeek, reflection, runBlocking)
  nonJvmMain/kotlin   ← actuals shared by JS + Native (formatter, failure types,
                        reflection-free helpers, callable-ref descriptions)
  jsMain/kotlin       ← JS-only actuals (synchronous coroutine runner, simpleName
                        type rendering)
  nativeMain/kotlin   ← Native-only actuals (runBlocking, qualifiedName rendering)
  commonTest/kotlin   ← kotlin.test assertions that run on every target
  jvmTest/kotlin      ← the original JVM test suite (JUnit 5 / failgood / minutest)
  jvmTest/java        ← Java test fixtures (PersonJava)
```

The source-set hierarchy is `commonMain ← nonJvmMain ← {jsMain, nativeMain}`, so
JS and Native share one implementation of each seam and only override the few
things that truly differ between them.

## The expect/actual seams

Each `expect` lives in `commonMain`. The JVM `actual` reproduces the original
behaviour exactly; the non-JVM `actual`s are real implementations.

| Concern | `expect` (commonMain) | JVM `actual` | Non-JVM `actual` |
| --- | --- | --- | --- |
| Failure exceptions | `strikt/internal/Failures.kt` | wraps `org.opentest4j` + trims Strikt frames from the stack trace | `AssertionError` subclasses (`strikt.internal.opentest4j.*` in `nonJvmMain`) carrying the same expected/actual metadata; no stack-trace trimming (not portable) |
| `String.format` rendering | `strikt/internal/reporting/Platform.kt` (`formatDescription`) | JDK `Formatter` via `String.format` | hand-written positional `printf` in `nonJvmMain` (`%s`, `%d`, `%x`/`%o`/`%b`/`%c`/`%f`, flags, width, precision, `%%`) |
| Line separator | `Platform.kt` (`EOL`) | `System.getProperty("line.separator")` | `"\n"` |
| Value formatting reflection | `Formatting.kt` (`formatOther`, `simpleTypeName`, `qualifiedTypeName`, `preferToString`) | `java.lang.Class` / `KClass.java` / `CallableReference` reflection | `KClass.simpleName` (shared); `qualifiedTypeName` uses `KClass.qualifiedName` on Native and `simpleName` on JS (JS has no `qualifiedName`); `preferToString` = `false` |
| Lambda mapping descriptions | `strikt/api/Assertion.kt` (`describe`) | callable-reference reflection + `filepeek` source decompilation | callable references described via `KProperty`/`KFunction` reflection; plain lambdas fall back to `"%s"` (no filepeek off-JVM — same as the JVM when its source lookup fails) |
| `runBlocking` bridge | `strikt/internal/Coroutines.kt` (`runBlockingCompat`) | `kotlinx.coroutines.runBlocking` | Native: `runBlocking`; JS: `startCoroutine` that reads the result synchronously (JS has no `runBlocking`), throwing a clear error if a block genuinely suspends |

### JS `runBlocking` note

Kotlin/JS has no `runBlocking`. Strikt only ever passes *synchronous* work to its
`expect*` entry points (assertion blocks that call `pass`/`fail` directly and
never actually suspend), so `Coroutines.js.kt` runs the block with
`startCoroutine` and reads the result the moment it completes. A block that truly
suspends (a real `delay`, an awaited network call) throws a clear error rather
than returning a wrong result; such callers would need a `suspend`/`Promise`-based
API on JS.

## Common-code changes made for portability

- `List.containsSequence` uses a small multiplatform `indexOfSubList` instead of
  `java.util.Collections.indexOfSubList`.
- `Iterable.filterIsInstance` uses the reified common-stdlib overload.
- `isSorted()` uses `naturalOrder()` instead of `Comparator.naturalOrder()`.
- `isA<T>()` passes `T::class` (a `KClass`) instead of `T::class.java`; a type
  token renders without a redundant `(Class)`/`(KClass)` suffix.
- `ResultWriter` builds strings with `StringBuilder` instead of `StringWriter`.
- `@JvmName` usages import `kotlin.jvm.JvmName` (a no-op annotation off-JVM).
- `formatValue` matches `Int`/`Long`/`Short` before `Byte`. On Kotlin/JS `is Byte`
  is a range check, so without this a small `Int` (e.g. `5`) would match the
  `Byte` branch and render as hex; the reorder keeps decimals on every platform.

## Build/toolchain notes

- Kotlin `2.4.21`, coroutines `1.11.0`, Gradle `9.8.1`. Language version is
  pinned to `2.2` (`languageVersion`) for stability. Dependency and plugin
  versions live in the `gradle/libs.versions.toml` version catalog.
- `filepeek` reconstructs a `.kt` path from the compiled class's output dir. KMP
  emits jvm test classes to `build/classes/kotlin/jvm/test` (extra `jvm`
  segment) which filepeek 0.1.3 doesn't recognise, so `compileTestKotlinJvm`'s
  output is redirected to `build/classes/kotlin/test` and the multiplatform
  source roots are registered in `strikt/internal/FilePeek.kt`.
- `allWarningsAsErrors` is disabled for the `*MainKotlinMetadata` compilations
  only: with an intermediate source set (`nonJvmMain`) the Kotlin 2.4 metadata
  compiler emits a benign duplicate-`kotlin-stdlib-common` KLIB warning that
  would otherwise trip `-Werror`.
- Under Gradle 9 the JUnit Platform launcher is no longer added to the test
  runtime classpath automatically; it is declared explicitly
  (`libs.junit.platform.launcher`).
- Requires a JDK the Kotlin compiler can parse (e.g. 17); JDK 25 is not
  parseable by this compiler version.
- Running native *tests* on macOS requires Xcode (the KGP native test tasks
  invoke `xcrun`); if `xcode-select -p` points at the command-line tools, pass
  `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer`. Compilation/linking
  of `linuxX64` works without it.
- The `site` module (Orchid docs) is excluded from the build: the abandoned
  Orchid Gradle plugin cannot configure under Gradle 9. It needs migrating off
  Orchid before it can rejoin the build.
