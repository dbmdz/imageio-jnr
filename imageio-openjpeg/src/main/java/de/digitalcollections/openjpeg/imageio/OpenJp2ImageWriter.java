package de.digitalcollections.openjpeg.imageio;

import de.digitalcollections.openjpeg.OpenJpeg;
import de.digitalcollections.openjpeg.lib.structs.opj_cparameters;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

/**
 * ImageWriter for JPEG2000 images, based on the openjp2 library from the OpenJPEG project, accessed
 * via JNR-FFI.
 */
public class OpenJp2ImageWriter extends ImageWriter {
  private OpenJpeg lib;

  private ImageOutputStream stream = null;
  private ImageOutputStreamWrapper wrapper = null;

  protected OpenJp2ImageWriter(ImageWriterSpi originatingProvider, OpenJpeg lib) {
    super(originatingProvider);
    this.lib = lib;
  }

  @Override
  public void setOutput(Object output) {
    super.setOutput(output);
    if (output != null) {
      if (!(output instanceof ImageOutputStream)) {
        throw new IllegalArgumentException("Output not an ImageOutputStream");
      }
      this.stream = (ImageOutputStream) output;
      this.wrapper = new ImageOutputStreamWrapper(this.stream, lib);
    } else {
      this.stream = null;
      this.wrapper = null;
    }
  }

  @Override
  public ImageWriteParam getDefaultWriteParam() {
    return new OpenJp2ImageWriteParam();
  }

  @Override
  public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
    return null;
  }

  @Override
  public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
    return null;
  }

  @Override
  public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
    return null;
  }

  @Override
  public IIOMetadata convertImageMetadata(
      IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
    return null;
  }

  @Override
  public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param)
      throws IOException {
    RenderedImage img = image.getRenderedImage();
    if (param == null) {
      param = getDefaultWriteParam();
    }
    Rectangle sourceRegion = new Rectangle(0, 0, img.getWidth(), img.getHeight());
    if (param.getSourceRegion() != null) {
      sourceRegion = sourceRegion.intersection(param.getSourceRegion());
    }
    Raster raster = img.getData(sourceRegion);
    opj_cparameters cparams = ((OpenJp2ImageWriteParam) param).toNativeParams(lib);
    lib.encode(raster, this.wrapper, cparams);
  }
}
