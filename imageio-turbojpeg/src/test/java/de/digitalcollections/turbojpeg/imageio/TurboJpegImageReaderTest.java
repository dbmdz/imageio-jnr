package de.digitalcollections.turbojpeg.imageio;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

import static de.digitalcollections.turbojpeg.imageio.CustomAssertions.assertThat;

class TurboJpegImageReaderTest {
  @Test
  public void testReaderIsRegistered() {
    Supplier<List<ImageReader>> getReaderIter =
        () -> Lists.newArrayList(ImageIO.getImageReadersBySuffix("jpg"));
    assertThat(getReaderIter.get()).isNotEmpty();
    assertThat(getReaderIter.get()).hasAtLeastOneElementOfType(TurboJpegImageReader.class);
    getReaderIter = () -> Lists.newArrayList(ImageIO.getImageReadersByMIMEType("image/jpeg"));
    assertThat(getReaderIter.get()).isNotEmpty();
    assertThat(getReaderIter.get()).hasAtLeastOneElementOfType(TurboJpegImageReader.class);
    getReaderIter = () -> Lists.newArrayList(ImageIO.getImageReadersByFormatName("jpeg"));
    assertThat(getReaderIter.get()).isNotEmpty();
    assertThat(getReaderIter.get()).hasAtLeastOneElementOfType(TurboJpegImageReader.class);
  }

  private TurboJpegImageReader getReader(String fixtureFile) throws IOException {
    File inFile = new File(ClassLoader.getSystemResource(fixtureFile).getFile());
    ImageInputStream is = ImageIO.createImageInputStream(inFile);
    ImageReader reader = ImageIO.getImageReaders(is).next();
    assertThat(reader).isInstanceOf(TurboJpegImageReader.class);
    reader.setInput(is);
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
    BufferedImage img = getReader("rgb.jpg").read(2, null);
    assertThat(img).hasDimensions(384, 384);
  }

  @Test
  public void testReadRegionAligned() throws IOException {
    ImageReader reader = getReader("crop_aligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(32, 32, 96, 96));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(96, 96).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadRegionUnaligned() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(116, 148, 204, 172));
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(204, 172).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadUnalignedScaled() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    ImageReadParam param = reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(87, 111, 152, 129));
    BufferedImage img = reader.read(2, param);
    assertThat(img).hasDimensions(152, 129).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadUnalignedRotated() throws IOException {
    ImageReader reader = getReader("crop_unaligned_rot90.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(16, 16, 339, 319));
    param.setRotationDegree(90);
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(319, 339).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(180);
    img = reader.read(0, param);
    assertThat(img).hasDimensions(339, 319).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(270);
    img = reader.read(0, param);
    assertThat(img).hasDimensions(319, 339).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadRotated() throws IOException {
    ImageReader reader = getReader("crop_unaligned.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setRotationDegree(90);
    BufferedImage img = reader.read(0, param);
    img = img.getSubimage(192, 116, 172, 204);

    // Need to copy the image so we can check the image data
    BufferedImage copy =
        new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
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
    assertThat(img).hasDimensions(172, 204).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(180);
    img = reader.read(0, param);
    assertThat(img).hasDimensions(204, 172).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(270);
    img = reader.read(0, param);
    assertThat(img).hasDimensions(172, 204).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testReadRegionRotatedFullWidth() throws IOException {
    ImageReader reader = getReader("rotated_fullwidth.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, 384, 368));
    param.setRotationDegree(90);
    BufferedImage img = reader.read(1, param);
    assertThat(img).hasDimensions(368, 384).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(180);
    img = reader.read(1, param);
    assertThat(img).hasDimensions(384, 368).hasNoPixelsOfColor(-1 /* white */);
    param.setRotationDegree(270);
    img = reader.read(1, param);
    assertThat(img).hasDimensions(368, 384).hasNoPixelsOfColor(-1 /* white */);
  }

  @Test
  public void testCanReuseReader() throws IOException {
    ImageReader reader = getReader("rgb.jpg");
    BufferedImage rgbImg = reader.read(1, null);

    reader.setInput(
        ImageIO.createImageInputStream(
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
    assertThat(img).hasDimensions(239, 397).hasNoPixelsOfColor(-1);
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
  public void testCropToOnePixel() throws IOException {
    ImageReader reader = getReader("prime_shaped.jpg");
    TurboJpegImageReadParam scaledParam = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    scaledParam.setSourceRegion(new Rectangle(0, 0, reader.getWidth(2), 1));
    BufferedImage img = reader.read(2, scaledParam);

    // Special case (imageIndex > 0), so we have Math.round will return 2 pixel height
    assertThat(img).hasDimensions(reader.getWidth(2), 2);

    TurboJpegImageReadParam unscaledParam = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    unscaledParam.setSourceRegion(new Rectangle(0, 0, reader.getWidth(0), 1));
    BufferedImage newImg = reader.read(0, unscaledParam);

    // In case of imageIndex == 0, we expect a 1 pixel height image
    assertThat(newImg).hasDimensions(reader.getWidth(0), 1);
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

  @Test
  public void testReadCMYKDelegatesToDefault() throws IOException {
    File inFile = new File(ClassLoader.getSystemResource("cmyk.jpg").getFile());
    ImageInputStream is = ImageIO.createImageInputStream(inFile);
    ImageReader reader = ImageIO.getImageReaders(is).next();
    assertThat(reader).isNotInstanceOf(TurboJpegImageReader.class);
  }

  @Test
  public void testDoubleFreeCrash() throws IOException {
    ImageReader reader = getReader("thumbnail.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, reader.getWidth(4), reader.getHeight(4)));
    BufferedImage img = reader.read(4, param);
    assertThat(img.getWidth()).isEqualTo(180);
    assertThat(img.getHeight()).isEqualTo(136);
  }

  @Test
  public void testCroppingRequiresReallocation() throws IOException {
    ImageReader reader = getReader("needs_realloc.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(1281, 1281, 365, 10));
    BufferedImage img = reader.read(3, param);
    assertThat(img.getWidth()).isEqualTo(365);
    assertThat(img.getHeight()).isEqualTo(10);
  }

  @Test
  void testAdjustMCURegion() {
    TurboJpegImageReader reader = new TurboJpegImageReader(null, null);

    Dimension mcuSize = new Dimension(16, 16);
    Rectangle region = new Rectangle(1185, 327, 309, 36);
    int rotation = 0;
    Dimension imageSize = new Dimension(1500, 2260);

    Rectangle extraCrop = reader.adjustRegion(mcuSize, region, rotation, imageSize);
    Rectangle regionExpected = new Rectangle(1184, 320, 316, 48);
    Rectangle extraCropExpected = new Rectangle(1, 7, 309, 36);

    assertThat(region).isEqualTo(regionExpected);
    assertThat(extraCrop).isEqualTo(extraCropExpected);
  }

  @Test
  public void testReadGrayscale() throws IOException {
    ImageReader reader = getReader("grayscale.jpg");
    assertThat(reader.getRawImageType(0).getNumComponents()).isEqualTo(1);
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    BufferedImage img = reader.read(0, param);
    assertThat(img).hasDimensions(1955, 524);
    assertThat(img.getType()).isEqualTo(BufferedImage.TYPE_BYTE_GRAY);
    InputStream input = ClassLoader.getSystemResourceAsStream("grayscale_control.png");
    assertThat(input).isNotNull();
    BufferedImage controlImg = ImageIO.read(input);
    assertThat(img).isEqualTo(controlImg);
  }

  @Test
  public void testReadRotatedAndCroppedGridSearch() throws IOException {
    ImageReader reader = getReader("crop_rotation.jpg");

    // Unit-test it hard
    int originalHeight = reader.getHeight(0);
    int originalWidth = reader.getWidth(0);

    // defines distance between new regions
    int padding = 20;

    int regionHeight = 100;
    int regionWidth = 50;

    int[] rotationSizes = {90, 180, 270};

    for (int rotationSize : rotationSizes) {
      for (int y = padding; y < originalHeight; y += regionHeight + padding) {
        if (y + regionHeight > originalHeight) {
          break;
        }

        for (int x = padding; x < originalWidth; x += regionWidth + padding) {
          if (x + regionWidth > originalWidth) {
            break;
          }

          TurboJpegImageReadParam current_param =
              (TurboJpegImageReadParam) reader.getDefaultReadParam();
          current_param.setSourceRegion(new Rectangle(x, y, regionWidth, regionHeight));
          current_param.setRotationDegree(rotationSize);

          BufferedImage currentCroppedImage = reader.read(0, current_param);

          int referenceRegionHeight =
              rotationSize == 90 || rotationSize == 270 ? regionWidth : regionHeight;
          int referenceRegionWidth =
              rotationSize == 90 || rotationSize == 270 ? regionHeight : regionWidth;
          assertThat(currentCroppedImage.getHeight()).isEqualTo(referenceRegionHeight);
          assertThat(currentCroppedImage.getWidth()).isEqualTo(referenceRegionWidth);
        }
      }
    }
  }

  @Test
  public void testReadRotatedAndCroppedSpecial() throws IOException {
    ImageReader reader = getReader("crop_rotation.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(160, 740, 50, 100));
    param.setRotationDegree(90);
    BufferedImage rotatedCroppedImage = reader.read(0, param);

    assertThat(rotatedCroppedImage.getHeight()).isEqualTo(50);
    assertThat(rotatedCroppedImage.getWidth()).isEqualTo(100);
  }

  @Test
  public void testRegionSelect() throws IOException {
    ImageReader reader = getReader("mock-page-106245331.jpg");
    TurboJpegImageReadParam param = (TurboJpegImageReadParam) reader.getDefaultReadParam();
    param.setSourceRegion(new Rectangle(0, 0, 750, 1024));
    param.setRotationDegree(90);
    BufferedImage image = reader.read(4, param);

    assertThat(image.getHeight()).isEqualTo(750);
  }
}
