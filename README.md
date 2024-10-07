Also see the following:
  - README of *multiplatform Git branch* in [Cutcon repository](https://github.com/mahozad/cutcon)
  - README of [compose-video-player repository](https://github.com/mahozad/compose-video-player)
  - https://github.com/JetBrains/compose-multiplatform/issues/1089#issuecomment-1617782890

For previous manual method of embedding VLC, check out Clipper Git tag 1.14.0.

All versions of VLC for Window/Linux/macOS are available for download in: https://get.videolan.org/vlc/
- macOS dmg files can be extracted using 7zip
- Linux should be compiled from source using CLang or GCC or even Gradle C/C++ support  
  See https://stackoverflow.com/a/34448694.

For playing videos, the vlcj library is used.
It provides Java bindings for libvlc which is a library developed by and used in VLC media player.

Normally, vlcj requires VLC to have been installed on the system, so that
it can use VLC plugin (library) files (*.dll* on Windows and *.so* on Linux and Android).

But we provide the DLL files ourselves so that VLC installation on system won't be required
(this also prevents many problems such as if the VLC gets uninstalled by user, or updated to another version, or is 32-bit, etc.)
We should just ensure to properly detect those DLLs to be used for the video player.
For this, in our video player component, we use a custom strategy for vlcj `NativeDiscovery` (its constructor argument).

Using a custom strategy is important because using the default strategy (no argument),
if VLC is installed on the system, it may be detected by the default strategy
and the VLC DLLs may interfere with our own DLLs.

Instead of calling `NativeDiscovery(...).discover()` could also have simply called the following:

> NOTE The order of loading these two files matters; first libvlccore.dll then libvlc.dll

```kotlin
System.load((assetsPath / VLC_DIRECTORY_NAME / "libvlccore.dll").absolutePathString())
System.load((assetsPath / VLC_DIRECTORY_NAME / "libvlc.dll").absolutePathString())
```

Not all VLC DLL files (plugins) are necessary for our app.
See the build script for a list of minimum required VLC DLL files that I found out with trial and error.
There are also tools to find out which DLLs are used by a process (like our app process):
- [Official Microsoft process explorer tool](https://learn.microsoft.com/en-us/outlook/troubleshoot/performance/using-process-explorer-to-list-dlls-running-under-outlook-exe)
- https://learn.microsoft.com/en-us/cpp/build/reference/dumpbin-reference?view=msvc-170
- https://learn.microsoft.com/en-us/sysinternals/downloads/listdlls
- https://stackoverflow.com/questions/475148/how-do-i-find-out-which-dlls-an-executable-will-load

I tried official Microsoft process explorer tool (first link above) and just used the DLLs detected by it but the player did not play and just stayed black.

The VLC is automatically downloaded, unzipped, and minified (if proper environment variables are set; see IDEA run configurations)
by the vlcSetup plugin and copied into `src/assets/windows/vlc/` directory.
Name of the directory in `src/assets/window/` (here, `vlc`) does NOT matter;
but name and directory structure of all files and subdirectories DOES matter and should be preserved;
`libvlc.dll` and `libvlccore.dll` at the root and a `plugins` subdirectory containing directories with other DLLs.

The Compose Multiplatform task that copies our custom app resources is `prepareAppResources` with type `Sync`.
So, we made this task depend on our custom task so the VLC files are extracted to the custom resources directory first,
so they will be included by the `prepareAppResources` task.

Minification of DLLs by about 50% (without changing any behavior) is done with this tool:
https://github.com/upx/upx

Note that, compressing DLLs and executables has a cost. The performance and the memory consumption increases.
See https://stackoverflow.com/q/353634
and https://quarkus.io/blog/upx/
