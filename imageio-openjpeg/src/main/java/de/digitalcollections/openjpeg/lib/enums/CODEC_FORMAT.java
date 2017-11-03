package de.digitalcollections.openjpeg.lib.enums;

import jnr.ffi.util.EnumMapper;

public enum CODEC_FORMAT implements EnumMapper.IntegerEnum {
  OPJ_CODEC_UNKNOWN(-1),
  OPJ_CODEC_J2K(0),
  OPJ_CODEC_JPT(1),
  OPJ_CODEC_JP2(2),
  OPJ_CODEC_JPP(3),
  OPJ_CODEC_JPX(4);

  private final int value;
  CODEC_FORMAT(int value){
    this.value = value;
  }
  public int intValue() {
    return value;
  }
}
