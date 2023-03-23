package de.digitalcollections.turbojpeg.lib.enums;

import java.util.Arrays;
import jnr.ffi.util.EnumMapper.IntegerEnum;

public enum TJERR implements IntegerEnum {
  TJERR_WARNING(0),
  TJERR_FATAL(1);

  private final int val;

  TJERR(int val) {
    this.val = val;
  }

  @Override
  public int intValue() {
    return val;
  }

  public static TJERR fromInt(int val) {
    return Arrays.stream(TJERR.values())
        .filter(v -> v.val == val)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(("Unknown TJERR: " + val)));
  }
}
