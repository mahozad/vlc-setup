This is for publishing self-contained VLC *.so* plugins as a JVM library to Maven repositories
to be used in Linux, because VLC does not publish .snap or .appimage files for Linux in its
releases (at least not yet).

### TODO
This whole Gradle project (vlc-plugins-linux) and its subproject (vlc-plugins-linux-setup)
and also their resulting library published to Maven repositories will no longer be needed
when the following issue is resolved: https://code.videolan.org/videolan/vlc/-/issues/29204.

The project should be executed on a Linux machine (like Linux Mint).

To generate the VLC files (plugins and some misc needed files) (generated in `src/main/resources/files/`):

```shell
./gradlew :vlc-plugins-linux:vlcPreparePlugins
```

To publish the VLC files (plugins and some misc needed files) as a JAR file to the *XYZ* repository:

```shell
./gradlew :vlc-plugins-linux:publishAllPublicationsToXYZ
```
