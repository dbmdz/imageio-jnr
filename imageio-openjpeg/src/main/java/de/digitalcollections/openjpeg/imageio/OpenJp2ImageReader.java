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

/**
 * ImageReader for JPEG2000 images, based on the openjp2 library from the OpenJPEG project, accessed via JNR-FFI.
 */
public class OpenJp2ImageReader extends ImageReader {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenJp2ImageReader.class);

  private OpenJpeg lib;
  private ImageInputStream stream = null;
  private ImageInputStreamWrapper streamWrapper = null;
  private Info info = null;

  protected OpenJp2ImageReader(ImageReaderSpi originatingProvider, OpenJpeg lib) {
    super(originatingProvider);
    this.lib = lib;
  }

  @Override
  public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
    super.setInput(input, seekForwardOnly, ignoreMetadata);
    // Clean up old input
    this.dispose();

    if (input == null) {
      return;
    }
    if (input instanceof  ImageInputStream) {
      stream = (ImageInputStream) input;
    } else {
      throw new IllegalArgumentException("Bad input.");
    }
    if (this.streamWrapper != null) {
      this.streamWrapper.close();
    }
    this.streamWrapper = new ImageInputStreamWrapper(stream, lib);
  }

  /**
   * Corresponds to the number of resolutions in the image.
   *
   * Image 0 has the native resolution, all following indices are 1/2^idx times smaller.
   */
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
        this.info = lib.getInfo(this.streamWrapper);
        this.stream.seek(0);
        this.streamWrapper = new ImageInputStreamWrapper(this.stream, lib);
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

  /**
   * Get the width of the given resolution of the image.
   */
  @Override
  public int getWidth(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return adjustSize(info.getNativeSize().width, imageIndex);
  }

  /**
   * Get the height of the given resolution of the image.
   */
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

  private Rectangle adjustRegion(int imageIndex, Rectangle sourceRegion) throws IOException {
    if (sourceRegion == null) {
      return null;
    }
    if (sourceRegion.x == 0 && sourceRegion.y == 0
        && sourceRegion.width == getWidth(imageIndex)
        && sourceRegion.height == getHeight(imageIndex)) {
      return null;
    }
    double scaleFactor = (double) getWidth(0) / (double) getWidth(imageIndex);
    return new Rectangle(
        (int) Math.ceil(scaleFactor * sourceRegion.x),
        (int) Math.ceil(scaleFactor * sourceRegion.y),
        (int) Math.ceil(scaleFactor * sourceRegion.width),
        (int) Math.ceil(scaleFactor * sourceRegion.height));
  }

  /**
   * Read the image in the given resolution.
   */
  @Override
  public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
    checkIndex(imageIndex);
    try {
      Rectangle sourceRegion = null;
      if (param != null) {
        sourceRegion = adjustRegion(imageIndex, param.getSourceRegion());
      }
      return lib.decode(streamWrapper, sourceRegion, imageIndex);
    } finally {
      if (this.streamWrapper != null) {
        this.streamWrapper.close();
      }
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
  public BufferedImage readTile(int imageIndex, int tileX, int tileY) throws IOException {
    checkIndex(imageIndex);
    int xOffset = getTileGridXOffset(imageIndex);
    int yOffset = getTileGridYOffset(imageIndex);
    int tileWidth = getTileWidth(imageIndex);
    int tileHeight = getTileHeight(imageIndex);
    Rectangle region = new Rectangle(
        xOffset + tileX * tileWidth,
        yOffset + tileY * tileHeight,
        tileWidth,
        tileHeight);
    if (region.x + region.width > getWidth(imageIndex) || region.y + region.height > getHeight(imageIndex)) {
      throw new IllegalArgumentException("Tile indices out of bounds.");
    }
    ImageReadParam param = getDefaultReadParam();
    param.setSourceRegion(region);
    return this.read(imageIndex, param);
  }

  @Override
  public IIOMetadata getStreamMetadata() throws IOException {
    return null;
  }

  @Override
  public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    return null;
  }

  @Override
  public void dispose() {
    if (this.streamWrapper != null) {
      this.streamWrapper.close();
      this.streamWrapper = null;
    }
    this.info = null;
  }
}
