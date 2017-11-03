package de.digitalcollections.turbojpeg.imageio;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class TurboJpegImageReaderSpi extends ImageReaderSpi {
  private static byte[] HEADER_MAGIC = new byte[]{(byte) 0xff, (byte) 0xd8};

  private static final String vendorName = "Münchener Digitalisierungszentrum/Digitale Bibliothek, Bayerische Staatsbibliothek";
  private static final String version = "0.1.0";
  private static final String readerClassName = "de.digitalcollections.openjpeg.turbojpeg.TurboJpegImageReader";
  private static final String[] names = { "JPEG", "jpeg", "JPG", "jpg" };
  private static final String[] suffixes = { "jpg", "jpeg" };
  private static final String[] MIMETypes = { "image/jpeg" };
  private static final String[] writerSpiNames = { "de.digitalcollections.turbojpeg.imageio.TurboJpegImageWriterSpi" };

  public TurboJpegImageReaderSpi() {
    super(vendorName, version, names, suffixes, MIMETypes, readerClassName,
          new Class[] { ImageInputStream.class }, writerSpiNames,
        false, null, null,
        null, null,  false,
        null, null, null,
        null);
  }

  @Override
  public boolean canDecodeInput(Object input) throws IOException {
    if (!(input instanceof ImageInputStream)) {
      input = ImageIO.createImageInputStream(input);
    }
    if (input == null) {
      return false;
    }
    ImageInputStream stream = (ImageInputStream)input;
    byte[] b = new byte[12];
    try {
      stream.mark();
      stream.readFully(b);
    } catch (IOException e) {
      return false;
    }
    return Arrays.equals(b, HEADER_MAGIC);
  }

  @Override
  public ImageReader createReaderInstance(Object extension) throws IOException {
    return new TurboJpegImageReader(this);
  }

  @Override
  public String getDescription(Locale locale) {
    return "JPEG reader plugin based on libjpeg-turbo/turbojpeg.";
  }
}
