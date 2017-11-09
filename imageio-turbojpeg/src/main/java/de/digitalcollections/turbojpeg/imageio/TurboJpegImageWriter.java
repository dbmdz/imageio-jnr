package de.digitalcollections.turbojpeg.imageio;

import de.digitalcollections.turbojpeg.TurboJpeg;
import de.digitalcollections.turbojpeg.TurboJpegException;
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

public class TurboJpegImageWriter extends ImageWriter {
  private ImageOutputStream stream;
  private final TurboJpeg lib;

  protected TurboJpegImageWriter(ImageWriterSpi originatingProvider, TurboJpeg lib) {
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
    } else {
      this.stream = null;
    }
  }

  @Override
  public ImageWriteParam getDefaultWriteParam() {
    return new TurboJpegImageWriteParam();
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
  public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
    return null;
  }

  @Override
  public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
    RenderedImage img = image.getRenderedImage();
    if (stream == null) {
      throw new IOException("Set an output first!");
    }
    if (param == null) {
      param = getDefaultWriteParam();
    }
    Rectangle sourceRegion = new Rectangle(0, 0, img.getWidth(), img.getHeight());
    if (param.getSourceRegion() != null) {
      sourceRegion = sourceRegion.intersection(param.getSourceRegion());
    }
    Raster raster = img.getData(sourceRegion);
    int quality = 85;
    if (param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT) {
      quality = (int) (param.getCompressionQuality() * 100);
    }
    try {
      stream.write(lib.encode(raster, quality).array());
    } catch (TurboJpegException e) {
      throw new IOException(e);
    }
  }
}
