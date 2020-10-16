package de.digitalcollections.openjpeg;

import java.awt.color.ColorSpace;

/**
 * A CMYK simulation like color_cmyk_to_rgb() in https://github.com/uclouvain/openjpeg/blob/master/src/bin/common/color.c
 * 
 * For a better CMYK is a ICC_Profile needed which required a license agreement on downloading. With the profile you can:
 * <pre><code>
 * ICC_Profile profile = ICC_Profile.getInstance( path );
 * new ICC_ColorSpace( profile );
 * </code></pre>
 */
class CMYKColorSpace extends ColorSpace {

  private final ColorSpace sRGB = getInstance(CS_sRGB);

  CMYKColorSpace() {
    super( ColorSpace.TYPE_CMYK, 4 );
  }

  @Override
  public float[] toRGB( float[] cmyk ) {
    float k = 1 - cmyk[3];
    return new float[] { (1 - cmyk[0]) * k, (1 - cmyk[1]) * k, (1 - cmyk[2]) * k };
  }

  @Override
  public float[] fromRGB( float[] rgbvalue ) {
    float c = 1 - rgbvalue[0];
    float m = 1 - rgbvalue[1];
    float y = 1 - rgbvalue[2];

    float k = Math.min( c, Math.min( m, y ) );

    return new float[] { (c - k), (m - k), (y - k), k };
  }

  @Override
  public float[] toCIEXYZ( float[] colorvalue ) {
    return sRGB.toCIEXYZ( toRGB( colorvalue ) );
  }

  @Override
  public float[] fromCIEXYZ( float[] colorvalue ) {
    return sRGB.fromCIEXYZ( fromRGB( colorvalue ) );
  }
}
