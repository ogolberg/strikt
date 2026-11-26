---
layout: home

hero:
  name: Strikt
  text: An assertion library for Kotlin
  tagline: Intended for use with a test runner such as JUnit, Minutest, Spek, or KotlinTest.
  image:
    src: /logo.png
    alt: Strikt logo
  actions:
    - theme: brand
      text: Get Started
      link: /guide/getting-started
    - theme: alt
      text: API Docs
      link: /api/
      target: _self
    - theme: alt
      text: GitHub
      link: https://github.com/robfletcher/strikt
---

<script setup>
import { useData } from 'vitepress'
const { theme } = useData()
</script>

<p style="text-align: center">
  <a href="/changelog">Version {{ theme.striktVersion }}</a>
</p>

Strikt gives you…

## A powerful fluent API

Type-safe fluent assertions:

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_one

## Collection handling

Flexible assertions about collections:

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_two

“Narrow” the assertion to elements or ranges:

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_three

Make grouping assertions:

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_four

## “Soft” assertions

Use lambdas to execute multiple assertions on a subject at once…

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_five

…with structured diagnostics of those that fail:

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_six{text}

Use lambdas to execute assertions on multiple subjects at once…

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_seven

## Strong typing

Assertion functions can “narrow” the type of the assertion:

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_eight

Assertions can “map” to properties and method results in a type safe way:

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_nine

## Extensibility

Custom assertions are extension functions:

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_ten

Custom mappings are extension properties:

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_eleven_a

<<< ../strikt-core/src/jvmTest/kotlin/strikt/docs/Homepage.kt#homepage_eleven_b
