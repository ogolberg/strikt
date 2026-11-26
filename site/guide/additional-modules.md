# Additional Modules

In addition to the core functionality provided by the [strikt-core](/api/) module, Strikt has the following optional modules:

- **strikt-arrow** — Extensions for assertions and traversals on types from the Arrow functional programming library.
- **strikt-jackson** — Extensions for assertions and traversals on Jackson's `JsonNode` and sub-types.
- **strikt-jvm** — Extensions for assertions and traversals on types from the Java standard library.
- **strikt-mockk** — Extensions for assertions and traversals on types from the MockK mocking and verification library.
- **strikt-protobuf** — Extensions for testing code that uses Protobuf / gRPC.
- **strikt-spring** — Extensions for testing code that uses the Spring Framework.

Each module is published to Maven Central under the `io.strikt` group with the same version as `strikt-core`.
For example:

<script setup>
import { useData } from 'vitepress'
const { theme } = useData()
</script>

```kotlin-vue
dependencies {
  testImplementation("io.strikt:strikt-jvm:{{ theme.striktVersion }}")
}
```

## Bill of materials

Strikt also publishes a `strikt-bom` bill of materials that aligns the versions of all the modules:

```kotlin-vue
dependencies {
  testImplementation(platform("io.strikt:strikt-bom:{{ theme.striktVersion }}"))
  testImplementation("io.strikt:strikt-core")
  testImplementation("io.strikt:strikt-jvm")
}
```

See the [API docs](/api/) for details of the assertions each module provides.
