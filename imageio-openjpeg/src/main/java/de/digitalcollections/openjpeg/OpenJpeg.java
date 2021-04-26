package de.digitalcollections.openjpeg;

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
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.byref.PointerByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenJpeg {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenJpeg.class);

  @SuppressWarnings("checkstyle:constantname")
  private static final opj_msg_callback debugLogFn = (msg, data) -> LOGGER.debug(msg.trim());

  @SuppressWarnings("checkstyle:constantname")
  private static final opj_msg_callback warnLogFn = (msg, data) -> LOGGER.warn(msg.trim());

  @SuppressWarnings("checkstyle:constantname")
  private static final opj_msg_callback errorLogFn = (msg, data) -> LOGGER.error(msg.trim());

  public libopenjp2 lib;
  public Runtime runtime;

  static {
  }

  /** Load the library. */
  public OpenJpeg() {
    this.lib = LibraryLoader.create(libopenjp2.class).load("openjp2");
    if (!lib.opj_version().startsWith("2.")) {
      throw new UnsatisfiedLinkError(
          String.format("OpenJPEG version must be at least 2.0.0 (found: %s)", lib.opj_version()));
    }
    this.runtime = Runtime.getRuntime(lib);
  }

  private void setupLogger(Pointer codec) {
    if (LOGGER.isDebugEnabled()) {
      if (!lib.opj_set_info_handler(codec, debugLogFn)) {
        throw new RuntimeException("Could not set info logging handler");
      }
    }
    if (LOGGER.isWarnEnabled()) {
      if (!lib.opj_set_warning_handler(codec, warnLogFn)) {
        throw new RuntimeException("Could not set warning logging handler");
      }
    }
    if (LOGGER.isErrorEnabled()) {
      if (!lib.opj_set_error_handler(codec, errorLogFn)) {
        throw new RuntimeException("Could not set error logging handler");
      }
    }
  }

  /** Obtain information about the JPEG200 image located at the given path. */
  public Info getInfo(Path filePath) throws IOException {
    if (!Files.exists(filePath)) {
      throw new FileNotFoundException(String.format("File not found at %s", filePath));
    } else if (!Files.isReadable(filePath)) {
      throw new IOException(String.format("File not readable at %s", filePath));
    }
    Pointer ptr =
        lib.opj_stream_create_default_file_stream(filePath.toAbsolutePath().toString(), true);
    try {
      return getInfo(ptr);
    } finally {
      lib.opj_stream_destroy(ptr);
    }
  }

  /** Obtain information about the JPEG200 image in the input stream. */
  public Info getInfo(InStreamWrapper wrapper) throws IOException {
    try {
      return this.getInfo(wrapper.getNativeStream());
    } finally {
      wrapper.close();
    }
  }

  private Info getInfo(Pointer stream) throws IOException {
    Pointer codec = null;
    opj_image img = null;
    try {
      codec = getCodec(0);
      img = getImage(stream, codec);
      return getInfo(codec, img);
    } finally {
      if (codec != null) {
        lib.opj_destroy_codec(codec);
      }
      if (img != null) {
        img.free(lib);
      }
    }
  }

  private Info getInfo(Pointer codecPointer, opj_image img) {
    opj_codestream_info_v2 csInfo = null;
    try {
      csInfo = lib.opj_get_cstr_info(codecPointer);
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
      if (csInfo != null) {
        csInfo.free(lib);
      }
    }
  }

  private Pointer getCodec(int reduceFactor) throws IOException {
    Pointer codec = lib.opj_create_decompress(CODEC_FORMAT.OPJ_CODEC_JP2);
    setupLogger(codec);
    opj_dparameters params = new opj_dparameters(Runtime.getRuntime(lib));
    lib.opj_set_default_decoder_parameters(params);
    params.cp_reduce.set(reduceFactor);
    if (!lib.opj_setup_decoder(codec, params)) {
      throw new IOException("Error setting up decoder!");
    }
    return codec;
  }

  private opj_image getImage(Pointer stream, Pointer codec) throws IOException {
    opj_image img = new opj_image(Runtime.getRuntime(lib));
    PointerByReference imgPtr = new PointerByReference();
    if (!lib.opj_read_header(stream, codec, imgPtr)) {
      throw new IOException("Error while reading header.");
    }
    img.useMemory(imgPtr.getValue());
    return img;
  }

  /**
   * Decode the JPEG2000 image in the input stream to a BufferedImage.
   *
   * @param wrapper Wrapper around the input stream pointing to the image
   * @param area Region of the image to decode
   * @param reduceFactor Scale down the image by a factor of 2^reduceFactor
   * @return the decoded image
   * @throws IOException if there's a problem decoding the image
   */
  public BufferedImage decode(InStreamWrapper wrapper, Rectangle area, int reduceFactor)
      throws IOException {
    try {
      return decode(wrapper.getNativeStream(), area, reduceFactor);
    } finally {
      wrapper.close();
    }
  }

  /**
   * Decode the JPEG2000 image located at the given path to a BufferedImage.
   *
   * @param filePath Path to the JPEG2000 image file.
   * @param area Region of the image to decode
   * @param reduceFactor Scale down the image by a factor of 2^reduceFactor
   * @return the decoded image
   * @throws IOException if there's a problem decoding the image or reading the file
   */
  public BufferedImage decode(Path filePath, Rectangle area, int reduceFactor) throws IOException {
    if (!Files.exists(filePath)) {
      throw new FileNotFoundException(String.format("File not found at %s", filePath));
    } else if (!Files.isReadable(filePath)) {
      throw new IOException(String.format("File not readable at %s", filePath));
    }
    Pointer ptr =
        lib.opj_stream_create_default_file_stream(filePath.toAbsolutePath().toString(), true);
    try {
      return decode(ptr, area, reduceFactor);
    } finally {
      lib.opj_stream_destroy(ptr);
    }
  }

  private BufferedImage decode(Pointer stream, Rectangle area, int reduceFactor)
      throws IOException {
    Pointer codec = null;
    opj_image img = null;
    try {
      codec = getCodec(reduceFactor);
      img = getImage(stream, codec);

      // Configure decoding area
      int targetWidth;
      int targetHeight;
      if (area == null) {
        if (!lib.opj_set_decode_area(
            codec,
            Struct.getMemory(img),
            img.x0.intValue(),
            img.y0.intValue(),
            img.x1.intValue(),
            img.y1.intValue())) {
          throw new IOException("Could not set decoding area!");
        }
      } else {
        lib.opj_set_decode_area(
            codec,
            Struct.getMemory(img),
            area.x,
            area.y,
            area.x + area.width,
            area.y + area.height);
      }

      if (!lib.opj_decode(codec, stream, Struct.getMemory(img))) {
        throw new IOException("Could not decode image!");
      }

      BufferedImage bufImg;
      int numcomps = img.numcomps.intValue();
      opj_image_comp[] comps = img.comps.get(numcomps);
      targetWidth = comps[0].w.intValue();
      targetHeight = comps[0].h.intValue();
      COLOR_SPACE colorSpace = img.color_space.get();

      if (colorSpace == COLOR_SPACE.OPJ_CLRSPC_SYCC) {
        // prevent a JVM crash because the 3 color components has a different size
        throw new IOException("Images with YUV color space are currently not supported.");
      }

      // 8bit color depth is assumed as default color depth here -> 2 ^ 8 = 256 colors are available
      // For 16bit color depth -> 2 ^ 16 = 65536 color are available.
      // To "scale down" images with 16bit color depth to 8 bit, just a color depth factor needs to
      // be
      // calculated (65536 / 256 = 256) to correctly assign pixel in the underlying image buffer.
      // Note: prec and bpp seems to be interchanged in opj_image_comp struc.
      int colorDepthFactor = ((int) Math.pow(2, comps[0].prec.intValue())) / 256;
      colorDepthFactor = colorDepthFactor > 0 ? colorDepthFactor : 1;

      switch (numcomps) {
        case 3:
          {
            bufImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_3BYTE_BGR);

            // NOTE: We don't use bufImg.getRaster().setPixel, since directly accessing the
            // underlying buffer is ~400% faster
            Pointer red = comps[0].data.get();
            Pointer green = comps[1].data.get();
            Pointer blue = comps[2].data.get();
            byte[] bgrData = ((DataBufferByte) bufImg.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < targetWidth * targetHeight; i++) {
              bgrData[i * 3] = (byte) (blue.getInt(i * 4) / colorDepthFactor);
              bgrData[i * 3 + 1] = (byte) (green.getInt(i * 4) / colorDepthFactor);
              bgrData[i * 3 + 2] = (byte) (red.getInt(i * 4) / colorDepthFactor);
            }
          }
          break;
        case 1:
          {
            bufImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_GRAY);
            Pointer ptr = comps[0].data.get();
            byte[] data = ((DataBufferByte) bufImg.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < targetWidth * targetHeight; i++) {
              data[i] = (byte) (ptr.getInt(i * 4) / colorDepthFactor);
            }
          }
          break;
        case 2:
          {
            // gray with alpha
            bufImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_4BYTE_ABGR);
            Pointer gray = comps[0].data.get();
            Pointer alpha = comps[1].data.get();
            byte[] bgrData = ((DataBufferByte) bufImg.getRaster().getDataBuffer()).getData();
            for (int i = 0, j = 0; i < targetWidth * targetHeight; i++) {
              bgrData[j++] = (byte) (alpha.getInt(i * 4) / colorDepthFactor);
              byte colValue = (byte) (gray.getInt(i * 4) / colorDepthFactor);
              bgrData[j++] = colValue;
              bgrData[j++] = colValue;
              bgrData[j++] = colValue;
            }
          }
          break;
        case 4:
          {
            if (colorSpace == COLOR_SPACE.OPJ_CLRSPC_CMYK) {
              bufImg = decodeCMYK(targetWidth, targetHeight, numcomps, colorDepthFactor, comps);
            } else {
              bufImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_4BYTE_ABGR);
              Pointer red = comps[0].data.get();
              Pointer green = comps[1].data.get();
              Pointer blue = comps[2].data.get();
              Pointer alpha = comps[3].data.get();
              byte[] bgrData = ((DataBufferByte) bufImg.getRaster().getDataBuffer()).getData();
              for (int i = 0, j = 0; i < targetWidth * targetHeight; i++) {
                bgrData[j++] = (byte) (alpha.getInt(i * 4) / colorDepthFactor);
                bgrData[j++] = (byte) (blue.getInt(i * 4) / colorDepthFactor);
                bgrData[j++] = (byte) (green.getInt(i * 4) / colorDepthFactor);
                bgrData[j++] = (byte) (red.getInt(i * 4) / colorDepthFactor);
              }
            }
          }
          break;
        case 5:
          {
            bufImg = decodeCMYK(targetWidth, targetHeight, numcomps, colorDepthFactor, comps);
          }
          break;
        default:
          throw new IOException(String.format("Unsupported number of components: %d", numcomps));
      }
      return bufImg;
    } finally {
      if (img != null) {
        img.free(lib);
      }
      if (codec != null) {
        lib.opj_destroy_codec(codec);
      }
    }
  }

  /**
   * decode image with CMYK color space.
   *
   * @param width width in pixel
   * @param height height in pixel
   * @param numcomps number of components
   * @param colorDepthFactor factor to reduce to a 8 bit color
   * @param comps the components
   * @return the image
   */
  private BufferedImage decodeCMYK(
      int width, int height, int numcomps, int colorDepthFactor, opj_image_comp[] comps) {
    boolean hasAlpha = numcomps > 4;
    ColorModel colorModel =
        new ComponentColorModel(
            new CMYKColorSpace(), hasAlpha, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    BufferedImage bufImg =
        new BufferedImage(
            colorModel, colorModel.createCompatibleWritableRaster(width, height), false, null);

    Pointer[] data = new Pointer[numcomps];
    for (int i = 0; i < data.length; i++) {
      // cyan, magenta, yellow, key, (alpha)
      data[i] = comps[i].data.get();
    }

    byte[] bgrData = ((DataBufferByte) bufImg.getRaster().getDataBuffer()).getData();
    for (int i = 0, j = 0; i < width * height; i++) {
      for (int c = 0; c < data.length; c++) {
        bgrData[j++] = (byte) (data[c].getInt(i * 4) / colorDepthFactor);
      }
    }

    return bufImg;
  }

  private opj_image createImage(Raster img) {
    int numBands = img.getSampleModel().getNumBands();
    if (numBands != 3 && numBands != 1) {
      throw new IllegalArgumentException("Image must be RGB or Greyscale");
    }
    if (!(img.getSampleModel() instanceof PixelInterleavedSampleModel)) {
      throw new IllegalArgumentException("Image must be of the 3BYTE_BGR or BYTE_GRAY");
    }
    opj_image_comptparm[] params = Struct.arrayOf(runtime, opj_image_comptparm.class, numBands);
    for (int i = 0; i < numBands; i++) {
      params[i].prec.set(8); // One byte per component
      params[i].bpp.set(8); // 8bit depth
      params[i].sgnd.set(0);
      params[i].dx.set(1);
      params[i].dy.set(1);
      params[i].w.set(img.getWidth());
      params[i].h.set(img.getHeight());
    }

    COLOR_SPACE cspace = numBands == 3 ? COLOR_SPACE.OPJ_CLRSPC_SRGB : COLOR_SPACE.OPJ_CLRSPC_GRAY;
    opj_image outImg = new opj_image(runtime);
    Pointer imgPtr = lib.opj_image_create(params.length, Struct.getMemory(params[0]), cspace);
    outImg.useMemory(imgPtr);

    outImg.x0.set(0);
    outImg.y0.set(0);
    outImg.x1.set(img.getWidth());
    outImg.y1.set(img.getHeight());

    byte[] imgData = ((DataBufferByte) img.getDataBuffer()).getData();
    int numcomps = (int) outImg.numcomps.get();
    opj_image_comp[] comps = outImg.comps.get(numcomps);

    if (numcomps > 1) {
      Pointer red = comps[0].data.get();
      Pointer green = comps[1].data.get();
      Pointer blue = comps[2].data.get();
      int offset = 0;
      for (int y = 0; y < img.getHeight(); y++) {
        for (int x = 0; x < img.getWidth(); x++) {
          red.putByte(offset * 4, imgData[offset * 3 + 2]);
          green.putByte(offset * 4, imgData[offset * 3 + 1]);
          blue.putByte(offset * 4, imgData[offset * 3]);
          offset += 1;
        }
      }
    } else {
      Pointer ptr = comps[0].data.get();
      for (int i = 0; i < img.getWidth() * img.getHeight(); i++) {
        ptr.putByte(i * 4, imgData[i]);
      }
    }
    return outImg;
  }

  /**
   * Encode a raster image to a JPEG2000 image.
   *
   * @param img image to encode
   * @param output wrapped OutputStream the image should be written to
   * @param params encoding parameters
   * @throws IOException if encoding fails
   */
  public void encode(Raster img, OutStreamWrapper output, opj_cparameters params)
      throws IOException {
    opj_image image = null;
    Pointer codec = null;
    try {
      image = createImage(img);

      // Disable MCT for grayscale images
      if (image.numcomps.get() == 1) {
        params.tcp_mct.set(0);
      }

      codec = lib.opj_create_compress(CODEC_FORMAT.OPJ_CODEC_JP2);
      setupLogger(codec);

      if (params == null) {
        params = new opj_cparameters(runtime);
        lib.opj_set_default_encoder_parameters(params);
      }

      if (!lib.opj_setup_encoder(codec, params, image)) {
        throw new IOException("Could not setup encoder!");
      }

      if (!lib.opj_start_compress(codec, image, output.getNativeStream())) {
        throw new IOException("Could not start encoding");
      }
      if (!lib.opj_encode(codec, output.getNativeStream())) {
        throw new IOException("Could not encode");
      }
      if (!lib.opj_end_compress(codec, output.getNativeStream())) {
        throw new IOException("Could not finish encoding.");
      }
    } finally {
      if (image != null) {
        image.free(lib);
      }
      if (codec != null) {
        lib.opj_destroy_codec(codec);
      }
      if (output != null) {
        output.close();
      }
    }
  }
}
