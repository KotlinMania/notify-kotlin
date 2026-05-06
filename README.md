# notify-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fnotify--kotlin-blue.svg)](https://github.com/KotlinMania/notify-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/notify-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/notify-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/notify-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/notify-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`notify-rs/notify`](https://github.com/notify-rs/notify.git).

**Original Project:** This port is based on [`notify-rs/notify`](https://github.com/notify-rs/notify.git). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `notify-rs/notify`

> The text below is reproduced and lightly edited from [`https://github.com/notify-rs/notify.git`](https://github.com/notify-rs/notify.git). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## Notify

[![» Crate](https://flat.badgen.net/crates/v/notify)][crate]
[![» Docs](https://flat.badgen.net/badge/api/docs.rs/df3600)][notify-docs]
[![» CI](https://flat.badgen.net/github/checks/notify-rs/notify/main)][build]
[![» Downloads](https://flat.badgen.net/crates/d/notify)][crate]
[![» Conduct](https://flat.badgen.net/badge/contributor/covenant/5e0d73)][coc]
[![» Public Domain](https://flat.badgen.net/badge/license/CC0-1.0/purple)][cc0]

_Cross-platform filesystem notification library for Rust._


- [Notify Documentation][notify-docs]
- [Notify Types Documentation][notify-types-docs]
- [Mini Debouncer Documentation][debouncer-mini-docs]
- [Full Debouncer Documentation][debouncer-full-docs]
- [File ID][file-id-docs]
- [Examples][examples]
- [Changelog][changelog]
- [Upgrading notify from v8](https://github.com/notify-rs/notify/blob/HEAD/docs/UPGRADING_V8_TO_V9.md)
- [Upgrading notify from v4](https://github.com/notify-rs/notify/blob/HEAD/docs/UPGRADING_V4_TO_V5.md)
- Minimum supported Rust version: **1.88**

As used by: [alacritty], [cargo watch], [cobalt], [deno], [docket], [mdBook],
[rust-analyzer], [watchexec], [watchfiles], [xi-editor], [zed]
and others.

(Looking for desktop notifications instead? Have a look at [notify-rust] or
[alert-after]!)

## Minimum Supported Rust Version (MSRV) Policy

We follow these MSRV rules:

- The current MSRV is **1.88**.
- MSRV bumps do NOT require a major release and may happen in minor releases.
- The MSRV may be updated when needed, but support for the current stable Rust release and the previous two stable releases (N, N-1, N-2) is always guaranteed.
  - For example, if the current stable version is 1.85, we guarantee support for 1.85, 1.84, and 1.83, so the minimum supported Rust version will be **at most** 1.83.
- MSRV is bumped only when needed, e.g.:
  - required by dependencies
  - any serious bug is found (including security-related)
  - adopting new stable Rust features
- Every MSRV bump is documented in the release notes when it happens.

## Platforms

- Linux / Android: inotify
- macOS: FSEvents or kqueue, see features
- Windows: ReadDirectoryChangesW
- iOS / FreeBSD / NetBSD / OpenBSD / DragonflyBSD: kqueue
- All platforms: polling

## License

notify is licensed under the [CC Zero 1.0][cc0].    
notify-types is licensed under the [MIT] or [Apache-2.0][apache] license.  
notify-debouncer-mini is licensed under the [MIT] or [Apache-2.0][apache] license.  
notify-debouncer-full is licensed under the [MIT] or [Apache-2.0][apache] license.  
file-id is licensed under the [MIT] or [Apache-2.0][apache] license.

## Origins

Inspired by Go's [fsnotify] and Node.js's [Chokidar], born out of need for
[cargo watch], and general frustration at the non-existence of C/Rust
cross-platform notify libraries.

Originally created by [Félix Saparelli] and awesome [contributors].

[Chokidar]: https://github.com/paulmillr/chokidar
[FileSystemEventSecurity]: https://developer.apple.com/library/mac/documentation/Darwin/Conceptual/FSEvents_ProgGuide/FileSystemEventSecurity/FileSystemEventSecurity.html
[debouncer-full-docs]: https://docs.rs/notify-debouncer-full/latest/notify_debouncer_full/
[debouncer-mini-docs]: https://docs.rs/notify-debouncer-mini/latest/notify_debouncer_mini/
[Félix Saparelli]: https://passcod.name
[alacritty]: https://github.com/jwilm/alacritty
[alert-after]: https://github.com/frewsxcv/alert-after
[build]: https://github.com/notify-rs/notify/actions
[cargo watch]: https://github.com/passcod/cargo-watch
[cc0]: ./notify/LICENSE-CC0
[MIT]: ./file-id/LICENSE-MIT
[apache]: ./file-id/LICENSE-APACHE
[changelog]: ./CHANGELOG.md
[cobalt]: https://github.com/cobalt-org/cobalt.rs
[coc]: http://contributor-covenant.org/version/1/4/
[contributors]: https://github.com/notify-rs/notify/graphs/contributors
[crate]: https://crates.io/crates/notify
[deno]: https://github.com/denoland/deno
[docket]: https://iwillspeak.github.io/docket/
[notify-docs]: https://docs.rs/notify/latest/notify/
[notify-types-docs]: https://docs.rs/notify-types/
[file-id-docs]: https://docs.rs/file-id/latest/file_id/
[fsnotify]: https://github.com/fsnotify/fsnotify
[handlebars-iron]: https://github.com/sunng87/handlebars-iron
[hotwatch]: https://github.com/francesca64/hotwatch
[mdBook]: https://github.com/rust-lang-nursery/mdBook
[notify-rust]: https://github.com/hoodie/notify-rust
[rust-analyzer]: https://github.com/rust-analyzer/rust-analyzer
[serde]: https://serde.rs/
[watchexec]: https://github.com/mattgreen/watchexec
[wiki]: https://github.com/notify-rs/notify/wiki
[xi-editor]: https://xi-editor.io/
[watchfiles]: https://watchfiles.helpmanual.io/
[zed]: https://github.com/zed-industries/zed
[examples]: examples/

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:notify-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same CC0-1.0 license as the upstream [`notify-rs/notify`](https://github.com/notify-rs/notify.git). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the notify authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`notify-rs/notify`](https://github.com/notify-rs/notify.git) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
