[![Gradle plugin badge]][Plugin page on Gradle Plugin Portal]

# VLC Setup
A Gradle plugin for [Compose Multiplatform][cmp] desktop applications to automatically
embed [VLC][vlc] which is needed to play media files (video, audio, image) via [vlcj][vlcj].

The plugin prepares and includes proper VLC plugin files (*.dll* for Windows, *.so* for Linux, *.dylib* for macOS)
so your application becomes self-contained and there will be no need for VLC media player to have been installed on end-user systems.

The plugin features:
  - Support for Windows, Linux, macOS (experimental)
  - Option to compress VLC plugin files to reduce their size
  - Option to include only some base VLC files for common media types
  - Option to select version of VLC to use (not supported for Linux yet)

> [!WARNING]
> The plugin was tested only on my system. Further feedback is needed to detect problems.

> [!Note]
> See the table in [supported-formats-codecs.html][Supported Formats and Codecs Preview] for supported formats/codecs by the base (default) VLC plugins.

> [!Note]
> If all VLC plugins are included, then virtually any format and codec should be supported and be playable.

> [!Note]
> The project (media playback) worked OK on the OSes defined in [tested-operating-systems.html][Tested Operating Systems Preview].

## Getting started
Follow the steps below to implement a media player in your desktop CMP app.  
To see a fully-working real application, visit the [Cutcon][Cutcon] project.

1. First, add the plugin to your build.gradle\[.kts] file:
   ```kotlin
   plugins {
       // ...
       id("ir.mahozad.vlc-setup") version "0.1.0"
   }
   ```
2. Next, you should [add files to the packaged CMP application][Add files to packaged app].  
   So, specify a custom directory (folder) where you would like to place VLC plugin files:
   ```kotlin
   compose.desktop {
       application {
           // ...
           nativeDistributions {
               appResourcesRootDir = rootDir.resolve("myAssets/") // <projectRoot>/myAssets/
   ```
3. Then specify the plugin options in the `vlcSetup{}` block in the build.gradle\[.kts] file  
   (specifically, the path for each OS (same path above suffixed with `/<OS name>` where the plugin should put the VLC plugin files):
   ```kotlin
   vlcSetup {
       vlcVersion = "3.0.21"
       shouldCompressVlcFiles = true
       shouldIncludeAllVlcFiles = false
       pathToCopyVlcLinuxFilesTo = rootDir.resolve("myAssets/linux/")
       pathToCopyVlcMacosFilesTo = rootDir.resolve("myAssets/macos/")
       pathToCopyVlcWindowsFilesTo = rootDir.resolve("myAssets/windows/")
   }
   ```
4. Implement custom vlcj `NativeDiscoveryStrategy` classes (See the [Cutcon][Cutcon] project)
5. Implement your media player (See the [Cutcon][Cutcon] project)

I plan to release a library for Compose Multiplatform to make steps 4 and 5 easier.

[cmp]: https://github.com/jetbrains/compose-multiplatform
[vlc]: https://github.com/videolan/vlc
[vlcj]: https://github.com/caprica/vlcj
[Add files to packaged app]: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-native-distribution.html#adding-files-to-packaged-application
[Cutcon]: https://github.com/mahozad/cutcon
[Gradle plugin badge]: https://img.shields.io/gradle-plugin-portal/v/ir.mahozad.vlc-setup?label=Gradle%20Plugin%20Portal&labelColor=303030&logo=data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMzIgMzIiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+DQogICAgPGxpbmVhckdyYWRpZW50IGlkPSJhIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgeDE9IjEuNDU3NjU3IiB4Mj0iMjYuNjMwOTMiIHkxPSIzLjIwOTcxMSIgeTI9IjI3LjU2MzgxIj4NCiAgICAgICAgPHN0b3Agb2Zmc2V0PSIwIiBzdG9wLWNvbG9yPSIjMjA5YmM0Ii8+DQogICAgICAgIDxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iIzRkYzljMCIvPg0KICAgIDwvbGluZWFyR3JhZGllbnQ+DQogICAgPHBhdGggZmlsbD0idXJsKCNhKSIgZD0ibTMwLjI2IDUuODA0YTUuMDkxIDUuMDkxIDAgMCAwIC0zLjUwMi0xLjQ3NSA1LjA5NyA1LjA5NyAwIDAgMCAtMy41NTEgMS4zNTUuNDg4LjQ4OCAwIDAgMCAwIC43MDZsLjYxMi42MjJhLjQ4NS40ODUgMCAwIDAgLjYzMy4wNDIgMi45MjUgMi45MjUgMCAwIDEgNC4wMTMuMzYyIDIuODc2IDIuODc2IDAgMCAxIC0uMiAzLjk5NWMtNC4wMyAzLjk5Ny05LjQwNy03LjIxLTIxLjYxLTEuNDM0YTEuNjUyIDEuNjUyIDAgMCAwIC0uODY1IDEgMS42MyAxLjYzIDAgMCAwIC4xNTQgMS4zMWwyLjA5NSAzLjU5YTEuNjYxIDEuNjYxIDAgMCAwIDIuMjQuNjFsLjA1LS4wMjctLjAzOC4wMjkuOTE3LS41MDlhMjEuNDY4IDIxLjQ2OCAwIDAgMCAyLjkyNC0yLjE2NC41MTQuNTE0IDAgMCAxIC42NjUtLjAyMS40NzIuNDcyIDAgMCAxIC4wMjIuNzA2IDIxLjk0IDIxLjk0IDAgMCAxIC0zLjA4NyAyLjMwNmgtLjAzMmwtLjkyOC41MTZhMi42MjYgMi42MjYgMCAwIDEgLTEuMjg1LjMzMiAyLjY2OSAyLjY2OSAwIDAgMSAtMS4zMjgtLjM0OSAyLjY0IDIuNjQgMCAwIDEgLS45NzMtLjk2MWwtMS45ODEtMy4zOTNjLTMuNzg4IDIuNjc2LTYuMTE1IDcuODE3LTQuODU1IDE0LjMzYS40OC40OCAwIDAgMCAuNDczLjM5MmgyLjIzNGEuNDg2LjQ4NiAwIDAgMCAuNDk0LS40MzggMy4yNyAzLjI3IDAgMCAxIDEuMDk4LTIuMDM3IDMuMzE5IDMuMzE5IDAgMCAxIDQuMzU2IDAgMy4yNzQgMy4yNzQgMCAwIDEgMS4wOTggMi4wMzcuNDc2LjQ3NiAwIDAgMCAuNDc3LjQyaDIuMTg0YS40ODYuNDg2IDAgMCAwIC40NzYtLjQyIDMuMjcgMy4yNyAwIDAgMSAxLjA5OC0yLjAzNyAzLjMxOSAzLjMxOSAwIDAgMSA0LjM1NyAwIDMuMjc0IDMuMjc0IDAgMCAxIDEuMDk4IDIuMDM3LjQ3OS40NzkgMCAwIDAgLjQ3Ni40MmgyLjE3YS40ODUuNDg1IDAgMCAwIC40ODQtLjQ3M2MuMDUtMy4wMzcuODc1LTYuNTI1IDMuMjI2LTguMjczIDguMTQ1LTYuMDQ4IDYuMDA0LTExLjIzIDQuMTE5LTEzLjF6bS04LjMwNSA5LjEzNC0xLjU1NC0uNzczYS45NjEuOTYxIDAgMCAxIC40NjMtLjgyNS45OC45OCAwIDAgMSAxLjMxOS4yODIuOTY0Ljk2NCAwIDAgMSAtLjIyOCAxLjMyeiIvPg0KPC9zdmc+DQo=
[Plugin page on Gradle Plugin Portal]: https://plugins.gradle.org/plugin/ir.mahozad.vlc-setup
[Supported Formats and Codecs Preview]: https://html-preview.github.io/?url=https://github.com/mahozad/vlc-setup/blob/main/supported-formats-codecs.html
[Tested Operating Systems Preview]: https://html-preview.github.io/?url=https://github.com/mahozad/vlc-setup/blob/main/tested-operating-systems.html
