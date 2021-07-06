package de.digitalcollections.turbojpeg.imageio;

import static de.digitalcollections.turbojpeg.imageio.CustomAssertions.assertThat;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.junit.jupiter.api.Test;

/**
 * NOTE: These tests can only be superficial, since TurboJPEG is merely a wrapping API for a number
 * of different JPEG implementations and versions, which is why we cannot make bit-accurate
 * assertions on the generated data.
 */
class TurboJpegImageWriterTest {
  @Test
  void writerIsDiscoverable() {
    assertThat(Lists.newArrayList(ImageIO.getImageWritersByFormatName("jpeg")))
        .hasAtLeastOneElementOfType(TurboJpegImageWriter.class);
    assertThat(Lists.newArrayList(ImageIO.getImageWritersByMIMEType("image/jpeg")))
        .hasAtLeastOneElementOfType(TurboJpegImageWriter.class);
  }

  @Test
  public void testEncode() throws IOException {
    ImageWriter writer =
        Streams.stream(ImageIO.getImageWritersByFormatName("jpeg"))
            .filter(TurboJpegImageWriter.class::isInstance)
            .findFirst()
            .orElseThrow(RuntimeException::new);
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

  @Test
  public void testEncodeBinary() throws Exception {
    ImageWriter writer =
        Streams.stream(ImageIO.getImageWritersByFormatName("jpeg"))
            .filter(TurboJpegImageWriter.class::isInstance)
            .findFirst()
            .orElseThrow(RuntimeException::new);
    ImageWriteParam param = writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(0.85f);
    BufferedImage in = ImageIO.read(ClassLoader.getSystemResource("binary.tif"));
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
      writer.setOutput(ios);
      writer.write(null, new IIOImage(in, null, null), param);
    }
    os.flush();
    assertThat(os.toByteArray()).isNotEmpty();
    ByteArrayInputStream bis = new ByteArrayInputStream(os.toByteArray());
    BufferedImage jpegImg = ImageIO.read(bis);
    assertThat(jpegImg.getWidth()).isEqualTo(in.getWidth());
    assertThat(jpegImg.getHeight()).isEqualTo(in.getHeight());
    int[] pixBuf = new int[jpegImg.getWidth() * jpegImg.getHeight()];
    jpegImg.getData().getPixels(0, 0, jpegImg.getWidth(), jpegImg.getHeight(), pixBuf);
    assertThat(pixBuf).contains(0, 255);
  }

  @Test
  public void testCanReuseWriter() throws IOException {
    ImageWriter writer =
        Streams.stream(ImageIO.getImageWritersByFormatName("jpeg"))
            .filter(TurboJpegImageWriter.class::isInstance)
            .findFirst()
            .orElseThrow(RuntimeException::new);

    BufferedImage in = ImageIO.read(ClassLoader.getSystemResource("rgb.jpg"));
    ByteArrayOutputStream rgb = new ByteArrayOutputStream();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(rgb)) {
      writer.setOutput(ios);
      writer.write(null, new IIOImage(in, null, null), null);
    }
    rgb.flush();

    in = ImageIO.read(ClassLoader.getSystemResource("crop_aligned.jpg"));
    ByteArrayOutputStream bw = new ByteArrayOutputStream();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(bw)) {
      writer.setOutput(ios);
      writer.write(null, new IIOImage(in, null, null), null);
    }
    bw.flush();

    assertThat(rgb.toByteArray()).isNotEqualTo(bw.toByteArray());
  }

  @Test
  public void testEncodeGrayscale() throws IOException {
    ImageWriter writer =
        Streams.stream(ImageIO.getImageWritersByFormatName("jpeg"))
            .filter(TurboJpegImageWriter.class::isInstance)
            .findFirst()
            .orElseThrow(RuntimeException::new);
    ImageWriteParam param = writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(0.85f);
    BufferedImage in = ImageIO.read(ClassLoader.getSystemResource("grayscale.jpg"));
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
      writer.setOutput(ios);
      writer.write(null, new IIOImage(in, null, null), param);
    }
    os.flush();
    Files.write(Paths.get("/tmp/debug.jpg"), os.toByteArray());
    assertThat(os.toByteArray()).isNotEmpty();
    ByteArrayInputStream bis = new ByteArrayInputStream(os.toByteArray());
    BufferedImage jpegImg = ImageIO.read(bis);
    assertThat(jpegImg).hasNoPixelsOfColor(255);
  }
}
