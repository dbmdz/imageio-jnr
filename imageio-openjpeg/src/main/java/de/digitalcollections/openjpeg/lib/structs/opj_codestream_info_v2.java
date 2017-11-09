package de.digitalcollections.openjpeg.lib.structs;

import de.digitalcollections.openjpeg.lib.libopenjp2;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.byref.AddressByReference;

public class opj_codestream_info_v2 extends Struct {
  /** tile origin in x = XTOsiz */
  public Unsigned32 tx0;
  /** tile origin in y = YTOsiz */
  public Unsigned32 ty0;
  /** tile size in x = XTsiz */
  public Unsigned32 tdx;
  /** tile size in y = YTsiz */
  public Unsigned32 tdy;
  /** number of tiles in X */
  public Unsigned32 tw;
  /** number of tiles in Y */
  public Unsigned32 th;

  /** number of components*/
  public Unsigned32 nbcomps;

  /** Default information regarding tiles inside image */
  public opj_tile_v2_info m_default_tile_info;

  public StructRef<opj_tile_v2_info> tinfo;

  public opj_codestream_info_v2(Runtime runtime) {
    super(runtime);
    // NOTE: We run the initializers in the constructor, since we need an instantiated runtime for the
    //       inner struct
    tx0 = new Unsigned32();
    ty0 = new Unsigned32();
    tdx = new Unsigned32();
    tdy = new Unsigned32();
    tw = new Unsigned32();
    th = new Unsigned32();
    nbcomps = new Unsigned32();
    m_default_tile_info = inner(new opj_tile_v2_info(runtime));
    tinfo = new StructRef<>(opj_tile_v2_info.class);
  }

  public void free(libopenjp2 lib) {
    if (this.tx0.getMemory().address() != 0) {
      AddressByReference addr = new AddressByReference(jnr.ffi.Address.valueOf(this.tx0.getMemory().address()));
      lib.opj_destroy_cstr_info(addr);
    }
  }
}
