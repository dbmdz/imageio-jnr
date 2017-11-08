package de.digitalcollections.turbojpeg.imageio;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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
    assertThat(((DataBufferByte) img.getData().getDataBuffer()).getData()).doesNotContain(-1);
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
    assertThat(((DataBufferByte) img.getData().getDataBuffer()).getData()).doesNotContain(-1);
  }

  @Test
  public void testReadUnalignedScaled() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(29, 37, 51, 43));
    BufferedImage img = reader.read(2, param);
    assertThat(img.getWidth()).isEqualTo(51);
    assertThat(img.getHeight()).isEqualTo(43);
    assertThat(((DataBufferByte) img.getData().getDataBuffer()).getData()).doesNotContain(-1);
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
    assertThat(((DataBufferByte) copy.getData().getDataBuffer()).getData()).doesNotContain(-1);
  }
}