package de.digitalcollections.turbojpeg.imageio;

import de.digitalcollections.turbojpeg.Info;
import de.digitalcollections.turbojpeg.TurboJpeg;
import de.digitalcollections.turbojpeg.TurboJpegException;
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

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR_PRE;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;

public class TurboJpegImageReader extends ImageReader {
  private ByteBuffer jpegData;
  private Info info;

  protected TurboJpegImageReader(ImageReaderSpi originatingProvider) {
    super(originatingProvider);
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
        info = TurboJpeg.getInfo(jpegData.array());
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
    if (imageIndex > 1) {
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

  private void readData() throws IOException {
    jpegData = bufferFromStream((ImageInputStream) input);
  }

  private void parseInfo() throws IOException {
    if (jpegData == null) {
      readData();
    }
    try {
      info = TurboJpeg.getInfo(jpegData.array());
    } catch (TurboJpegException e) {
      throw new IOException(e);
    }
  }

  @Override
  public int getNumImages(boolean allowSearch) throws IOException {
    return 1;
  }

  @Override
  public int getWidth(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    if (info == null) {
      parseInfo();
    }
    return info.getWidth();
  }

  @Override
  public int getHeight(int imageIndex) throws IOException {
    if (info == null) {
      parseInfo();
    }
    return info.getHeight();
  }

  @Override
  public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
    return Stream.of(TYPE_3BYTE_BGR, TYPE_4BYTE_ABGR, TYPE_4BYTE_ABGR_PRE, TYPE_BYTE_GRAY)
        .map(ImageTypeSpecifier::createFromBufferedImageType)
        .iterator();
  }

  @Override
  public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
    if (jpegData == null) {
      readData();
    }
    ByteBuffer data = jpegData;
    if (info == null) {
      parseInfo();
    }
    try {
      if (param != null && param.getSourceRegion() != null) {
        data = TurboJpeg.transform(data.array(), info, param.getSourceRegion(), 0);
      }
      return TurboJpeg.decode(data.array(), info);
    } catch (TurboJpegException e) {
      throw new IOException(e);
    }
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
