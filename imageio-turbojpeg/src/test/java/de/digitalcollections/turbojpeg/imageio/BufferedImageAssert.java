package de.digitalcollections.turbojpeg.imageio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.assertj.core.api.AbstractAssert;

class BufferedImageAssert extends AbstractAssert<BufferedImageAssert, BufferedImage> {
  public BufferedImageAssert(BufferedImage actual) {
    super(actual, BufferedImageAssert.class);
  }

  private String writeDebugImage() {
    try {
      File temp = File.createTempFile("imageAssert", ".png");
      ImageIO.write(actual, "PNG", temp);
      return "\nDebug image was written to " + temp.getAbsolutePath();
    } catch (IOException e) {
      System.err.printf("Could not write debug image: %s%n", e);
      return "";
    }
  }

  public static BufferedImageAssert assertThat(BufferedImage actual) {
    return new BufferedImageAssert(actual);
  }

  public BufferedImageAssert hasNoPixelsOfColor(int rgbColor) {
    isNotNull();
    int w = actual.getWidth();
    int h = actual.getHeight();
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        if (actual.getRGB(x, y) == rgbColor) {
          String debugFilename = null;
          failWithMessage(
              "Expected no pixels of color %d, but pixel at position (%d, %d) has color.%s",
              rgbColor, x, y, writeDebugImage());
        }
      }
    }
    return this;
  }

  public BufferedImageAssert hasDimensions(int width, int height) {
    isNotNull();
    if (actual.getWidth() != width || actual.getHeight() != height) {
      failWithMessage(
          "Expected image to be of size %dx%d, but is %dx%d.%s",
          width, height, actual.getWidth(), actual.getHeight(), writeDebugImage());
    }
    return this;
  }

  public BufferedImageAssert isEqualTo(BufferedImage other) {
    int width = other.getWidth();
    assertEquals(width, actual.getWidth());
    int height = other.getHeight();
    assertEquals(height, actual.getHeight());
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int expectedPixel = other.getRGB(x, y);
        int actualPixel = actual.getRGB(x, y);
        if (expectedPixel >> 24 == 0 && actualPixel >> 24 == 0) {
          // transparent
          continue;
        }
        if (expectedPixel != actualPixel) {
          failWithMessage(
              "Expected pixel with color '%s' at x=%d, y=%d, got '%s'",
              expectedPixel, x, y, actualPixel);
        }
      }
    }
    return this;
  }
}
