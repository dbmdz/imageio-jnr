package de.digitalcollections.openjpeg.imageio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

class OpenJp2ImageReaderTest {

  @Test
  public void testReaderIsRegistered() {
    assertThat(Lists.newArrayList(ImageIO.getImageReadersBySuffix("jp2"))).isNotEmpty();
  }

  private OpenJp2ImageReader getReader(String fixtureFile) throws IOException {
    File inFile = new File(ClassLoader.getSystemResource(fixtureFile).getFile());
    ImageReader reader = ImageIO.getImageReaders(inFile).next();
    assertThat(reader).isInstanceOf(OpenJp2ImageReader.class);

    reader.setInput(ImageIO.createImageInputStream(inFile));
    return (OpenJp2ImageReader) reader;
  }

  @Test
  public void testReadRGB() throws Exception {
    OpenJp2ImageReader reader = getReader("rgb.jp2");
    BufferedImage img = reader.read(0, null);
    assertThat(img.getType()).isEqualTo(BufferedImage.TYPE_3BYTE_BGR);
    assertThat(img.getWidth()).isEqualTo(512);
    assertThat(img.getHeight()).isEqualTo(512);
  }

  @Test
  public void testReadRGBScaled() throws Exception {
    OpenJp2ImageReader reader = getReader("rgb.jp2");
    ImageReadParam param = reader.getDefaultReadParam();
    BufferedImage img = reader.read(1, param);
    assertThat(img.getWidth()).isEqualTo(256);
    assertThat(img.getHeight()).isEqualTo(256);
  }

  @Test
  public void testReadRGBTile() throws Exception {
    OpenJp2ImageReader reader = getReader("hires.jp2");
    int tileWidth = reader.getTileWidth(0);
    int tileHeight = reader.getTileHeight(0);
    assertThat(tileWidth).isEqualTo(1024);
    assertThat(tileHeight).isEqualTo(1024);

    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(tileWidth, tileHeight));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(1024);
    assertThat(img.getHeight()).isEqualTo(1024);
  }

  @Test
  public void testReadRGBTileScaled() throws Exception {
    OpenJp2ImageReader reader = getReader("hires.jp2");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(1024, 1024, 512, 512));
    BufferedImage img = reader.read(1, param);
    assertThat(img.getWidth()).isEqualTo(512);
    assertThat(img.getHeight()).isEqualTo(512);
  }

  @Test
  public void testReadRGBTileUnaligned() throws Exception {
    OpenJp2ImageReader reader = getReader("hires.jp2");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(1512, 983, 1284, 768));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(1284);
    assertThat(img.getHeight()).isEqualTo(768);
  }

  @Test
  public void testReadGrayScale() throws Exception {
    OpenJp2ImageReader reader = getReader("gray.jp2");
    BufferedImage img = reader.read(0, null);
    assertThat(img.getType()).isEqualTo(BufferedImage.TYPE_BYTE_GRAY);
    assertThat(img.getWidth()).isEqualTo(512);
    assertThat(img.getHeight()).isEqualTo(512);
    InputStream input = ClassLoader.getSystemResourceAsStream("gray_control.png");
    assertThat(input).isNotNull();
    BufferedImage controlImg = ImageIO.read(input);
    assertImageEquals(controlImg, img);
  }

  @Test
  public void testReadBinary() throws Exception {
    OpenJp2ImageReader reader = getReader("binary.jp2");
    BufferedImage img = reader.read(0, null);
    assertThat(img.getWidth()).isEqualTo(7216);
    assertThat(img.getHeight()).isEqualTo(4910);
    assertThat(img.getType()).isEqualTo(BufferedImage.TYPE_BYTE_BINARY);
    InputStream input = ClassLoader.getSystemResourceAsStream("binary_control.png");
    assertThat(input).isNotNull();
    BufferedImage controlImg = ImageIO.read(input);
    assertImageEquals(controlImg, img);
  }

  @Test
  public void testCanReuseReader() throws IOException {
    ImageReader reader = getReader("rgb.jp2");
    BufferedImage rgbImg = reader.read(0, null);

    reader.setInput(
        ImageIO.createImageInputStream(
            new File(ClassLoader.getSystemResource("hires.jp2").getFile())));
    BufferedImage bwImg = reader.read(0, null);

    assertThat(rgbImg.getRGB(256, 256)).isNotEqualTo(bwImg.getRGB(256, 256));
  }

  private void assertImageEquals(BufferedImage expected, BufferedImage actual) {
    int width = expected.getWidth();
    assertEquals(width, actual.getWidth());
    int height = expected.getHeight();
    assertEquals(height, actual.getHeight());
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int expectedPixel = expected.getRGB(x, y);
        int actualPixel = actual.getRGB(x, y);
        if (expectedPixel >> 24 == 0 && actualPixel >> 24 == 0) {
          // transparent
          continue;
        }
        assertEquals(expectedPixel, actualPixel, "RGB of Pixel " + x + "," + y);
      }
    }
  }

  private void assertImageEquals(String expectedImageName, String actualImageName)
      throws IOException {
    OpenJp2ImageReader reader = getReader(actualImageName);
    BufferedImage actualImage = reader.read(0, null);

    InputStream input = ClassLoader.getSystemResourceAsStream(expectedImageName);
    assertThat(input).isNotNull();
    BufferedImage expectedImg = ImageIO.read(input);

    assertImageEquals(expectedImg, actualImage);
  }

  public static void compareImagesByRaster(BufferedImage expectedImage, BufferedImage actualImage,
      Raster actualRaster, int tolerance) {
    int width = expectedImage.getWidth();
    assertEquals(width, actualImage.getWidth());
    int height = expectedImage.getHeight();
    assertEquals(height, actualImage.getHeight());

    Raster expectedRaster = expectedImage.getData();
    int bands = expectedRaster.getNumBands();

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int totalDiff = 0;
        for (int b = 0; b < bands; b++) {
          int expectedSample = expectedRaster.getSample(x, y, b);
          int actualSample = actualRaster.getSample(x, y, b);
          totalDiff += Math.abs(expectedSample - actualSample);
        }
        if (totalDiff > tolerance) {
          assertEquals(totalDiff, tolerance, "Tolerance of Raster " + x + "," + y);
        }
      }
    }
  }

  private void assertRasterEquals(String expectedImageName, String actualImageName)
      throws IOException {
    assertRasterEquals(expectedImageName, actualImageName, 5);
  }

  private void assertRasterEquals(String expectedImageName, String actualImageName, int tolerance)
      throws IOException {
    OpenJp2ImageReader reader = getReader(actualImageName);
    BufferedImage actualImage = reader.read(0, null);
    reader = getReader(actualImageName);
    Raster actualRaster = reader.readRaster(0, null);
    InputStream input = ClassLoader.getSystemResourceAsStream(expectedImageName);
    assertThat(input).isNotNull();
    BufferedImage expectedImg = ImageIO.read(input);

    compareImagesByRaster(expectedImg, actualImage, actualRaster, tolerance);
  }

  @Test
  public void testReadRGBA() throws Exception {
    assertImageEquals("rgba.png", "rgba.jp2");
    assertRasterEquals("rgba.png", "rgba.jp2");
  }

  @Test
  public void testReadCMYK() throws Exception {
    assertImageEquals("cmyk.png", "cmyk.jp2");
    // need higher tolerance due to lower image quality which will appear in raster sample
    assertRasterEquals("cmyk.png", "cmyk.jp2", 1500);
  }

  @Test
  public void testReadCMYK_withAlpha() throws Exception {
    assertImageEquals("cmykWithAlpha.png", "cmykWithAlpha.jp2");
    // need higher tolerance due to lower image quality which will appear in raster sample
    assertRasterEquals("cmykWithAlpha.png", "cmykWithAlpha.jp2",1500);
  }

  @Test
  public void testReadGrayWithAlpha() throws Exception {
    assertImageEquals("grayWithAlpha.png", "grayWithAlpha.jp2");
    assertRasterEquals("grayWithAlpha.png", "grayWithAlpha.jp2");
  }

  @Test
  public void testReadGray16bitWithoutAlpha() throws Exception {
    assertImageEquals("gray16bitWithoutAlpha.png", "gray16bitWithoutAlpha.jp2");
    assertRasterEquals("gray16bitWithoutAlpha.png", "gray16bitWithoutAlpha.jp2");
  }

  @Test
  public void testReadYUV() throws Exception {
    OpenJp2ImageReader reader = getReader("yuv.jp2");
    try {
      reader.read(0, null);
    } catch (IOException ex) {
      assertEquals("Images with YUV color space are currently not supported.", ex.getMessage());
    }
  }

  @Test
  public void testReadWeirdGrayscale() throws IOException {
    assertImageEquals("gray2_control.png", "gray2.jp2");
    assertRasterEquals("gray2_control.png", "gray2.jp2");
  }
}
