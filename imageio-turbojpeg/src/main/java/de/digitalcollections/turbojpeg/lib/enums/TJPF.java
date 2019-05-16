package de.digitalcollections.turbojpeg.lib.enums;

import java.util.Arrays;
import jnr.ffi.util.EnumMapper.IntegerEnum;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR_PRE;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;

public enum TJPF implements IntegerEnum {
  TJPF_RGB(0),
  TJPF_BGR(1),
  TJPF_RGBX(2),
  TJPF_BGRX(3),
  TJPF_XBGR(4),
  TJPF_XRGB(5),
  TJPF_GRAY(6),
  TJPF_RGBA(7),
  TJPF_BGRA(8),
  TJPF_AGBR(9),
  TJPF_ARGB(10),
  TJPF_CMYK(11);

  private int val;

  public static TJPF fromImageType(int imageType) {
    switch (imageType) {
      case TYPE_3BYTE_BGR:
        return TJPF_BGR;
      case TYPE_4BYTE_ABGR:
      case TYPE_4BYTE_ABGR_PRE:
        return TJPF_XBGR;
      case TYPE_BYTE_GRAY:
        return TJPF_GRAY;
      default:
        throw new IllegalArgumentException("Unsupported image type");
    }
  }

  TJPF(int val) {
    this.val = val;
  }

  @Override
  public int intValue() {
    return val;
  }

  public static TJPF fromInt(int val) {
    return Arrays.stream(TJPF.values())
        .filter(v -> v.val == val)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown TJPF: " + val));
  }
}
