package de.digitalcollections.turbojpeg.lib.enums;

import java.util.Arrays;
import jnr.ffi.util.EnumMapper.IntegerEnum;

public enum TJCS implements IntegerEnum {
  TJCS_RGB(0),
  TJCS_YCbCr(1),
  TJCS_GRAY(2),
  TJCS_CMYK(3),
  TJCS_YCCK(4);

  private final int val;

  TJCS(int val) {
    this.val = val;
  }

  @Override
  public int intValue() {
    return val;
  }

  public static TJCS fromInt(int val) {
    return Arrays.stream(TJCS.values())
        .filter(v -> v.val == val)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown TJCS: " + val));
  }
}
