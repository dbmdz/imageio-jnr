package de.digitalcollections.turbojpeg.lib.enums;

import jnr.ffi.util.EnumMapper.IntegerEnum;

public enum TJXOP implements IntegerEnum {
  TJXOP_NONE(0),
  TJXOP_HFLIP(1),
  TJXOP_VFLIP(2),
  TJXOP_TRANSPOSE(3),
  TJXOP_TRANSVERSE(4),
  TJXOP_ROT90(5),
  TJXOP_ROT180(6),
  TJXOP_ROT270(7);

  private int val;

  TJXOP(int val) {
    this.val = val;
  }

  @Override
  public int intValue() {
    return val;
  }
}
