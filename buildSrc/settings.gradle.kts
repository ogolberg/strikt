// Make the root version catalog available to buildSrc so the convention
// plugins and buildSrc's own dependencies can reference the same versions.
dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
