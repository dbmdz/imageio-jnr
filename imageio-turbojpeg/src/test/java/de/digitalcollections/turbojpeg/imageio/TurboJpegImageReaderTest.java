package de.digitalcollections.turbojpeg.imageio;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TurboJpegImageReaderTest {
  @Test
  public void testReaderIsRegistered() {
    Supplier<Iterator<ImageReader>> getReaderIter = () -> ImageIO.getImageReadersBySuffix("jpg");
    assertThat(getReaderIter.get()).isNotEmpty();
    assertThat(getReaderIter.get()).hasAtLeastOneElementOfType(TurboJpegImageReader.class);
    getReaderIter = () -> ImageIO.getImageReadersByMIMEType("image/jpeg");
    assertThat(getReaderIter.get()).isNotEmpty();
    assertThat(getReaderIter.get()).hasAtLeastOneElementOfType(TurboJpegImageReader.class);
    getReaderIter = () -> ImageIO.getImageReadersByFormatName("jpeg");
    assertThat(getReaderIter.get()).isNotEmpty();
    assertThat(getReaderIter.get()).hasAtLeastOneElementOfType(TurboJpegImageReader.class);
  }

  private TurboJpegImageReader getReader(String fixtureFile) throws IOException {
    File inFile = new File(ClassLoader.getSystemResource(fixtureFile).getFile());
    ImageReader reader = ImageIO.getImageReaders(inFile).next();
    assertThat(reader).isInstanceOf(TurboJpegImageReader.class);
    reader.setInput(ImageIO.createImageInputStream(inFile));
    return (TurboJpegImageReader) reader;
  }

  @Test
  public void testRead() throws IOException {
    ImageReader reader = getReader("rgb.jpg");
    assertThat(reader.getNumImages(true)).isEqualTo(4);
    BufferedImage img = reader.read(0, null);
    assertThat(img.getWidth()).isEqualTo(512);
    assertThat(img.getHeight()).isEqualTo(512);
  }

  @Test
  public void testReadScaled() throws IOException {
    BufferedImage img = getReader("rgb.jpg").read(1, null);
    assertThat(img.getWidth()).isEqualTo(384);
    assertThat(img.getHeight()).isEqualTo(384);
  }

  @Test
  public void testReadRegionAligned() throws IOException {
    ImageReader reader = getReader("crop_aligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(32, 32, 96, 96));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(96);
    assertThat(img.getHeight()).isEqualTo(96);
    assertHasNoWhite(img);
  }

  @Test
  public void testReadRegionUnaligned() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(116, 148, 204, 172));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(204);
    assertThat(img.getHeight()).isEqualTo(172);
    // FIXME: For some reason there are some color inaccuracies in the decoded picture
    assertHasNoWhite(img);
  }

  @Test
  public void testReadUnalignedScaled() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(87, 111, 152, 129));
    BufferedImage img = reader.read(1, param);
    assertThat(img.getWidth()).isEqualTo(152);
    assertThat(img.getHeight()).isEqualTo(129);
    assertHasNoWhite(img);
  }

  @Test
  public void testReadRotated() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setRotationDegree(90);
    BufferedImage img = reader.read(0, param);
    img = img.getSubimage(192, 116, 172, 204);

    // Need to copy the image so we can check the image data
    BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    Graphics g = copy.createGraphics();
    g.drawImage(img, 0, 0, null);
    assertHasNoWhite(img);
  }

  @Test
  public void testReadRegionRotated() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(116, 192, 204, 172));
    param.setRotationDegree(90);
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(172);
    assertThat(img.getHeight()).isEqualTo(204);
    assertHasNoWhite(img);
  }

  @Test
  public void testReadRegionRotatedFullWidth() throws IOException {
    ImageReader reader = getReader("rgb.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, 384, 368));
    param.setRotationDegree(90);
    BufferedImage img = reader.read(1, param);
    assertThat(img.getWidth()).isEqualTo(368);
    assertThat(img.getHeight()).isEqualTo(384);
  }

  @Test
  public void testCanReuseReader() throws IOException {
    ImageReader reader = getReader("rgb.jpg");
    BufferedImage rgbImg = reader.read(1, null);

    reader.setInput(ImageIO.createImageInputStream(
        new File(ClassLoader.getSystemResource("crop_unaligned.jpg").getFile())));
    BufferedImage bwImg = reader.read(1, null);

    assertThat(rgbImg.getRGB(256, 256)).isNotEqualTo(bwImg.getRGB(256, 256));
  }

  @Test
  public void testCropFullWidth() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 192, 521, 172));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(521);
    assertThat(img.getHeight()).isEqualTo(172);
  }

  @Test
  public void testCropFullWidthOffset() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(21, 192, 500, 172));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(500);
    assertThat(img.getHeight()).isEqualTo(172);
  }

  @Test
  public void testCropFullHeight() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(192, 0, 172, 509));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(172);
    assertThat(img.getHeight()).isEqualTo(509);
  }

  @Test
  public void testCropFullHeightOffset() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(192, 9, 172, 500));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(172);
    assertThat(img.getHeight()).isEqualTo(500);
  }

  @Test
  public void testUnalignedCropOnPrimeShaped() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(131, 57, 239, 397));
    BufferedImage img = reader.read(0, param);
    ImageIO.write(img, "PNG", new File("/tmp/debug.png"));
    assertThat(img.getWidth()).isEqualTo(239);
    assertThat(img.getHeight()).isEqualTo(397);
    assertHasNoWhite(img);
  }

  @Test
  public void testCropFullImageScaled() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, reader.getWidth(2), reader.getHeight(2)));
    BufferedImage img = reader.read(2, param);
    assertThat(img.getWidth()).isEqualTo(131);
    assertThat(img.getHeight()).isEqualTo(128);
  }

  private void assertHasNoWhite(BufferedImage img) {
    Set<Integer> pixels = new HashSet<>();
    int w = img.getWidth();
    int h = img.getHeight();
    for (int x=0; x < w; x++) {
      for (int y=0; y < h; y++) {
        pixels.add(img.getRGB(x, y));
      }
    }
    assertThat(pixels).doesNotContain(-1);
  }
}