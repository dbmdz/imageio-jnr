package de.digitalcollections.turbojpeg;

import de.digitalcollections.turbojpeg.lib.enums.TJCS;
import de.digitalcollections.turbojpeg.lib.enums.TJSAMP;
import de.digitalcollections.turbojpeg.lib.structs.tjscalingfactor;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Info {
  private final int width;
  private final int height;
  private final TJSAMP subsampling;
  private final TJCS colorspace;
  final List<Dimension> availableSizes;

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public TJSAMP getSubsampling() {
    return subsampling;
  }

  public TJCS getColorspace() {
    return colorspace;
  }

  public List<Dimension> getAvailableSizes() {
    return availableSizes;
  }

  private static int getScaled(int dim, int num, int denom) {
    return (dim * num + denom - 1) / denom;
  }

  /** Create a new instance with the information parsed from the JPEG image. */
  public Info(int width, int height, int subsampling, int colorspace, tjscalingfactor[] factors) {
    this.width = width;
    this.height = height;
    this.subsampling = TJSAMP.fromInt(subsampling);
    this.colorspace = TJCS.fromInt(colorspace);
    // The available sizes are determined from the list of scaling factors.
    this.availableSizes =
        Arrays.stream(factors)
            .filter(f -> f.denom.get() > 0)
            .sorted(Comparator.comparing(f -> -getScaled(width, f.num.get(), f.denom.get())))
            .map(
                f ->
                    new Dimension(
                        getScaled(width, f.num.get(), f.denom.get()),
                        getScaled(height, f.num.get(), f.denom.get())))
            .filter(d -> d.width <= width && d.height <= height && d.width > 0 && d.height > 0)
            .distinct()
            .collect(Collectors.toList());
  }

  /**
   * Get the size of the Minimum Coding Units.
   *
   * <p>Neccessary to calculate the right cropping alignments.
   */
  public Dimension getMCUSize() {
    switch (subsampling) {
      case TJSAMP_422: // 4:2:2
        return new Dimension(16, 8);
      case TJSAMP_420:
        return new Dimension(16, 16);
      case TJSAMP_440:
        return new Dimension(8, 16);
      case TJSAMP_411:
        return new Dimension(32,8);
      default:
        return new Dimension(8, 8);
    }
  }
}
