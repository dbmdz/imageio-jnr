package de.digitalcollections.openjpeg.imageio;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;

import de.digitalcollections.openjpeg.OpenJpeg;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("checkstyle:constantname")
public class OpenJp2ImageWriterSpi extends ImageWriterSpi {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenJp2ImageWriterSpi.class);
  private static final String vendorName =
      "Münchener Digitalisierungszentrum/Digitale Bibliothek, Bayerische Staatsbibliothek";
  private static final String version = "0.2.6";
  private static final String writerClassName =
      "de.digitalcollections.openjpeg.imageio.OpenJp2ImageWriter";
  private static final String[] names = {"jpeg2000"};
  private static final String[] suffixes = {"jp2"};
  private static final String[] MIMETypes = {"image/jp2"};
  private static final String[] readerSpiNames = {
    "de.digitalcollections.openjpeg.imageio.OpenJp2ImageWriterSpi"
  };
  private static final Class<?>[] outputTypes = {ImageOutputStream.class};

  private OpenJpeg lib;

  /** Build the SPI, boilerplate. */
  public OpenJp2ImageWriterSpi() {
    super(
        vendorName,
        version,
        names,
        suffixes,
        MIMETypes,
        writerClassName,
        outputTypes,
        readerSpiNames,
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
  public boolean canEncodeImage(ImageTypeSpecifier type) {
    // TODO: Implement alpha support
    return ((type.getNumBands() == 3 && type.getBufferedImageType() == TYPE_3BYTE_BGR)
        || (type.getNumBands() == 1 && type.getBufferedImageType() == TYPE_BYTE_GRAY));
  }

  @Override
  public ImageWriter createWriterInstance(Object extension) throws IOException {
    this.loadLibrary();
    return new OpenJp2ImageWriter(this, lib);
  }

  @Override
  public String getDescription(Locale locale) {
    return "JPEG2000 reader plugin based on the OpenJp2 library from the OpenJPEG project.";
  }
}
