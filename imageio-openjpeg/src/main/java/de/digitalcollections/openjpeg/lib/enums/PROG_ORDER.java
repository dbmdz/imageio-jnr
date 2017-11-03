package de.digitalcollections.openjpeg.lib.enums;

import jnr.ffi.util.EnumMapper.IntegerEnum;

public enum PROG_ORDER implements IntegerEnum {
  OPJ_PROG_UNKNOWN(-1),  /**< place-holder */
  OPJ_LRCP(0),           /**< layer-resolution-component-precinct order */
  OPJ_RLCP(1),           /**< resolution-layer-component-precinct order */
  OPJ_RPCL(2),           /**< resolution-precinct-component-layer order */
  OPJ_PCRL(3),           /**< precinct-component-resolution-layer order */
  OPJ_CPRL(4);           /**< component-precinct-resolution-layer order */

  public final int value;

  PROG_ORDER(int val) {
    this.value = val;
  }

  @Override
  public int intValue() {
    return value;
  }
}
