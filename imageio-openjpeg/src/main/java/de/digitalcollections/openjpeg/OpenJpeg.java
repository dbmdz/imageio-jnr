package de.digitalcollections.openjpeg;

import de.digitalcollections.openjpeg.imageio.OpenJp2ImageWriteParam;
import de.digitalcollections.openjpeg.lib.callbacks.opj_msg_callback;
import de.digitalcollections.openjpeg.lib.enums.CODEC_FORMAT;
import de.digitalcollections.openjpeg.lib.enums.COLOR_SPACE;
import de.digitalcollections.openjpeg.lib.libopenjp2;
import de.digitalcollections.openjpeg.lib.structs.opj_codestream_info_v2;
import de.digitalcollections.openjpeg.lib.structs.opj_cparameters;
import de.digitalcollections.openjpeg.lib.structs.opj_dparameters;
import de.digitalcollections.openjpeg.lib.structs.opj_image;
import de.digitalcollections.openjpeg.lib.structs.opj_image_comp;
import de.digitalcollections.openjpeg.lib.structs.opj_image_comptparm;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.byref.PointerByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenJpeg {
  private final static Logger LOGGER = LoggerFactory.getLogger(OpenJpeg.class);
  public static libopenjp2 LIB = LibraryLoader.create(libopenjp2.class).load("openjp2");
  public static Runtime RUNTIME = Runtime.getRuntime(LIB);
  static {
    if (!LIB.opj_version().startsWith("2.")) {
      throw new RuntimeException(String.format(
          "OpenJPEG version must be at least 2.0.0 (found: %s)", LIB.opj_version()));
    }
  }

  private static final opj_msg_callback infoLogFn = (msg, data) -> LOGGER.debug(msg.trim());
  private static final opj_msg_callback warnLogFn = (msg, data) -> LOGGER.warn(msg.trim());
  private static final opj_msg_callback errorLogFn = (msg, data) -> LOGGER.error(msg.trim());

  private static void setupLogger(Pointer codec) {
    if (LOGGER.isInfoEnabled()) {
      if (!LIB.opj_set_info_handler(codec, infoLogFn)) {
        throw new RuntimeException("Could not set info logging handler");
      }
    }
    if (LOGGER.isWarnEnabled()) {
      if (!LIB.opj_set_warning_handler(codec, warnLogFn)) {
        throw new RuntimeException("Could not set warning logging handler");
      }
    }
    if (LOGGER.isErrorEnabled()) {
      if (!LIB.opj_set_error_handler(codec, errorLogFn)) {
        throw new RuntimeException("Could not set error logging handler");
      }
    }
  }

  public static Info getInfo(InStreamWrapper wrapper) throws IOException {
    Pointer codec = null;
    opj_image img = null;
    try {
      codec = getCodec(0);
      img = getImage(wrapper, codec);
      return getInfo(codec, img);
    } finally {
      if (codec != null) LIB.opj_destroy_codec(codec);
      if (img != null) img.close();
      wrapper.close();
    }
  }

  public static Pointer getCodec(int reduceFactor) throws IOException {
    Pointer codec = LIB.opj_create_decompress(CODEC_FORMAT.OPJ_CODEC_JP2);
    setupLogger(codec);
    opj_dparameters params = new opj_dparameters(Runtime.getRuntime(LIB));
    LIB.opj_set_default_decoder_parameters(params);
    params.cp_reduce.set(reduceFactor);
    if (!LIB.opj_setup_decoder(codec, params)) {
      throw new IOException("Error setting up decoder!");
    }
    return codec;
  };

  public static opj_image getImage(InStreamWrapper wrapper, Pointer codec) throws IOException {
    opj_image img = new opj_image(Runtime.getRuntime(LIB));
    PointerByReference imgPtr = new PointerByReference();
    if (!LIB.opj_read_header(wrapper.getNativeStream(), codec, imgPtr)) {
      throw new IOException("Error while reading header.");
    }
    img.useMemory(imgPtr.getValue());
    return img;
  }

  public static Info getInfo(Pointer codecPointer, opj_image img) {
    opj_codestream_info_v2 csInfo = null;
    try {
      csInfo = LIB.opj_get_cstr_info(codecPointer);
      Info info = new Info();
      info.setWidth(img.x1.intValue());
      info.setHeight(img.y1.intValue());
      info.setNumComponents(csInfo.nbcomps.intValue());
      info.setNumTilesX(csInfo.tw.intValue());
      info.setNumTilesY(csInfo.th.intValue());
      info.setTileWidth(csInfo.tdx.intValue());
      info.setTileHeight(csInfo.tdy.intValue());
      info.setTileOriginX(csInfo.tx0.intValue());
      info.setTileOriginY(csInfo.ty0.intValue());
      info.setNumResolutions(csInfo.m_default_tile_info.tccp_info.get().numresolutions.intValue());
      return info;
    } finally {
      if (csInfo != null) csInfo.close();
    }
  }

  public static BufferedImage decode(InStreamWrapper wrapper, Rectangle area, int reduceFactor)
      throws IOException {
    Pointer codec = null;
    opj_image img = null;
    try {
      codec = getCodec(reduceFactor);
      img = getImage(wrapper, codec);

      // Configure decoding area
      int targetWidth;
      int targetHeight;
      if (area == null) {
        if (!LIB.opj_set_decode_area(codec, Struct.getMemory(img), img.x0.intValue(), img.y0.intValue(),
                                     img.x1.intValue(), img.y1.intValue())) {
          throw new IOException("Could not set decoding area!");
        }
      } else {
        LIB.opj_set_decode_area(
            codec, Struct.getMemory(img),
            area.x, area.y, area.x + area.width, area.y + area.height);
      }

      if (!LIB.opj_decode(codec, wrapper.getNativeStream(), Struct.getMemory(img))) {
        throw new IOException("Could not decode image!");
      }

      opj_image_comp[] comps = img.comps.get(img.numcomps.intValue());
      targetWidth = comps[0].w.intValue();
      targetHeight = comps[0].h.intValue();
      BufferedImage bufImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_3BYTE_BGR);

      // NOTE: We don't use bufImg.getRaster().setPixel, since directly accessing the underlying buffer is ~400% faster
      Pointer red = comps[0].data.get();
      Pointer green = comps[1].data.get();
      Pointer blue = comps[2].data.get();
      byte[] rgbData = ((DataBufferByte) bufImg.getRaster().getDataBuffer()).getData();
      for (int i = 0; i < targetWidth * targetHeight; i++) {
        rgbData[i * 3] = (byte) blue.getInt(i * 4);
        rgbData[i * 3 + 1] = (byte) green.getInt(i * 4);
        rgbData[i * 3 + 2] = (byte) red.getInt(i * 4);
      }
      return bufImg;
    } finally {
      if (img != null) img.close();
      if (codec != null) LIB.opj_destroy_codec(codec);
    }
  }

  public static opj_image createImage(Raster img) {
    if (img.getSampleModel().getNumBands() != 3) {
      throw new IllegalArgumentException("Image must be RGB");
    }
    if (!(img.getSampleModel() instanceof PixelInterleavedSampleModel)) {
      throw new IllegalArgumentException("Image must be of the 3BYTE_BGR");
    }
    opj_image_comptparm parms[] = Struct.arrayOf(RUNTIME, opj_image_comptparm.class, 3);
    for (int i=0; i < 3; i++) {
      parms[i].prec.set(8);  // One byte per component
      parms[i].bpp.set(8);   // 8bit depth
      parms[i].sgnd.set(0);
      parms[i].dx.set(1);
      parms[i].dy.set(1);
      parms[i].w.set(img.getWidth());
      parms[i].h.set(img.getHeight());
    }

    opj_image outImg = new opj_image(RUNTIME);
    Pointer imgPtr = LIB.opj_image_create(parms.length, Struct.getMemory(parms[0]), COLOR_SPACE.OPJ_CLRSPC_SRGB);
    outImg.useMemory(imgPtr);

    outImg.x0.set(0);
    outImg.y0.set(0);
    outImg.x1.set(img.getWidth());
    outImg.y1.set(img.getHeight());

    byte[] rgbData = ((DataBufferByte) img.getDataBuffer()).getData();
    opj_image_comp[] comps = outImg.comps.get((int) outImg.numcomps.get());
    Pointer red = comps[0].data.get();
    Pointer green = comps[1].data.get();
    Pointer blue = comps[2].data.get();
    int offset = 0;
    for (int y=0; y < img.getHeight(); y++) {
      for (int x=0; x < img.getWidth(); x++) {
        red.putByte(offset*4, rgbData[offset*3+2]);
        green.putByte(offset*4, rgbData[offset*3+1]);
        blue.putByte(offset*4, rgbData[offset*3]);
        offset += 1;
      }
    }
    try (FileOutputStream fos = new FileOutputStream(new File("/tmp/red.bin"))) {
      byte[] buf = new byte[img.getWidth()*img.getHeight()];
      red.get(0, buf, 0, buf.length);
      fos.write(buf);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return outImg;
  }

  public static void encode(Raster img, OutStreamWrapper output, opj_cparameters params) throws IOException {
    opj_image image = null;
    Pointer codec = null;
    try {
      image = createImage(img);

      codec = LIB.opj_create_compress(CODEC_FORMAT.OPJ_CODEC_JP2);
      setupLogger(codec);

      if (params == null) {
        params = new OpenJp2ImageWriteParam().toNativeParams();
      }

      if (!LIB.opj_setup_encoder(codec, params, image)) {
        throw new IOException("Could not setup encoder!");
      }

      if (!LIB.opj_start_compress(codec, image, output.getNativeStream())) {
        throw new IOException("Could not start encoding");
      }
      if (!LIB.opj_encode(codec, output.getNativeStream())) {
        throw new IOException("Could not encode");
      }
      if (!LIB.opj_end_compress(codec, output.getNativeStream())) {
        throw new IOException("Could not finish encoding.");
      }
    } finally {
      if (image != null) image.close();
      if (codec != null) LIB.opj_destroy_codec(codec);
      if (output != null) output.close();
    }
  }
}
