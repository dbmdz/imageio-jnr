package de.digitalcollections.turbojpeg.imageio;

import javax.imageio.plugins.jpeg.JPEGImageReadParam;

/**
 * Parameters for reading JPEG images.
 *
 * <p>Currently the only extra setting apart from the default ImageIO ones is setting the rotation
 * degree.
 */
public class TurboJpegImageReadParam extends JPEGImageReadParam {

  private int rotationDegree;

  public int getRotationDegree() {
    return rotationDegree;
  }

  public void setRotationDegree(int rotationDegree) {
    if (rotationDegree == 90 || rotationDegree == 180 || rotationDegree == 270) {
      this.rotationDegree = rotationDegree;
    }
    throw new IllegalArgumentException("Illegal rotation, must be 90, 180 or 270");
  }
}
