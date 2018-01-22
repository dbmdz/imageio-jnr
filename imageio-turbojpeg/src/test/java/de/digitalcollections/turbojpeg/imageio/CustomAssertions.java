package de.digitalcollections.turbojpeg.imageio;

import org.assertj.core.api.Assertions;

import java.awt.image.BufferedImage;

class CustomAssertions extends Assertions {
  public static BufferedImageAssert assertThat(BufferedImage actual) {
    return new BufferedImageAssert(actual);
  }
}
