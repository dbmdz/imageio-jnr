package de.digitalcollections.turbojpeg.imageio;

import de.digitalcollections.turbojpeg.Info;
import de.digitalcollections.turbojpeg.TurboJpeg;
import de.digitalcollections.turbojpeg.TurboJpegException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.stream.Stream;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR_PRE;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;

public class TurboJpegImageReader extends ImageReader {
  private final static Logger LOGGER = LoggerFactory.getLogger(TurboJpegImageReader.class);

  private final TurboJpeg lib;
  private ByteBuffer jpegData;
  private Info info;

  protected TurboJpegImageReader(ImageReaderSpi originatingProvider, TurboJpeg lib) {
    super(originatingProvider);
    this.lib = lib;
  }

  @Override
  public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
    super.setInput(input, seekForwardOnly, ignoreMetadata);
    if (input == null) {
      return;
    }
    if (input instanceof ImageInputStream) {
      try {
        jpegData = bufferFromStream((ImageInputStream) input);
        info = lib.getInfo(jpegData.array());
      } catch (IOException e) {
        throw new IllegalArgumentException("Failed to read input.");
      } catch (TurboJpegException e) {
        throw new IllegalArgumentException("Failed to read JPEG info.");
      }
    } else {
      throw new IllegalArgumentException("Bad input.");
    }
  }

  private void checkIndex(int imageIndex) {
    if (imageIndex >= info.getAvailableSizes().size()) {
      throw new IndexOutOfBoundsException("bad index");
    }
  }

  private ByteBuffer bufferFromStream(ImageInputStream stream) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final byte[] buf = new byte[8192];
    int n;
    while (0 < (n = stream.read(buf))) {
      bos.write(buf, 0, n);
    }
    return ByteBuffer.wrap(bos.toByteArray());
  }

  @Override
  public ImageReadParam getDefaultReadParam() {
    return new TurboJpegImageReadParam();
  }

  @Override
  public int getNumImages(boolean allowSearch) throws IOException {
    return info.getAvailableSizes().size();
  }

  @Override
  public int getWidth(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return info.getAvailableSizes().get(imageIndex).width;
  }

  @Override
  public int getHeight(int imageIndex) throws IOException {
    return info.getAvailableSizes().get(imageIndex).height;
  }

  @Override
  public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
    return Stream.of(TYPE_3BYTE_BGR, TYPE_4BYTE_ABGR, TYPE_4BYTE_ABGR_PRE, TYPE_BYTE_GRAY)
        .map(ImageTypeSpecifier::createFromBufferedImageType)
        .iterator();
  }

  /** Since TurboJPEG can only crop to values divisible by the MCU size, we may need to
   *  expand the cropping area to get a suitable rectangle.
   *
   * @param mcuSize The size of the MCUs
   * @param region The source region to be cropped
   * @return The region that needs to be cropped from the image cropped to the expanded rectangle
   */
  private Rectangle adjustRegion(Dimension mcuSize, Rectangle region, int rotation) throws IOException {
    if (region == null) {
      return null;
    }
    boolean modified = false;
    Rectangle extraCrop = new Rectangle(0, 0, region.width, region.height);
    if (region.width == 0) {
      extraCrop.width = getWidth(0) - region.x;
    }
    if (region.height == 0) {
      extraCrop.height = getHeight(0) - region.y;
    }
    if (region.x % mcuSize.width != 0) {
      extraCrop.x = region.x % mcuSize.width;
      region.x -= extraCrop.x;
      if (region.width > 0) {
        region.width += extraCrop.x;
      }
      modified = true;
    }
    if (region.y % mcuSize.height != 0) {
      extraCrop.y = region.y % mcuSize.height;
      region.y -= extraCrop.y;
      if (region.height > 0) {
        region.height += extraCrop.y;
      }
      modified = true;
    }
    if (region.width % mcuSize.width != 0) {
      region.width = (int) (mcuSize.width*(Math.ceil(region.getWidth() / mcuSize.width)));
      modified = true;
    }
    if (region.height % mcuSize.height != 0) {
      region.height = (int) (mcuSize.height*(Math.ceil(region.getHeight() / mcuSize.height)));
      modified = true;
    }
    if (rotation == 90 || rotation == 270) {
      int w = region.width;
      int h = region.height;
      int x = region.x;
      int y = region.y;
      region.width = h;
      region.height = w;
      region.x = y;
      region.y = x;
      int ew = extraCrop.width;
      int eh = extraCrop.height;
      int ex = extraCrop.x;
      int ey = extraCrop.y;
      extraCrop.width = eh;
      extraCrop.height = ew;
      extraCrop.x = ey;
      extraCrop.y = ex;
    }
    if (modified) {
      return extraCrop;
    } else {
      return null;
    }
  }

  private void adjustExtraCrop(int imageIndex, Info croppedInfo, Rectangle rectangle) {
    double factor = croppedInfo.getAvailableSizes().get(imageIndex).getWidth() / croppedInfo.getAvailableSizes().get(0).getWidth();
    if (factor < 1) {
      rectangle.x = (int) Math.round(factor * rectangle.x);
      rectangle.y = (int) Math.round(factor * rectangle.y);
      rectangle.width = (int) Math.round(factor * rectangle.width);
      rectangle.height = (int) Math.round(factor * rectangle.height);
    }
    int maxWidth = croppedInfo.getAvailableSizes().get(imageIndex).width;
    int maxHeight = croppedInfo.getAvailableSizes().get(imageIndex).height;
    if (rectangle.x + rectangle.width > maxWidth) {
      rectangle.width = maxWidth - rectangle.x;
    }
    if (rectangle.y + rectangle.height > maxHeight) {
      rectangle.height = maxHeight - rectangle.y;
    }
  }

  private void scaleRegion(int targetIndex, Rectangle sourceRegion) throws IOException {
    if (targetIndex == 0) {
      return;
    }
    int nativeWidth = getWidth(0);
    int nativeHeight = getHeight(0);
    double scaleFactor = (double) nativeWidth / (double) getWidth(targetIndex);
    sourceRegion.x = (int) Math.ceil(scaleFactor * sourceRegion.x);
    sourceRegion.y = (int) Math.ceil(scaleFactor * sourceRegion.y);
    sourceRegion.width = Math.min((int) Math.ceil(scaleFactor * sourceRegion.width), nativeWidth - sourceRegion.x);
    sourceRegion.height = Math.min((int) Math.ceil(scaleFactor * sourceRegion.height), nativeHeight - sourceRegion.y);
  }

  @Override
  public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
    checkIndex(imageIndex);
    ByteBuffer data = jpegData;
    try {
      int rotation = 0;
      Rectangle region = null;
      Rectangle extraCrop = null;
      if (param instanceof TurboJpegImageReadParam) {
        rotation = ((TurboJpegImageReadParam) param).getRotationDegree();
      }
      if (param != null && param.getSourceRegion() != null) {
        region = param.getSourceRegion();
        scaleRegion(imageIndex, region);
        if (region.x + region.width == getWidth(0)) {
          region.width = 0;
        }
        if (region.y + region.height == getHeight(0)) {
          region.height = 0;
        }
        if (!isRegionFullImage(imageIndex, region)) {
          extraCrop = adjustRegion(info.getMCUSize(), region, rotation);
        } else {
          region = null;
        }
      }
      if (region != null || rotation != 0) {
        data = lib.transform(data.array(), info, region, rotation);
      }
      Info transformedInfo = lib.getInfo(data.array());
      BufferedImage img = lib.decode(
          data.array(), transformedInfo, transformedInfo.getAvailableSizes().get(imageIndex));
      if (extraCrop != null) {
        adjustExtraCrop(imageIndex, transformedInfo, extraCrop);
        img = img.getSubimage(extraCrop.x, extraCrop.y, extraCrop.width, extraCrop.height);
      }
      return img;
    } catch (TurboJpegException e) {
      throw new IOException(e);
    }
  }

  private boolean isRegionFullImage(int imageIndex, Rectangle region) throws IOException {
    return (region.x == 0 && region.y == 0 && region.width == 0 && region.height == 0);
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
