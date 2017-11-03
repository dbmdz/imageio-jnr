# imageio-jnr

This repository contains ImageIO plugins that wrap the most common native
libraries for various image formats with [JNR-FFI](https://github.com/jnr/jnr-ffi).
This has the advantage of being generally the fastest available option on the
JVM, with the drawback that you need to make sure that the corresponding native
libraries are installed on the target system. However, thanks to JNR-FFI, no
compiler is neccessary, the plugins will directly use the installed native
libraries and you can install them directly from Maven Central.

## Currently available plugins

|     Artifact     |   Format  |      Backing native library         |             Required version               |
| ---------------- | --------- | ----------------------------------- | ------------------------------------------ |
| imageio-openjpeg | JPEG2000  | [OpenJPEG](http://www.openjpeg.org) | >= 2.0 (>=2.3 recommended for performance) |


## Planned plugins
- libjpeg-turbo (via TurboJPEG API)
- libpng
- libtiff
- libwebp

## Documentation
Coming up...
