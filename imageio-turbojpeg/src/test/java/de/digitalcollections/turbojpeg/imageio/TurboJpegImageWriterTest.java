package de.digitalcollections.turbojpeg.imageio;

import com.google.common.collect.Streams;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** NOTE: This tests can only be superficial, since TurboJPEG is merely a wrapping API for a number
 *  of different JPEG implementations and versions, which is why we cannot make bit-accurate assertions on the
 *  generated data. */
class TurboJpegImageWriterTest {
  @Test
  void writerIsDiscoverable() {
    assertThat(ImageIO.getImageWritersByFormatName("jpeg")).hasAtLeastOneElementOfType(TurboJpegImageWriter.class);
    assertThat(ImageIO.getImageWritersByMIMEType("image/jpeg")).hasAtLeastOneElementOfType(TurboJpegImageWriter.class);
  }

  @Test
  public void testEncode() throws IOException {
    ImageWriter writer = Streams.stream(ImageIO.getImageWritersByFormatName("jpeg"))
        .filter(TurboJpegImageWriter.class::isInstance)
        .findFirst().get();
    ImageWriteParam param = writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(0.85f);
    BufferedImage in = ImageIO.read(ClassLoader.getSystemResource("crop_aligned.jpg"));
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
      writer.setOutput(ios);
      writer.write(null, new IIOImage(in, null, null), param);
    }
    os.flush();
    assertThat(os.toByteArray()).isNotEmpty();
  }
}