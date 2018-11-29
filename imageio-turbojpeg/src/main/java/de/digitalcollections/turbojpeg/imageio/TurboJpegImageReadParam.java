package de.digitalcollections.turbojpeg.imageio;

import com.google.common.collect.ImmutableSet;
import javax.imageio.plugins.jpeg.JPEGImageReadParam;

public class TurboJpegImageReadParam extends JPEGImageReadParam {

  private int rotationDegree;

  public int getRotationDegree() {
    return rotationDegree;
  }

  public void setRotationDegree(int rotationDegree) {
    if (!ImmutableSet.of(90, 180, 270).contains(rotationDegree)) {
      throw new IllegalArgumentException("Illegal rotation, must be 90, 180 or 270");
    }
    this.rotationDegree = rotationDegree;
  }
}
