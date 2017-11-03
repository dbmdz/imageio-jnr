package de.digitalcollections.openjpeg.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class opj_marker_info extends Struct {
  /** marker type */
  public Unsigned16 type = new Unsigned16();
  /** position in codestream */
  public Signed64 pos = new Signed64();
  /** length, marker val included */
  public Signed32 len = new Signed32();

  public opj_marker_info(Runtime runtime) {
    super(runtime);
  }
}
