package de.digitalcollections.turbojpeg.imageio;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Supplier;

import static de.digitalcollections.turbojpeg.imageio.CustomAssertions.assertThat;

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
    BufferedImage img = reader.read(0, null);
    assertThat(img).hasDimensions(512, 512);
  }

  @Test
  public void testReadScaled() throws IOException {
    BufferedImage img = getReader("rgb.jpg").read(1, null);
    assertThat(img).hasDimensions(384, 384);
  }

  @Test
  public void testReadRegionAligned() throws IOException {
    ImageReader reader = getReader("crop_aligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(32, 32, 96, 96));
    BufferedImage img = reader.read(0, param);
    assertThat(img)
        .hasDimensions(96, 96)
        .hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadRegionUnaligned() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(116, 148, 204, 172));
    BufferedImage img = reader.read(0, param);
    assertThat(img)
        .hasDimensions(204, 172)
        .hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadUnalignedScaled() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(87, 111, 152, 129));
    BufferedImage img = reader.read(1, param);
    assertThat(img)
        .hasDimensions(152, 129)
        .hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadUnalignedRotated() throws IOException {
    ImageReader reader = getReader("crop_unaligned_rot90.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(16, 16, 339, 319));
    param.setRotationDegree(90);
    BufferedImage img = reader.read(0, param);
    assertThat(img)
        .hasDimensions(319, 339)
        .hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(180);
    img = reader.read(0, param);
    assertThat(img)
        .hasDimensions(339, 319)
        .hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(270);
    img = reader.read(0, param);
    assertThat(img)
        .hasDimensions(319, 339)
        .hasNoPixelsOfColor(-1 /* white */);
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
    assertThat(img).hasNoPixelsOfColor(-1);
  }

  @Test
  public void testReadRegionRotated() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(116, 148, 204, 172));
    param.setRotationDegree(90);
    BufferedImage img = reader.read(0, param);
    assertThat(img)
        .hasDimensions(172, 204)
        .hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(180);
    img = reader.read(0, param);
    assertThat(img)
            .hasDimensions(204, 172)
            .hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(270);
    img = reader.read(0, param);
    assertThat(img)
            .hasDimensions(172, 204)
            .hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadRegionRotatedFullWidth() throws IOException {
    ImageReader reader = getReader("rotated_fullwidth.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, 384, 368));
    param.setRotationDegree(90);
    BufferedImage img = reader.read(1, param);
    assertThat(img)
        .hasDimensions(368, 384)
        .hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(180);
    img = reader.read(1, param);
    assertThat(img)
            .hasDimensions(384, 368)
            .hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(270);
    img = reader.read(1, param);
    assertThat(img)
            .hasDimensions(368, 384)
            .hasNoPixelsOfColor(-1 /* white */);
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
    assertThat(img).hasDimensions(521, 172);
  }

  @Test
  public void testCropFullWidthOffset() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(21, 192, 500, 172));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(500, 172);
  }

  @Test
  public void testCropFullHeight() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(192, 0, 172, 509));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(172, 509);
  }

  @Test
  public void testCropFullHeightOffset() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(192, 9, 172, 500));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(172, 500);
  }

  @Test
  public void testUnalignedCropOnPrimeShaped() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(131, 57, 239, 397));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(239, 397)
            .hasNoPixelsOfColor(-1);
  }

  @Test
  public void testCropFullImageScaled() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, reader.getWidth(2), reader.getHeight(2)));
    BufferedImage img = reader.read(2, param);
    assertThat(img).hasDimensions(reader.getWidth(2), reader.getHeight(2));
  }

  @Test
  public void testReadTinyImage() throws IOException {
    ImageReader reader = getReader("tiny.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, reader.getWidth(0), reader.getHeight(0)));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(1);
    assertThat(img.getHeight()).isEqualTo(1);

  }
}