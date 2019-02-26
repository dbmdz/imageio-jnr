package de.digitalcollections.openjpeg.imageio;

import de.digitalcollections.openjpeg.OpenJpeg;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class OpenJp2ImageWriterTest {
  private OpenJp2ImageWriter writer;

  private static byte[] sha1digest(File file) throws Exception {
    return sha1digest(new FileInputStream(file));
  }

  private static byte[] sha1digest(InputStream is) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-1");
    int n = 0;
    byte[] buffer = new byte[8192];
    while (n != -1) {
      n = is.read(buffer);
      if (n > 0) {
        digest.update(buffer, 0, n);
      }
    }
    return digest.digest();
  }

  @BeforeEach
  void beforeEach() {
    OpenJpeg lib = new OpenJpeg();
    // We need to pin the version so we can reliably compare checksums
    if (!lib.lib.opj_version().equals("2.3.0")) {
      throw new RuntimeException(String.format(
          "Writer tests must be run with version 2.3.0 of the shared library (installed version is %s)",
          lib.lib.opj_version()));
    }
    writer = (OpenJp2ImageWriter) ImageIO.getImageWritersByFormatName("jpeg2000").next();
  }

  private void compressAndCompare(String fixtureName, ImageWriteParam param) throws Exception {
    File expected = new File(ClassLoader.getSystemResource("expected/" + fixtureName).getFile());
    BufferedImage in = ImageIO.read(ClassLoader.getSystemResource("lenna.png"));
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
      writer.setOutput(ios);
      writer.write(null, new IIOImage(in, null, null), param);
    }
    os.flush();
    assertThat(sha1digest(new ByteArrayInputStream(os.toByteArray()))).isEqualTo(sha1digest(expected));
  }

  @Test
  void writerIsDiscoverable() {
    assertThat(Lists.newArrayList(ImageIO.getImageWritersByFormatName("jpeg2000"))).hasAtLeastOneElementOfType(OpenJp2ImageWriter.class);
    assertThat(Lists.newArrayList(ImageIO.getImageWritersByMIMEType("image/jp2"))).hasAtLeastOneElementOfType(OpenJp2ImageWriter.class);
    assertThat(Lists.newArrayList(ImageIO.getImageWritersBySuffix("jp2"))).hasAtLeastOneElementOfType(OpenJp2ImageWriter.class);
  }

  @Test
  void writeLosslessUntiled() throws Exception {
    // = opj_compress
    OpenJp2ImageWriteParam param = (OpenJp2ImageWriteParam) writer.getDefaultWriteParam();
    compressAndCompare("lenna_defaults.jp2", param);
  }

  @Test
  void writeGreyscale() throws Exception {
    File expected = new File(ClassLoader.getSystemResource("expected/lenna_gray.jp2").getFile());
    BufferedImage in = ImageIO.read(ClassLoader.getSystemResource("lenna_gray.png"));
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    OpenJp2ImageWriteParam param = (OpenJp2ImageWriteParam) writer.getDefaultWriteParam();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
      writer.setOutput(ios);
      writer.write(null, new IIOImage(in, null, null), param);
    }
    os.flush();
    assertThat(sha1digest(new ByteArrayInputStream(os.toByteArray()))).isEqualTo(sha1digest(expected));
  }

  @Test
  void writeLossyUntiledDefaultQuality() throws Exception {
    // = opj_compress -I
    OpenJp2ImageWriteParam param = (OpenJp2ImageWriteParam) writer.getDefaultWriteParam();
    param.setCompressionType("lossy");
    compressAndCompare("lenna_lossy.jp2", param);
  }

  @Test
  void writeLossyUntiledCustomQuality() throws Exception {
    // = opj_compress -I -r 10
    OpenJp2ImageWriteParam param = (OpenJp2ImageWriteParam) writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(0.9f);
    compressAndCompare("lenna_lossy_r10.jp2", param);
  }

  @Test
  void writeLossyTiledCustomQuality() throws Exception {
    // = opj_compress -I -r 10 -t 128,128
    OpenJp2ImageWriteParam param = (OpenJp2ImageWriteParam) writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(0.9f);
    param.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
    param.setTiling(128, 128, 0, 0);
    compressAndCompare("lenna_tiled_lossy_r10.jp2", param);
  }

  @Test
  public void testCanReuseWriter() throws IOException {
    BufferedImage in = ImageIO.read(ClassLoader.getSystemResource("lenna.png"));
    ByteArrayOutputStream lenna = new ByteArrayOutputStream();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(lenna)) {
      writer.setOutput(ios);
      writer.write(null, new IIOImage(in, null, null), null);
    }
    lenna.flush();

    in = ImageIO.read(ClassLoader.getSystemResource("hires.jp2"));
    ByteArrayOutputStream hires = new ByteArrayOutputStream();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(lenna)) {
      writer.setOutput(ios);
      writer.write(null, new IIOImage(in, null, null), null);
    }
    hires.flush();

    assertThat(lenna.toByteArray().length).isNotEqualTo(hires.toByteArray().length);
  }
}
