# imageio-jnr

This repository contains ImageIO plugins that wrap the most common native
libraries for various image formats with [JNR-FFI](https://github.com/jnr/jnr-ffi).
This has the advantage of being generally the fastest available option on the
JVM, with the drawback that you need to make sure that the corresponding native
libraries are installed on the target system. However, thanks to JNR-FFI, no
compiler is neccessary, the plugins will directly use the installed native
libraries and you can install them directly from Maven Central.

## Currently available plugins

|       Module      |   Format  |      Backing native library         |             Required version               |
| ----------------- | --------- | ----------------------------------- | ------------------------------------------ |
| imageio-openjpeg  | JPEG2000  | [OpenJPEG](http://www.openjpeg.org) | \>= 2.0 (>=2.3 recommended for performance) |
| imageio-turbojpeg |    JPEG   | [TurboJPEG/libjpeg-turbo](https://www.libjpeg-turbo.org/) | \>= 1.0               |


## Supported features

### imageio-openjpeg

- [x] **Decoding:**
  * [x] Tiled decoding
  * [x] Decoding of arbitrary regions
  * [x] Decoding of only a specific resolution
- [x] **Encoding:**
  * [x] Lossless compression
  * [x] Lossy compression with user-defined quality
  * [x] Tiled encoding
  * [x] Encoding of multiple resolutions
  
  
### imageio-turbojpeg
- [x] **Decoding:**
  * [x] Decoding of arbitrary regions
  * [x] Decoding of only a specific resolution
  * [x] Rotate image before decoding
- [x] **Encoding:**
  * [x] Lossy compression with user-defined quality


## Platform-(In)Dependence

JNR-FFI itself is compatible with [a large number of architectures](https://github.com/jnr/jffi/tree/master/archive),
so you merely need to make sure that the backing native libraries are compatible with your architecture.
Both [OpenJPEG](https://packages.debian.org/stretch/libopenjp2-7) and [TurboJPEG](https://packages.debian.org/stretch/libturbojpeg0)
are available for the majority of commonly used platforms.


## Planned plugins

- libpng
- libtiff
- libwebp

