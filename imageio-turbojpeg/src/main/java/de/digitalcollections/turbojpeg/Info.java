package de.digitalcollections.turbojpeg;

import de.digitalcollections.turbojpeg.lib.structs.tjscalingfactor;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Info {
  private int width;
  private int height;
  private int subsampling;
  List<Dimension> availableSizes;

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getSubsampling() {
    return subsampling;
  }

  public List<Dimension> getAvailableSizes() {
    return availableSizes;
  }

  private static int getScaled(int dim, int num, int denom) {
    return (dim * num + denom -1) / denom;
  }

  public Info(int width, int height, int subsampling, tjscalingfactor[] factors) {
    this.width = width;
    this.height = height;
    this.subsampling = subsampling;
    this.availableSizes = Arrays.stream(factors)
        .sorted(Comparator.comparing(f -> getScaled(width, f.num.get(), f.denom.get())))
        .map(f -> new Dimension(getScaled(width, f.num.get(), f.denom.get()),
                                getScaled(height, f.num.get(), f.denom.get())))
        .filter(d -> d.width <= width && d.height <= height && d.width > 1 && d.height > 1)
        .distinct()
        .collect(Collectors.toList());
  }
}
