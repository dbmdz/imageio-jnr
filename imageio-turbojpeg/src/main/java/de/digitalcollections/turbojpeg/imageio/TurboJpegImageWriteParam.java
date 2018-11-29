package de.digitalcollections.turbojpeg.imageio;

import java.util.Locale;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

public class TurboJpegImageWriteParam extends JPEGImageWriteParam {

  public TurboJpegImageWriteParam(Locale locale) {
    super(locale);
  }

  @Override
  public boolean canWriteCompressed() {
    return true;
  }

  @Override
  public boolean isCompressionLossless() {
    return false;
  }
}
