package de.digitalcollections.turbojpeg.imageio;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;

import de.digitalcollections.turbojpeg.TurboJpeg;
import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("checkstyle:constantname")
public class TurboJpegImageWriterSpi extends ImageWriterSpi {

  private static final Logger LOGGER = LoggerFactory.getLogger(TurboJpegImageWriterSpi.class);
  private static final String vendorName =
      "Münchener Digitalisierungszentrum/Digitale Bibliothek, Bayerische Staatsbibliothek";
  private static final String version = "0.2.6";
  private static final String writerClassName =
      "de.digitalcollections.openjpeg.turbojpeg.TurboJpegImageWriter";
  private static final String[] names = {"JPEG", "jpeg", "JPG", "jpg"};
  private static final String[] suffixes = {"jpg", "jpeg"};
  private static final String[] MIMETypes = {"image/jpeg"};
  private static final String[] readerSpiNames = {
    "de.digitalcollections.turbojpeg.imageio.TurboJpegImageReaderSpi"
  };
  private static final Class[] outputTypes = {ImageOutputStream.class};

  private TurboJpeg lib;

  /** Construct the SPI. Boilerplate. */
  public TurboJpegImageWriterSpi() {
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
        this.lib = new TurboJpeg();
      } catch (UnsatisfiedLinkError e) {
        LOGGER.warn("Could not load libturbojpeg, plugin will be disabled");
        throw new IOException(e);
      }
    }
  }

  /** Instruct registry to prioritize this WriterSpi over other JPEG writers. * */
  @SuppressWarnings("unchecked")
  @Override
  public void onRegistration(final ServiceRegistry registry, final Class<?> category) {
    Stream.of(
            "com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageWriterSpi",
            "com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi")
        .forEach(
            (clsName) -> {
              try {
                ImageWriterSpi defaultProvider =
                    (ImageWriterSpi) registry.getServiceProviderByClass(Class.forName(clsName));
                registry.setOrdering((Class<ImageWriterSpi>) category, this, defaultProvider);
              } catch (ClassNotFoundException e) {
                // NOP
              }
            });
  }

  @Override
  public boolean canEncodeImage(ImageTypeSpecifier type) {
    // TODO: Support all image types, if necessary convert before encoding
    int bufferedImageType = type.getBufferedImageType();
    return (type.getNumBands() == 3 || type.getNumBands() == 1)
        && (bufferedImageType == TYPE_3BYTE_BGR
            || bufferedImageType == TYPE_4BYTE_ABGR
            || bufferedImageType == TYPE_BYTE_GRAY);
  }

  @Override
  public ImageWriter createWriterInstance(Object extension) throws IOException {
    this.loadLibrary();
    return new TurboJpegImageWriter(this, lib);
  }

  @Override
  public String getDescription(Locale locale) {
    return "JPEG writer plugin based on libjpeg-turbo/turbojpeg.";
  }
}
