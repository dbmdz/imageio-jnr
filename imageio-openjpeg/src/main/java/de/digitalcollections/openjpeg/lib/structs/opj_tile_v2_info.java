package de.digitalcollections.openjpeg.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class opj_tile_v2_info extends Struct {
  /** number (index) of tile */
  public Signed32 tileno = new Signed32();
  /** coding style */
  public Unsigned32 csty = new Unsigned32();
  /** progression order */
  public Unsigned32 prg = new Unsigned32();
  /** number of layers */
  public Unsigned32 numlayers = new Unsigned32();
  /** multi-component transform identifier */
  public Unsigned32 mct = new Unsigned32();

  /** information concerning tile component parameters */
  public final StructRef<opj_tccp_info> tccp_info = new StructRef<>(opj_tccp_info.class);

  public opj_tile_v2_info(Runtime runtime) {
    super(runtime);
  }
}
