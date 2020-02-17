package de.digitalcollections.turbojpeg.imageio;

import java.awt.image.BufferedImage;
import org.assertj.core.api.Assertions;

class CustomAssertions extends Assertions {
  public static BufferedImageAssert assertThat(BufferedImage actual) {
    return new BufferedImageAssert(actual);
  }
}
