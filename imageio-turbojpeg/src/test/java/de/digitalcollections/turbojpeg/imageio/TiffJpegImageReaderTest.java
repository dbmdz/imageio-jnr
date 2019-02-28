package de.digitalcollections.turbojpeg.imageio;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TiffJpegImageReaderTest {
  ImageReader getReader() throws IOException {
    File inFile = new File(ClassLoader.getSystemResource("jpeg.tif").getFile());
    ImageInputStream is = ImageIO.createImageInputStream(inFile);
    Iterator<ImageReader> readers = ImageIO.getImageReaders(is);
    readers.next();
    ImageReader reader = readers.next();
    reader.setInput(is);
    return reader;
  }

  @Test
  public void testReadFull() throws IOException {
    ImageReader reader = getReader();
    BufferedImage image = reader.read(0);
    assertThat(image.getWidth()).isEqualTo(2064);
    assertThat(image.getHeight()).isEqualTo(2553);
    image = reader.read(1);
    assertThat(image.getWidth()).isEqualTo(1032);
    assertThat(image.getHeight()).isEqualTo(1276);
    image = reader.read(2);
    assertThat(image.getWidth()).isEqualTo(516);
    assertThat(image.getHeight()).isEqualTo(638);
    image = reader.read(3);
    assertThat(image.getWidth()).isEqualTo(258);
    assertThat(image.getHeight()).isEqualTo(319);
    image = reader.read(4);
    assertThat(image.getWidth()).isEqualTo(129);
    assertThat(image.getHeight()).isEqualTo(159);
    image = reader.read(5);
    assertThat(image.getWidth()).isEqualTo(64);
    assertThat(image.getHeight()).isEqualTo(79);
  }

  @Test
  public void testReadRegion() throws IOException {
    ImageReader reader = getReader();
    ImageReadParam param = new ImageReadParam();
    param.setSourceRegion(new Rectangle(512, 512, 512, 512));
    BufferedImage img = reader.read(0, param);
    assertThat(img.getWidth()).isEqualTo(512);
    assertThat(img.getHeight()).isEqualTo(512);
  }
}
