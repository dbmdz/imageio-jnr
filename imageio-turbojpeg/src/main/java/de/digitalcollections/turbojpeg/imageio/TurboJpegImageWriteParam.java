package de.digitalcollections.turbojpeg.imageio;

import javax.imageio.ImageWriteParam;

public class TurboJpegImageWriteParam extends ImageWriteParam {

  @Override
  public boolean canWriteCompressed() {
    return true;
  }

  @Override
  public boolean isCompressionLossless() {
    return false;
  }
}
