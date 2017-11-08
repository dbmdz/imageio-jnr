package de.digitalcollections.turbojpeg;

import com.google.common.io.ByteStreams;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.byref.NativeLongByReference;
import jnr.ffi.byref.PointerByReference;

public class TurboJpeg {
  public static libturbojpeg LIB = LibraryLoader.create(libturbojpeg.class).load("turbojpeg");
  public static Runtime RUNTIME = Runtime.getRuntime(LIB);

  public static Info getInfo(byte[] jpegData) throws TurboJpegException {
    Pointer codec = null;
    try {
      codec = LIB.tjInitDecompress();

      IntByReference width = new IntByReference();
      IntByReference height = new IntByReference();
      IntByReference jpegSubsamp = new IntByReference();
      int rv = LIB.tjDecompressHeader2(
          codec, ByteBuffer.wrap(jpegData), jpegData.length, width, height, jpegSubsamp);
      if (rv != 0) {
        throw new TurboJpegException(LIB.tjGetErrorStr());
      }

      IntByReference numRef = new IntByReference();
      Pointer factorPtr = LIB.tjGetScalingFactors(numRef);
      tjscalingfactor[] factors = new tjscalingfactor[numRef.getValue()];
      for (int i=0; i < numRef.getValue(); i++) {
        tjscalingfactor f = new tjscalingfactor(RUNTIME);
        factorPtr = factorPtr.slice(i * Struct.size(f));
        f.useMemory(factorPtr);
        factors[i] = f;
      }
      return new Info(width.getValue(), height.getValue(), jpegSubsamp.getValue(), factors);
    } finally {
      if (codec != null && codec.address() != 0) LIB.tjDestroy(codec);
    }
  }

  public static BufferedImage decode(byte[] jpegData, Info info, Dimension size) throws TurboJpegException {
    Pointer codec = null;
    try {
      codec = LIB.tjInitDecompress();
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
                                    .order(RUNTIME.byteOrder());
      int rv = LIB.tjDecompress2(
          codec, ByteBuffer.wrap(jpegData), jpegData.length, outBuf,
          width, width * 3, height, TJPF.TJPF_BGR, 0);
      if (rv != 0) {
        throw new TurboJpegException(LIB.tjGetErrorStr());
      }
      return img;
    } finally {
      if (codec != null && codec.address() != 0) LIB.tjDestroy(codec);
    }
  }

  public static ByteBuffer encode(Raster img, int quality) throws TurboJpegException {
    Pointer codec = null;
    Pointer bufPtr = null;
    try {
      codec = LIB.tjInitCompress();
      int bufSize = (int) LIB.tjBufSize(img.getWidth(), img.getHeight(), TJSAMP.TJSAMP_444);
      bufPtr = LIB.tjAlloc(bufSize);
      NativeLongByReference lenPtr = new NativeLongByReference(bufSize);
      ByteBuffer inBuf = ByteBuffer.wrap(((DataBufferByte) img.getDataBuffer()).getData())
                                   .order(RUNTIME.byteOrder());
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
      int rv = LIB.tjCompress2(
          codec, inBuf, img.getWidth(), 0, img.getHeight(),  pixelFmt,
          new PointerByReference(bufPtr), lenPtr, TJSAMP.TJSAMP_444, quality, 0);
      if (rv != 0) {
        throw new TurboJpegException(LIB.tjGetErrorStr());
      }
      ByteBuffer outBuf = ByteBuffer.allocate(lenPtr.getValue().intValue()).order(RUNTIME.byteOrder());
      bufPtr.get(0, outBuf.array(), 0, lenPtr.getValue().intValue());
      outBuf.rewind();
      return outBuf;
    } finally {
      if (codec != null && codec.address() != 0) LIB.tjDestroy(codec);
      if (bufPtr != null && bufPtr.address() != 0) LIB.tjFree(bufPtr);
    }
  }

  public static ByteBuffer transform(byte[] jpegData, Info info, Rectangle region, int rotation) throws TurboJpegException {
    Pointer codec = null;
    Pointer bufPtr = null;
    try {
      codec = LIB.tjInitTransform();
      tjtransform transform = new tjtransform(RUNTIME);

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
      int bufSize = (int) LIB.tjBufSize(width, height, TJSAMP.TJSAMP_444);
      bufPtr = LIB.tjAlloc(bufSize);
      NativeLongByReference lenRef = new NativeLongByReference(bufSize);
      Buffer inBuf = ByteBuffer.wrap(jpegData).order(RUNTIME.byteOrder());
      int rv = LIB.tjTransform(
          codec, inBuf, jpegData.length, 1, new PointerByReference(bufPtr),
          lenRef, transform, 0);
      if (rv != 0) {
        throw new TurboJpegException(LIB.tjGetErrorStr());
      }
      ByteBuffer outBuf = ByteBuffer.allocate(lenRef.getValue().intValue()).order(RUNTIME.byteOrder());
      bufPtr.get(0, outBuf.array(), 0, lenRef.getValue().intValue());
      outBuf.rewind();
      return outBuf;
    } finally {
      if (codec != null && codec.address() != 0) LIB.tjDestroy(codec);
      if (bufPtr != null && bufPtr.address() != 0) LIB.tjFree(bufPtr);
    }
  }

  public static void main(String[] args) throws Exception {
    byte[] jpegData = ByteStreams.toByteArray(new FileInputStream(new File("/tmp/lenna.jpg")));
    Info info = getInfo(jpegData);
    System.out.printf(
        "Width: %d, Height: %d, Subsampling: %d\n",
        info.getWidth(), info.getHeight(), info.getSubsampling());
    System.out.printf("There are %d available sizes\n", info.getAvailableSizes().size());
    for (Dimension size : info.getAvailableSizes()) {
      System.out.printf("%dx%d\n", size.width, size.height);
    }
    BufferedImage img = decode(jpegData, getInfo(jpegData), info.getAvailableSizes().get(2));
    System.out.printf("Image size: %dx%d\n", img.getWidth(), img.getHeight());
    ImageIO.write(img, "png", new File("/tmp/test.png"));

    BufferedImage in = ImageIO.read(new File("/tmp/test.png"));
    ByteBuffer data = encode(in.getRaster(), 85);
    try (FileOutputStream fos = new FileOutputStream(new File("/tmp/test.jpg"))) {
      fos.getChannel().write(data);
    };

    ByteBuffer transformedData = transform(jpegData, info, new Rectangle(15, 16, 165, 165), 270);
    Info transInfo = getInfo(transformedData.array());
    System.out.printf("Transformed size: %dx%d\n", transInfo.getWidth(), transInfo.getHeight());
    try (FileOutputStream fos = new FileOutputStream(new File("/tmp/test_transformed.jpg"))) {
      fos.getChannel().write(transformedData);
    }
    BufferedImage transformedImg = decode(transformedData.array(), transInfo, transInfo.getAvailableSizes().get(1));
    System.out.printf("Transformed and scaled size: %dx%d\n", transformedImg.getWidth(), transformedImg.getHeight());
    ImageIO.write(img, "png", new File("/tmp/test_transformed_decoded.png"));
  }
}
