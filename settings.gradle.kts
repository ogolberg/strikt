rootProject.name = "strikt"

include(
  "strikt-bom",
  "strikt-core",
  // The docs site is not a Gradle module: it lives in `site/` as a standalone
  // VitePress (npm) project and is built and deployed by the docs workflow
  // (.github/workflows/docs.yml), which also publishes the aggregated Dokka
  // API docs (`./gradlew :dokkaGenerate`) under /api/.
  "strikt-arrow",
  "strikt-jackson",
  "strikt-jvm",
  "strikt-mockk",
  "strikt-protobuf",
  "strikt-spring"
)

rootProject.children.forEach {
  it.buildFileName = "${it.name}.gradle.kts"
}
