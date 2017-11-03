package de.digitalcollections.turbojpeg.lib.enums;

import jnr.ffi.util.EnumMapper.IntegerEnum;

public enum TJSAMP implements IntegerEnum {
  TJSAMP_444(0),
  TJSAMP_422(1),
  TJSAMP_420(2),
  TJSAMP_GRAY(3),
  TJSAMP_440(4);

  private final int val;

  TJSAMP(int val) {
    this.val = val;
  }

  @Override
  public int intValue() {
    return val;
  }
}
