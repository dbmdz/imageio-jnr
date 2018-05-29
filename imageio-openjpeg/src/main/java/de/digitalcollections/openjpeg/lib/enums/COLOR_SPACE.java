package de.digitalcollections.openjpeg.lib.enums;

import jnr.ffi.util.EnumMapper;

public enum COLOR_SPACE implements EnumMapper.IntegerEnum {
  OPJ_CLRSPC_UNKNOWN(-1), // not supported by the library
  OPJ_CLRSPC_UNSPECIFIED(0), // not specified in the codestream
  OPJ_CLRSPC_SRGB(1), // sRGB
  OPJ_CLRSPC_GRAY(2), // grayscale
  OPJ_CLRSPC_SYCC(3), // YUV
  OPJ_CLRSPC_EYCC(4), // e-YCC
  OPJ_CLRSPC_CMYK(5); // CMYK

  private final int value;

  COLOR_SPACE(int value) {
    this.value = value;
  }

  public int intValue() {
    return value;
  }
}
