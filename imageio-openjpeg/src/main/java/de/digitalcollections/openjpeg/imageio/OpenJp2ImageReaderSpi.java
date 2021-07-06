package de.digitalcollections.openjpeg.imageio;

import de.digitalcollections.openjpeg.OpenJpeg;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("checkstyle:constantname")
public class OpenJp2ImageReaderSpi extends ImageReaderSpi {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenJp2ImageReaderSpi.class);
  private static final byte[] HEADER_MAGIC =
      new byte[] {0x00, 0x00, 0x00, 0x0c, 0x6a, 0x50, 0x20, 0x20, 0x0d, 0x0a, (byte) 0x87, 0x0a};
  private static final String vendorName =
      "Münchener Digitalisierungszentrum/Digitale Bibliothek, Bayerische Staatsbibliothek";
  private static final String version = "0.2.6";
  private static final String readerClassName =
      "de.digitalcollections.openjpeg.imageio.OpenJp2ImageReader";
  private static final String[] names = {"jpeg2000"};
  private static final String[] suffixes = {"jp2"};
  private static final String[] MIMETypes = {"image/jp2"};
  private static final String[] writerSpiNames = {
    "de.digitalcollections.openjpeg.imageio.OpenJp2ImageWriterSpi"
  };
  private static final Class<?>[] inputTypes = {ImageInputStream.class};

  private OpenJpeg lib;

  /** Construct the SPI, boilerplate. */
  public OpenJp2ImageReaderSpi() {
    super(
        vendorName,
        version,
        names,
        suffixes,
        MIMETypes,
        readerClassName,
        inputTypes,
        writerSpiNames,
        false,
        null,
        null,
        null,
        null,
        false,
        null,
        null,
        null,
        null);
  }

  private void loadLibrary() throws IOException {
    if (this.lib == null) {
      try {
        this.lib = new OpenJpeg();
      } catch (UnsatisfiedLinkError e) {
        LOGGER.warn("Could not load libopenjp2, plugin will be disabled");
        throw new IOException(e);
      }
    }
  }

  @Override
  public boolean canDecodeInput(Object input) throws IOException {
    if (!(input instanceof ImageInputStream)) {
      input = ImageIO.createImageInputStream(input);
    }
    if (input == null) {
      return false;
    }
    ImageInputStream stream = (ImageInputStream) input;
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
    loadLibrary();
    return new OpenJp2ImageReader(this, this.lib);
  }

  @Override
  public String getDescription(Locale locale) {
    return "JPEG2000 reader plugin based on the OpenJp2 library from the OpenJPEG project.";
  }
}
