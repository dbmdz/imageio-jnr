package de.digitalcollections.openjpeg.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class opj_image_comptparm extends Struct {
  /**
   * XRsiz: horizontal separation of a sample of ith component with respect to the reference grid
   */
  public Unsigned32 dx = new Unsigned32();
  /** YRsiz: vertical separation of a sample of ith component with respect to the reference grid */
  public Unsigned32 dy = new Unsigned32();
  /** data width */
  public Unsigned32 w = new Unsigned32();
  /** data height */
  public Unsigned32 h = new Unsigned32();
  /** x component offset compared to the whole image */
  public Unsigned32 x0 = new Unsigned32();
  /** y component offset compared to the whole image */
  public Unsigned32 y0 = new Unsigned32();
  /** precision */
  public Unsigned32 prec = new Unsigned32();
  /** image depth in bits */
  public Unsigned32 bpp = new Unsigned32();
  /** signed (1) / unsigned (0) */
  public Unsigned32 sgnd = new Unsigned32();

  public opj_image_comptparm(Runtime runtime) {
    super(runtime);
  }
}
