package strikt.internal

import filepeek.FilePeek

internal object FilePeek {
  val filePeek by lazy {
    FilePeek(
      listOf(
        "strikt.internal",
        "strikt.api",
        "filepeek"
      ),
      // filepeek's default source roots are the single-target `src/{test,main}/{kotlin,java}`
      // layout. Since strikt-core became a multiplatform module its sources live under
      // per-source-set directories (`src/commonMain/kotlin`, `src/jvmMain/kotlin`,
      // `src/jvmTest/kotlin`, ...), so those must be added for lambda-body decompilation
      // (used to auto-describe `get { ... }` mappings) to find the source file.
      sourceRoots.flatMap { root ->
        listOf("src/$root/kotlin", "src/$root/java")
      }
    )
  }

  private val sourceRoots =
    listOf(
      "commonMain",
      "jvmMain",
      "commonTest",
      "jvmTest"
    )
}
