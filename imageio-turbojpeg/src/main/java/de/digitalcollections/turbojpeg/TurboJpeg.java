package de.digitalcollections.turbojpeg;

import com.google.common.collect.Streams;
import de.digitalcollections.turbojpeg.imageio.TurboJpegImageReader;
import de.digitalcollections.turbojpeg.imageio.TurboJpegImageWriter;
import de.digitalcollections.turbojpeg.lib.enums.TJPF;
import de.digitalcollections.turbojpeg.lib.enums.TJSAMP;
import de.digitalcollections.turbojpeg.lib.enums.TJXOP;
import de.digitalcollections.turbojpeg.lib.enums.TJXOPT;
import de.digitalcollections.turbojpeg.lib.libturbojpeg;
import de.digitalcollections.turbojpeg.lib.structs.tjscalingfactor;
import de.digitalcollections.turbojpeg.lib.structs.tjtransform;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.byref.NativeLongByReference;
import jnr.ffi.byref.PointerByReference;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public class TurboJpeg {
  public libturbojpeg lib;
  public Runtime runtime;

  public TurboJpeg() {
    lib = LibraryLoader.create(libturbojpeg.class).load("turbojpeg");
    runtime = Runtime.getRuntime(lib);
  }

  public Info getInfo(byte[] jpegData) throws TurboJpegException {
    Pointer codec = null;
    try {
      codec = lib.tjInitDecompress();

      IntByReference width = new IntByReference();
      IntByReference height = new IntByReference();
      IntByReference jpegSubsamp = new IntByReference();
      int rv = lib.tjDecompressHeader2(
          codec, ByteBuffer.wrap(jpegData), jpegData.length, width, height, jpegSubsamp);
      if (rv != 0) {
        throw new TurboJpegException(lib.tjGetErrorStr());
      }

      IntByReference numRef = new IntByReference();
      Pointer factorPtr = lib.tjGetScalingFactors(numRef);
      tjscalingfactor[] factors = new tjscalingfactor[numRef.getValue()];
      for (int i=0; i < numRef.getValue(); i++) {
        tjscalingfactor f = new tjscalingfactor(runtime);
        factorPtr = factorPtr.slice(i * Struct.size(f));
        f.useMemory(factorPtr);
        factors[i] = f;
      }
      return new Info(width.getValue(), height.getValue(), jpegSubsamp.getValue(), factors);
    } finally {
      if (codec != null && codec.address() != 0) lib.tjDestroy(codec);
    }
  }

  public BufferedImage decode(byte[] jpegData, Info info, Dimension size) throws TurboJpegException {
    Pointer codec = null;
    try {
      codec = lib.tjInitDecompress();
      int width = info.getWidth();
      int height = info.getHeight();
      if (size != null) {
        if (!info.getAvailableSizes().contains(size)) {
          throw new IllegalArgumentException(String.format(
              "Invalid size, must be one of %s", info.getAvailableSizes()));
        } else {
          width = size.width;
          height = size.height;
        }
      }
      boolean isGray = info.getSubsampling() == TJSAMP.TJSAMP_GRAY.intValue();
      int imgType;
      if (isGray) {
        imgType = BufferedImage.TYPE_BYTE_GRAY;
      } else {
        imgType = BufferedImage.TYPE_3BYTE_BGR;
      }
      BufferedImage img = new BufferedImage(width, height, imgType);
      ByteBuffer outBuf = ByteBuffer.wrap(((DataBufferByte) img.getRaster().getDataBuffer()).getData())
                                    .order(runtime.byteOrder());
      int rv = lib.tjDecompress2(
          codec, ByteBuffer.wrap(jpegData), jpegData.length, outBuf,
          width, isGray ? width : width * 3, height, isGray ? TJPF.TJPF_GRAY : TJPF.TJPF_BGR, 0);
      if (rv != 0) {
        throw new TurboJpegException(lib.tjGetErrorStr());
      }
      return img;
    } finally {
      if (codec != null && codec.address() != 0) lib.tjDestroy(codec);
    }
  }

  public ByteBuffer encode(Raster img, int quality) throws TurboJpegException {
    Pointer codec = null;
    Pointer bufPtr = null;
    try {
      TJPF pixelFmt;
      switch (img.getNumBands()) {
        case 4:
          pixelFmt = TJPF.TJPF_BGRX; // 4BYTE_BGRA
          break;
        case 3:
          pixelFmt = TJPF.TJPF_BGR;  // 3BYTE_BGR
          break;
        case 1:
          pixelFmt = TJPF.TJPF_GRAY; // 1BYTE_GRAY
          break;
        default:
          throw new IllegalArgumentException("Illegal sample format");
      }
      TJSAMP sampling = pixelFmt == TJPF.TJPF_GRAY ? TJSAMP.TJSAMP_GRAY : TJSAMP.TJSAMP_444;
      codec = lib.tjInitCompress();
      int bufSize = (int) lib.tjBufSize(img.getWidth(), img.getHeight(), sampling);
      bufPtr = lib.tjAlloc(bufSize);
      NativeLongByReference lenPtr = new NativeLongByReference(bufSize);
      ByteBuffer inBuf = ByteBuffer.wrap(((DataBufferByte) img.getDataBuffer()).getData())
          .order(runtime.byteOrder());
      int rv = lib.tjCompress2(
          codec, inBuf, img.getWidth(), 0, img.getHeight(),  pixelFmt,
          new PointerByReference(bufPtr), lenPtr, sampling, quality, 0);
      if (rv != 0) {
        throw new TurboJpegException(lib.tjGetErrorStr());
      }
      ByteBuffer outBuf = ByteBuffer.allocate(lenPtr.getValue().intValue()).order(runtime.byteOrder());
      bufPtr.get(0, outBuf.array(), 0, lenPtr.getValue().intValue());
      outBuf.rewind();
      return outBuf;
    } finally {
      if (codec != null && codec.address() != 0) lib.tjDestroy(codec);
      if (bufPtr != null && bufPtr.address() != 0) lib.tjFree(bufPtr);
    }
  }

  public ByteBuffer transform(byte[] jpegData, Info info, Rectangle region, int rotation) throws TurboJpegException {
    Pointer codec = null;
    Pointer bufPtr = null;
    try {
      codec = lib.tjInitTransform();
      tjtransform transform = new tjtransform(runtime);

      int width = info.getWidth();
      int height = info.getHeight();
      boolean flipCoords = rotation == 90 || rotation == 270;
      if (region != null) {
        Dimension mcuSize = info.getMCUSize();
        if (region.width % mcuSize.width != 0 || region.height % mcuSize.height != 0) {
          throw new IllegalArgumentException(String.format(
              "Invalid cropping region, width must be divisible by %d, height by %d", mcuSize.width, mcuSize.height));
        }
        width = region.width;
        height = region.height;
        transform.options.set(TJXOPT.TJXOPT_CROP | TJXOPT.TJXOPT_TRIM);
        transform.r.x.set(region.x);
        transform.r.y.set(region.y);
        if ((region.x + region.width) >= (flipCoords ? info.getHeight() : info.getWidth())) {
          transform.r.w.set(0);
        } else {
          transform.r.w.set(region.width);
        }
        if ((region.y + region.height) >= (flipCoords ? info.getWidth() : info.getHeight())) {
          transform.r.h.set(0);
        } else {
          transform.r.h.set(region.height);
        }
      }
      if (rotation != 0) {
        if (flipCoords) {
          int w = width;
          int h = height;
          height = w;
          width = h;
        }
        TJXOP op;
        switch (rotation) {
          case 90:
            op = TJXOP.TJXOP_ROT90;
            break;
          case 180:
            op = TJXOP.TJXOP_ROT180;
            break;
          case 270:
            op = TJXOP.TJXOP_ROT270;
            break;
          default:
            throw new IllegalArgumentException("Invalid rotation, must be 90, 180 or 270");
        }
        transform.op.set(op.intValue());
      }
      int bufWidth = width;
      if (width == 0 && region != null) {
        bufWidth = info.getWidth() - region.x;
      }
      int bufHeight = height;
      if (height == 0 && region != null) {
        bufHeight = info.getHeight() - region.y;
      }
      int bufSize = (int) lib.tjBufSize(bufWidth, bufHeight, TJSAMP.TJSAMP_444);
      bufPtr = lib.tjAlloc(bufSize);
      NativeLongByReference lenRef = new NativeLongByReference(bufSize);
      Buffer inBuf = ByteBuffer.wrap(jpegData).order(runtime.byteOrder());
      int rv = lib.tjTransform(
          codec, inBuf, jpegData.length, 1, new PointerByReference(bufPtr),
          lenRef, transform, 0);
      if (rv != 0) {
        throw new TurboJpegException(lib.tjGetErrorStr());
      }
      ByteBuffer outBuf = ByteBuffer.allocate(lenRef.getValue().intValue()).order(runtime.byteOrder());
      bufPtr.get(0, outBuf.array(), 0, lenRef.getValue().intValue());
      outBuf.rewind();
      return outBuf;
    } finally {
      if (codec != null && codec.address() != 0) lib.tjDestroy(codec);
      if (bufPtr != null && bufPtr.address() != 0) lib.tjFree(bufPtr);
    }
  }

  private static Duration benchmarkDecode(ImageReader reader) throws IOException {
    Instant start = Instant.now();
    for (int i=0; i < 100; i++) {
      reader.setInput(ImageIO.createImageInputStream(new FileInputStream(new File("/tmp/bench/artificial.jpg"))));
      BufferedImage img = reader.read(0, null);
    }
    return Duration.between(start, Instant.now());
  }

  private static Duration benchmarkEncode(ImageWriter writer) throws IOException {
    IIOImage img = new IIOImage(ImageIO.read(new File("/tmp/bench/big_building.png")), null, null);
    ImageWriteParam param = writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(0.85f);
    Instant start = Instant.now();
    for (int i=0; i < 100; i++) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      writer.setOutput(ImageIO.createImageOutputStream(bos));
      writer.write(null, img, null);
    }
    return Duration.between(start, Instant.now());
  }

  public static void main(String[] args) throws Exception {
    TurboJpegImageReader tjReader = Streams.stream(ImageIO.getImageReadersByFormatName("jpeg"))
        .filter(TurboJpegImageReader.class::isInstance)
        .map(TurboJpegImageReader.class::cast)
        .findFirst().get();
    ImageReader defaultReader = Streams.stream(ImageIO.getImageReadersByFormatName("jpeg"))
        .filter(r -> !(r instanceof TurboJpegImageReader))
        .findFirst().get();

    TurboJpegImageWriter tjWriter = Streams.stream(ImageIO.getImageWritersByFormatName("jpeg"))
        .filter(TurboJpegImageWriter.class::isInstance)
        .map(TurboJpegImageWriter.class::cast)
        .findFirst().get();
    ImageWriter defaultWriter = Streams.stream(ImageIO.getImageWritersByFormatName("jpeg"))
        .filter(w -> !(w instanceof TurboJpegImageWriter))
        .findFirst().get();

    Duration tjDuration = benchmarkDecode(tjReader);
    Duration defaultDuration = benchmarkDecode(defaultReader);
    System.out.printf("Default decoding took %dms (%dms per iteration)\n", defaultDuration.toMillis(), defaultDuration.toMillis() / 100);
    System.out.printf("TurboJPEG decoding took %dms (%dms per iteration)\n", tjDuration.toMillis(), tjDuration
        .toMillis() / 100);


    defaultDuration = benchmarkEncode(defaultWriter);
    System.out.printf("Default encoding took %dms (%dms per iteration)\n", defaultDuration.toMillis(), defaultDuration.toMillis() / 100);

    tjDuration = benchmarkEncode(tjWriter);
    System.out.printf("TurboJPEG encoding took %dms (%dms per iteration)\n", tjDuration.toMillis(), tjDuration.toMillis
        () / 100);
  }
}
