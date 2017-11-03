package de.digitalcollections.turbojpeg.imageio;

import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;

public class TurboJpegImageWriter extends ImageWriter {
  protected TurboJpegImageWriter(ImageWriterSpi originatingProvider) {
    super(originatingProvider);
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

  }
}
