package de.digitalcollections.openjpeg.imageio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
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

  private void assertImageEquals( String expectedImageName, String actualImageName ) throws IOException {
    OpenJp2ImageReader reader = getReader( actualImageName );
    BufferedImage actualImage = reader.read(0, null);

    InputStream input = ClassLoader.getSystemResourceAsStream( expectedImageName);
    BufferedImage expectedImg = ImageIO.read( input );
    int width = expectedImg.getWidth();
    assertEquals( width, actualImage.getWidth() );
    int height = expectedImg.getHeight();
    assertEquals( height, actualImage.getHeight() );
    for( int x = 0; x < width; x++ ) {
      for( int y = 0; y < height; y++ ) {
        int expected = expectedImg.getRGB( x, y );
        int actual = actualImage.getRGB( x, y );
        if( expected >> 24 == 0 && actual >> 24 == 0 ) {
          // transparent
          continue;
        }
        assertEquals( expected, actual, "RGB of Pixel " + x + "," + y );
      }
    }
  }

  @Test
  public void testReadRGBA() throws Exception {
    assertImageEquals( "rgba.png", "rgba.jp2" );
  }

  @Test
  public void testReadCMYK() throws Exception {
    assertImageEquals( "cmyk.png", "cmyk.jp2" );
  }

  @Test
  public void testReadCMYK_withAlpha() throws Exception {
    assertImageEquals( "cmykWithAlpha.png", "cmykWithAlpha.jp2" );
  }
}
