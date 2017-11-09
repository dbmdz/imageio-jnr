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
import java.awt.Dimension;
import java.awt.Rectangle;
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
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.byref.NativeLongByReference;
import jnr.ffi.byref.PointerByReference;

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
      BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
      ByteBuffer outBuf = ByteBuffer.wrap(((DataBufferByte) img.getRaster().getDataBuffer()).getData())
                                    .order(runtime.byteOrder());
      int rv = lib.tjDecompress2(
          codec, ByteBuffer.wrap(jpegData), jpegData.length, outBuf,
          width, width * 3, height, TJPF.TJPF_BGR, 0);
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
      codec = lib.tjInitCompress();
      int bufSize = (int) lib.tjBufSize(img.getWidth(), img.getHeight(), TJSAMP.TJSAMP_444);
      bufPtr = lib.tjAlloc(bufSize);
      NativeLongByReference lenPtr = new NativeLongByReference(bufSize);
      ByteBuffer inBuf = ByteBuffer.wrap(((DataBufferByte) img.getDataBuffer()).getData())
                                   .order(runtime.byteOrder());
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
      int rv = lib.tjCompress2(
          codec, inBuf, img.getWidth(), 0, img.getHeight(),  pixelFmt,
          new PointerByReference(bufPtr), lenPtr, TJSAMP.TJSAMP_444, quality, 0);
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
      if (region != null) {
        if (width % 8 != 0 || height % 8 != 0) {
          throw new IllegalArgumentException(
              "Invalid cropping region, dimensions must be divisible by 8");
        }
        width = region.width;
        height = region.height;
        transform.options.set(TJXOPT.TJXOPT_CROP | TJXOPT.TJXOPT_PERFECT);
        transform.r.x.set(region.x);
        transform.r.y.set(region.y);
        transform.r.w.set(region.width);
        transform.r.h.set(region.height);
      }
      if (rotation != 0) {
        if (rotation == 90 || rotation == 270) {
          height = width;
          width = height;
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
        transform.op.set(transform.op.get() | op.intValue());
      }
      int bufSize = (int) lib.tjBufSize(width, height, TJSAMP.TJSAMP_444);
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
