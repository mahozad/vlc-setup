For a list of tested media formats and codecs, see the HTML file besides this file.

Links for downloading VLC releases (source code or packaged):
- https://get.videolan.org/vlc/
- https://download.videolan.org/pub/vlc/
- http://ftp.videolan.org/pub/videolan/vlc
  Very useful code/issues of a multiplatform app embedding vlc for windows/linux/mac:
- https://github.com/JetBrains/compose-multiplatform/issues/1089
- https://github.com/simplex-chat/simplex-chat/pull/3052
- https://github.com/simplex-chat/simplex-chat/pull/3120
- https://github.com/simplex-chat/simplex-chat/pull/3130
- https://github.com/simplex-chat/simplex-chat/pull/3136
- https://github.com/simplex-chat/simplex-chat/scripts/desktop/prepare-vlc-linux.sh
  How to build VLC from source:
- https://wiki.videolan.org/Category:Building/
  How to build VLC for Linux:
- https://wiki.videolan.org/UnixCompile/
  How to build VLC for Android (could also be helpful because Android is Linux; see how VLC builds itself for Android):
- https://wiki.videolan.org/AndroidCompile/
- https://code.videolan.org/videolan/vlc-android/
- https://mvnrepository.com/artifact/org.videolan.android/libvlc-all
- https://github.com/masterwok/simple-vlc-player
- https://github.com/mrmaffen/vlc-android-sdk
- https://stackoverflow.com/questions/39311753/embed-libvlc-into-my-android-app-is-not-playing-video-only-audio-is-being-playe

Hardware video acceleration (see https://wiki.archlinux.org/index.php/Hardware_video_acceleration):
- NVIDIA "vdpau" (mesa-vdpau-drivers;libvdpau)
- intel "vaapi"(libva )
- AMD "vaapi" and "vdpau"

To inspect which libraries are used by a process or by a .so library file,
see https://stackoverflow.com/questions/50159/how-to-show-all-shared-libraries-used-by-executables-in-linux
Note that, if the so files have been optimized/shrinked with upx, then probably cannot see the needed library names.
- readelf -d file.so | grep 'NEEDED'
- ldd file.so
- use chrpath file.so or readelf -d file.so to see the current rpath/runpath of the file
- very important: use single quote ' and NOT double quote " in '$ORIGIN' to prevent it being evaluated as a bash variable

chrpath has the downside that if the so file does not already have an rpath/runpath, setting rpath does not work. so we use patchelf.

By default, the libvlccore.so has no rpath/runpath in it, so in a case like this, Linux looks for dynamic libraries in places like
/lib/ /usr/lib/ /lib64/ etc. which are defined by the LD_LIBRARY_PATH environment variable. See https://unix.stackexchange.com/q/22926.


https://unix.stackexchange.com/questions/46478/join-the-executable-and-all-its-libraries


---------------------------------------------------------------------------------------------------

## Setup VLC for Linux
In each of VLC releases, it provides installers for Windows and macOS but provides just the source code (a tar file) for Linux.
Providing VLC for Linux is more complicated: see https://github.com/caprica/vlcj/issues/1096#issuecomment-981168374
There are various ways to build VLC for Linux:
- Download the VLC release source code tar (from the links above) and build the vlc dynamically (use the distro libraries) (see building for linux above)
- Download the VLC release source code tar (from the links above) and build the vlc statically (provide all needed libraries) (see building for linux above)
- Download and use official/unofficial VLC universal self-contained packages/installers (Snap/AppImage/Flatpak)
- Checkout the release version tag on VLC GitHub/Gitlab repository and build the VLC Snap package ourselves

See:
- List of VLC libraries/plugins: https://wiki.videolan.org/Contrib_Status/
- https://unix.stackexchange.com/questions/227910/will-my-linux-binary-work-on-all-distros
- https://stackoverflow.com/questions/78000488/is-there-a-list-of-shared-libraries-available-in-any-linux
- https://askubuntu.com/questions/350068/where-does-ubuntu-look-for-shared-libraries
- https://www.tecmint.com/understanding-shared-libraries-in-linux/
- https://github.com/conan-io/conan/issues/11465#Sharing-binaries-across-different-linux-distros

### Troubleshooting and debugging
- Make sure to clean/delete the clipper/cutcon directory in which the vlcSetup plugin copies the vlc plugins to
- Make sure to clean/delete the vlc files and directories in the ~/.gradle/vlc-plugins-linux
- Make sure to clean/delete the vlc files and directories in the ~/.gradle/vlc-setup
- Try to enable the full (all) vlc plugins by setting environment variable `vlcAllPlugins=true` (or any other way that this is set in the application or vlcSetup plugin)
- Try to disable compression of plugins by setting environment variable `vlcCompression=false` (or any other way that this is set in the application or vlcSetup plugin)
- Enable clipper/cutcon debug logs by setting environment variable `loggingLevel=debug` (or any other way that this is set in the application)
    + This enables getting the vlcj-found path of vlc plugins. The path is accessible in the `onFound()` method of our custom discoverer
- Inspect the exact logs of vlc plugins printed in the standard output
- Remove the `--plugins-cache` vlc option in the MediaPlayer class of clipper/cutcon
- Add `--reset-plugins-cache` vlc option in the MediaPlayer class of clipper/cutcon
- Add `--no-plugins-cache` vlc option in the MediaPlayer class of clipper/cutcon
- Do a successful run of the clipper/cutcon app on a linux distribution that runs the app successfully and check which .so libraries are loaded/accessed by the clipper/cutcon process

### Build VLC from source code (dynamically or statically)
See building for Linux above for more detail.
- Download the VLC release source code archive (see the links above)
- extract it: `tar xJf vlc-3.0.21.tar.xz`
- `cd vlc-3.0.21`
- `sudo apt install g++ make libtool automake autopoint pkg-config flex bison lua5.2`
- Enable sources with either of these ways (instructions for Ubuntu):
    + Open *Software & Updates* app and enable the *Sources* checkbox and click close and click reload
    + In `/etc/apt/sources.list` uncomment lines that start with `deb-src` and then `sudo apt update`
- `./bootstrap`
- Link against libraries:
    + To link against dynamic libraries (meaning libraries installed or available on the OS):  
      I tried this on Ubuntu 18.04 and the result vlc program created and launched successfully.  
      `sudo apt build-dep vlc`
    + To link statically (that is, provide the libraries along with the vlc):  
      I tried this on Ubuntu 18.04 but after a lot of time and downloading many libraries, the make command failed at the end
      Also, even if this method works, what should be done next? How and what files should we grab for our plugin?  
      `sudo apt install subversion yasm cvs cmake ragel`  
      `cd contrib`  
      `mkdir native`  
      `cd native`  
      `../bootstrap`  
      `make`
- `./configure` (make sure it executes and ends with no error)  
  To disable one or more capabilities, pass --disable-<NAME> arguments to the command. For example, --disable-libass --disable-lua --disable-swscale
  See https://stackoverflow.com/a/57985984
- `./compile`

### Use VLC packages/installers
Different Linux distributions have different package management systems.

Debian and distributions derived from it (like Ubuntu (and its variants like Kubuntu, Xubuntu etc.), Mint, Kali, etc.)
use a packaging format called **.deb**. The tool to deal with this format is called **apt**.

RedHat (RHEL) and distributions derived from it (like CentOS, Fedora, openSUSE, etc.) use a packaging format called **.rpm**.

Arch linux and Manjaro use **pacman** for package management.

The deb and rpm formats typically do not include all the required dependencies of a program.
Instead, they just include the main program files and the instructions to download/install/use
required libraries for the program.

Now, new formats have been introduced to make it possible to publish a single self-contained installer/package
(like that of Windows .msi or .exe installers) that can be installed or used in most Linux distributions without additional requirements.
These include Flatpak, AppImage, and Snap formats.

Fortunately, VLC publishes an official Snap package: https://snapcraft.io/vlc
Note that apps can publish different variants (called channels) on the Snap repository.
For example, a stable channel, a beta channel, an old channel etc.
Unfortunately, each channel only has the latest version of an app, so there seems
to be no way to download, for example, a previous stable Snap version of VLC (meaning, our builds could not be reliably reproduced).
So, we can probably upload each vlc snap files to maven repository as a library or keep a backup of them if/when VLC snap gets updated.
So, we are able to use this self-contained package of VLC (that includes all its libraries) down below.

See https://github.com/cmatomic/VLCplayer-AppImage
and https://github.com/flathub/org.videolan.VLC
and https://github.com/ivan-hc/VLC-appimage/releases
and https://stackoverflow.com/q/51355937
and https://forum.videolan.org/viewtopic.php?f=13&p=539607
and https://code.google.com/archive/p/olpc-video-streaming/wikis/BuildingStaticVlc.wiki
and https://askubuntu.com/questions/865858/how-to-compile-the-current-vlc-version-2-2-4-on-ubuntu-12-04
and https://code.videolan.org/videolan/vlc/-/issues/28356
and https://code.videolan.org/videolan/vlc/-/issues/27174

Make sure to remove the option "--quiet" and pass the options "--verbose", "2" to vlc (through vlcj MediaPlayerFactory)
to see all errors and warnings from VLC when running the app.

Here are the steps for extracting the vlc snap package (tried on Ubuntu 18.04):

1. Remove the default installed VLC (if any) on Ubuntu (to make sure our app does not accidentally use it):  
   https://askubuntu.com/questions/572865/how-to-fully-remove-vlc-player
    - snap remove vlc
    - sudo apt remove vlc-nox
    - sudo apt remove vlc
    - sudo apt autoremove

2. Make sure no VLC is installed:
   which vlc
   whereis vlc

3. Inspect available versions of VLC in SNAP format:  
   https://askubuntu.com/questions/1268615/snap-install-specific-old-version
    - snap info vlc

4. Download the snap package of VLC (instead of directly installing it)  
   it will be downloaded in the current working directory
    - sudo snap download vlc --channel=latest/stable
      (can also install vlc with sudo snap install vlc --channel=latest/stable)
    - Another way to get a direct download link (and other information) of the vlc snap: https://search.apps.ubuntu.com/api/v1/package/vlc

5. Extract the Snap file using either of the following ways:  
   https://askubuntu.com/questions/1162798/how-do-i-view-the-contents-of-a-snap-file
    - file-roller --force --extract-to="vlc/" vlc.snap
    - unsquashfs -d "vlc/" vlc.snap
    - Mount the downloaded vlc snap file   
      mkdir <mount-folder-name>  
      sudo mount -t squashfs -o ro /path/to/my.snap /path/to/<mount-folder-name>  
      Extract the directory to another folder (also needed because it is read-only):  
      sudo cp -r vlc-mount/ vlc-mount-copy/  
      Unmount and remove the original mounted folder:  
      sudo umount vlc-mount/ && rm -r vlc-mount/

6. Install chrpath tool (could also use patchelf program):  
   sudo apt update  
   sudo apt install chrpath

7. (Optional) View all .so files that have rpath= or runpath= in them
   find . -name "*.so*" | xargs -n1 chrpath | grep "="

8. Do these in order (very important: use single quote ' and NOT double quote " in '$ORIGIN' to prevent it being evaluated as a bash variable):
   cd vlc-mount-copy/usr/lib/
   sudo chrpath -r '$ORIGIN' libvlc.so
   cd vlc/
   sudo chrpath -r '$ORIGIN/..' libvlc_pulse.so.0.0.0
   sudo chrpath -r '$ORIGIN/..' libvlc_xcb_events.so.0.0.0
   cd plugins/
   find . -name "*.so*" | sudo xargs -n1 chrpath -r '$ORIGIN/../../..'
   cd ../../../../..
   cp -r vlc-mount-copy/usr/lib/* <project-path>/asset/linux/vlc
   cp -r <project-path>/asset/linux/vlc/x86_64-linux-gnu/* <project-path>/asset/linux/vlc/
   rm -r <project-path>/asset/linux/vlc/ssl/
   rm -r <project-path>/asset/linux/vlc/jvm/
   rm -r <project-path>/asset/linux/vlc/debug/
   rm -r <project-path>/asset/linux/vlc/x86_64-linux-gnu/

### Build VLC package/installer ourselves
We may be able to build the Snap package ourselves like how VLC itself builds its Snap package:
- https://github.com/videolan/vlc/blob/master/extras/package/snap/snapcraft.yaml
- https://code.videolan.org/videolan/vlc/-/blob/master/extras/ci/gitlab-ci.yml
- https://search.apps.ubuntu.com/api/v1/package/vlc (download link of vlc snap)
  It cannot be done in a VirtualBox Linux because snapcraft does not work for some reason.

I tried this method in Ubuntu 18.04 both in the release source code of vlc (see download links above)
(which did not contain the snap file and I manually copied the snap files from vlc git repository to the vlc/extras/package/snap/)
and then tried to build the snap using the`snapcraft` command
with the working directory in vlc/extras/package/ or .../package/snap (no additional argument needed)
and after a lot of time passed and they downloaded many things, they both failed with an error like
*could not clone into ../../../ something... git exit code 128*

I did not try to make the snap like how the vlc makes it in its extras/ci/gitlab-ci.yml.
Try the way it does as well and see if it works.

#### What is rpath (DT_RPATH) and runpath (DT_RUNPATH)?
See this good explanation:
https://unix.stackexchange.com/questions/22926/where-do-executables-look-for-shared-objects-at-runtime

rpath designates the run-time search path hard-coded in an executable file or library.

The rpath is stored in the executable (it's the DT_RPATH or DT_RUNPATH dynamic attribute).
It can contain absolute paths or relative paths or paths starting with $ORIGIN.
Relative paths are relative to the terminal or process working directory whereas $ORIGIN is the location of the file
(e.g. if the file is in /opt/myapp/bin and its rpath is $ORIGIN/../lib:$ORIGIN/../plugins then the dynamic linker will look in /opt/myapp/lib and /opt/myapp/plugins).
https://stackoverflow.com/questions/38058041/correct-usage-of-rpath-relative-vs-absolute

rpath was deprecated in favor of runpath.
https://stackoverflow.com/questions/7967848/use-rpath-but-not-runpath

patchelf vs chrpath (for viewing, changing, deleting rpath)
https://stackoverflow.com/questions/13769141/can-i-change-rpath-in-an-already-compiled-binary

using objdump -x libvlc.so or objdump -p libvlc.so | grep NEEDED to inspect an so file
https://en.wikipedia.org/wiki/Rpath
https://en.wikipedia.org/wiki/Ldd_(Unix)

using ldd mylib.so file to inspect references
it is a way to view what/which libraries are bound to your executable
https://stackoverflow.com/questions/29422614/how-to-set-the-path-that-a-so-library-will-search-for-other-so-libraries

Note that ldd seems to show the indirect library dependencies as well. For example, ldd libvlc.so
shows libidn but that is because libvlc depends on libvlccore and libvlccore needs libidn.

ldd vs readelf
https://stackoverflow.com/questions/6242761/determine-direct-shared-object-dependencies-of-a-linux-binary


---------------------------------------------------------------------------------------------------


Also see the following:
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
