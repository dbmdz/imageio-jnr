package de.digitalcollections.turbojpeg.imageio;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class TurboJpegImageWriterSpi extends ImageWriterSpi {
  private static final String vendorName = "Münchener Digitalisierungszentrum/Digitale Bibliothek, Bayerische Staatsbibliothek";
  private static final String version = "0.1.0";
  private static final String writerClassName = "de.digitalcollections.openjpeg.turbojpeg.TurboJpegImageWriter";
  private static final String[] names = { "JPEG", "jpeg", "JPG", "jpg" };
  private static final String[] suffixes = { "jpg", "jpeg" };
  private static final String[] MIMETypes = { "image/jpeg" };
  private static final String[] readerSpiNames = { "de.digitalcollections.turbojpeg.imageio.TurboJpegImageReaderSpi" };
  private static final Class[] outputTypes = { ImageOutputStream.class };


  public TurboJpegImageWriterSpi() {
    super(vendorName, version, names, suffixes, MIMETypes, writerClassName, outputTypes, readerSpiNames,
        false, null, null,
        null, null, false,
        null, null, null, null);
  }

  @Override
  public boolean canEncodeImage(ImageTypeSpecifier type) {
    return false;
  }

  @Override
  public ImageWriter createWriterInstance(Object extension) throws IOException {
    return null;
  }

  @Override
  public String getDescription(Locale locale) {
    return null;
  }
}
