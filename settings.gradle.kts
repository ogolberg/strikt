rootProject.name = "strikt"

include(
  "strikt-bom",
  "strikt-core",
  // "site" is intentionally excluded: it uses the abandoned Orchid Gradle
  // plugin (0.21.1), which cannot configure under Gradle 9 (it sets a removed
  // task property and fails the whole build). The docs site needs to be
  // migrated off Orchid before it can be part of a Gradle 9 build again.
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
