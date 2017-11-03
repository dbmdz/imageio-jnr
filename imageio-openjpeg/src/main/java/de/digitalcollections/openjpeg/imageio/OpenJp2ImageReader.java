package de.digitalcollections.openjpeg.imageio;

import de.digitalcollections.openjpeg.Info;
import de.digitalcollections.openjpeg.OpenJpeg;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenJp2ImageReader extends ImageReader {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenJp2ImageReader.class);

  private ImageInputStream stream = null;
  private ImageInputStreamWrapper streamWrapper = null;
  private Info info = null;

  protected OpenJp2ImageReader(ImageReaderSpi originatingProvider) {
    super(originatingProvider);
  }

  @Override
  public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
    super.setInput(input, seekForwardOnly, ignoreMetadata);
    if (seekForwardOnly) {
      throw new IllegalArgumentException(
          "Only streams that can be seeked backwards are supported." +
          "If you used ImageIO.read(), please switch to using the reader directly.");
    }
    if (input == null) {
      return;
    }
    if (input instanceof  ImageInputStream) {
      stream = (ImageInputStream) input;
    } else {
      throw new IllegalArgumentException("Bad input.");
    }
    this.streamWrapper = new ImageInputStreamWrapper(stream);
  }

  @Override
  public int getNumImages(boolean allowSearch) throws IOException {
    return getInfo().getNumResolutions();
  }

  private void checkIndex(int imageIndex) {
    if (imageIndex >= getInfo().getNumResolutions()) {
      throw new IndexOutOfBoundsException("bad index");
    }
  }

  private Info getInfo() {
    if (this.info == null) {
      try {
        this.info = OpenJpeg.getInfo(this.streamWrapper);
        this.stream.seek(0);
        this.streamWrapper = new ImageInputStreamWrapper(this.stream);
      } catch (IOException e) {
        LOGGER.error("Error obtaining info", e);
        this.info = null;
      }
    }
    return this.info;
  }

  private int adjustSize(int size, int imageIndex) {
    return (int) (size / Math.pow(2, (double) imageIndex));
  }

  @Override
  public int getWidth(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return adjustSize(info.getNativeSize().width, imageIndex);
  }

  @Override
  public int getHeight(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return adjustSize(info.getNativeSize().height, imageIndex);
  }

  @Override
  public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    // TODO: Support grayscale?
    return Collections.singletonList(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_3BYTE_BGR))
                      .iterator();
  }

  /**
   * Decode the image. Note that the source region (if set via params) is always relative to the full-resolution image, not the requested resolution.*/
  @Override
  public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
    checkIndex(imageIndex);
    try {
      Rectangle sourceRegion = null;
      if (param != null) {
        sourceRegion = param.getSourceRegion();
      }
      return OpenJpeg.decode(streamWrapper, sourceRegion, imageIndex);
    } finally {
      if (this.streamWrapper != null) this.streamWrapper.close();
    }
  }

  @Override
  public boolean isImageTiled(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return getInfo().getNumTiles() > 1;
  }

  @Override
  public int getTileWidth(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return adjustSize(getInfo().getTileSize().width, imageIndex);
  }

  @Override
  public int getTileHeight(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return adjustSize(getInfo().getTileSize().height, imageIndex);
  }

  @Override
  public int getTileGridXOffset(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return getInfo().getTileOrigin().x;
  }

  @Override
  public int getTileGridYOffset(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return getInfo().getTileOrigin().y;
  }

  @Override
  public IIOMetadata getStreamMetadata() throws IOException {
    return null;
  }

  @Override
  public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    return null;
  }
}
