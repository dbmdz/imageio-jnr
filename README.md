# imageio-jnr

[![Javadocs](https://javadoc.io/badge/de.digitalcollections.imageio/imageio-jnr.svg)](https://javadoc.io/doc/de.digitalcollections.imageio/imageio-jnr)
[![License](https://img.shields.io/github/license/dbmdz/imageio-jnr.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/release/dbmdz/imageio-jnr.svg)](https://github.com/dbmdz/imageio-jnr/releases)
[![Maven Central](https://img.shields.io/maven-central/v/de.digitalcollections.imageio/imageio-jnr.svg)](https://central.sonatype.com/search?q=imageio-jnr)

This repository contains ImageIO plugins that wrap the most common native
libraries for various image formats with [JNR-FFI](https://github.com/jnr/jnr-ffi).
This has the advantage of being generally the fastest available option on the
JVM, with the drawback that you need to make sure that the corresponding native
libraries are installed on the target system. However, thanks to JNR-FFI, no
compiler is neccessary, the plugins will directly use the installed native
libraries and you can install them directly from Maven Central.

Please note that the plugins need at least Java 11 and Ubuntu 20.04.

## Currently available plugins

|       Module      |   Format  |      Backing native library         |             Required version               | JavaDoc
| ----------------- | --------- | ----------------------------------- | ------------------------------------------ | ---
| imageio-openjpeg  | JPEG2000  | [OpenJPEG](http://www.openjpeg.org) | \>= 2.0 (>=2.3 recommended for performance) | [![Javadocs](http://javadoc.io/badge/de.digitalcollections.imageio/imageio-openjpeg.svg)](http://javadoc.io/doc/de.digitalcollections.imageio/imageio-openjpeg)
| imageio-turbojpeg |    JPEG   | [TurboJPEG](https://libjpeg-turbo.org/About/TurboJPEG) | \>= 1.4               | [![Javadocs](http://javadoc.io/badge/de.digitalcollections.imageio/imageio-turbojpeg.svg)](http://javadoc.io/doc/de.digitalcollections.imageio/imageio-turbojpeg)


## Installation
To use the ImageIO plugins, include them in your dependencies:

```xml
<dependency>
  <groupId>de.digitalcollections.imageio</groupId>
  <artifactId>imageio-turbojpeg</artifactId>
  <version>0.6.7</version>
</dependency>

<dependency>
  <groupId>de.digitalcollections.imageio</groupId>
  <artifactId>imageio-openjpeg</artifactId>
  <version>0.6.7</version>
</dependency>
```

Before using them, make sure that you have all the required native libraries
installed, e.g. on Debian-based systems:

```
$ sudo apt install libturbojpeg0 libopenjp2-7
```


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
Both [OpenJPEG](https://packages.debian.org/bullseye/libopenjp2-7) and [TurboJPEG](https://packages.debian.org/bullseye/libturbojpeg0)
are available for the majority of commonly used platforms.