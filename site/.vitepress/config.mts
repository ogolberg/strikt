import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitepress'

// The site version is the most recent release in the changelog (or the
// STRIKT_VERSION environment variable, which CI sets from the release tag —
// hence the stripping of the tag's leading "v").
function striktVersion(): string {
  if (process.env.STRIKT_VERSION) {
    return process.env.STRIKT_VERSION.replace(/^v/, '')
  }
  const changelog = readFileSync(
    fileURLToPath(new URL('../../CHANGELOG.md', import.meta.url)),
    'utf-8'
  )
  const match = changelog.match(/^## (\S+)/m)
  if (!match) {
    throw new Error('Unable to determine the latest version from CHANGELOG.md')
  }
  return match[1]
}

export default defineConfig({
  title: 'Strikt',
  description:
    'Strikt is an assertion library for Kotlin intended for use with a test runner such as JUnit, Minutest, Spek, or KotlinTest.',

  head: [['link', { rel: 'icon', href: '/favicon.ico' }]],

  // API docs are generated separately by Dokka and deployed under /api/, so
  // links to them cannot be resolved by VitePress at build time.
  ignoreDeadLinks: [/^\/api\//],

  themeConfig: {
    logo: '/logo.png',

    // Available to pages via useData().theme (see guide/getting-started.md).
    striktVersion: striktVersion(),

    nav: [
      { text: 'Home', link: '/' },
      { text: 'User Guide', link: '/guide/getting-started' },
      { text: 'API', link: '/api/', target: '_self' },
      { text: 'Changelog', link: '/changelog' },
      { text: 'About', link: '/about' }
    ],

    sidebar: {
      '/guide/': [
        {
          text: 'User Guide',
          items: [
            { text: 'Getting Started', link: '/guide/getting-started' },
            { text: 'Assertion Styles', link: '/guide/assertion-styles' },
            {
              text: 'Collection Element Assertions',
              link: '/guide/collection-elements'
            },
            {
              text: 'Expecting Exceptions',
              link: '/guide/expecting-exceptions'
            },
            { text: 'Strongly Typed Assertions', link: '/guide/flow-typing' },
            {
              text: 'Traversing Assertion Subjects',
              link: '/guide/traversing-subjects'
            },
            {
              text: 'Grouping Assertions With and or with',
              link: '/guide/grouping-with-and'
            },
            { text: 'Custom Assertions', link: '/guide/custom-assertions' },
            { text: 'Additional Modules', link: '/guide/additional-modules' }
          ]
        }
      ]
    },

    search: {
      provider: 'local'
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/robfletcher/strikt' },
      {
        icon: 'slack',
        link: 'https://kotlinlang.slack.com/messages/CAR7KJ96J'
      },
      { icon: 'twitter', link: 'https://twitter.com/stri_kt' }
    ],

    footer: {
      message: 'Licensed under the Apache License, Version 2.0',
      copyright: 'Copyright © Rob Fletcher'
    }
  }
})
