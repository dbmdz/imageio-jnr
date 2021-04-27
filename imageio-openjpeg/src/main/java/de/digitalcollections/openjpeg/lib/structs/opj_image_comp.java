package de.digitalcollections.openjpeg.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class opj_image_comp extends Struct {
  /**
   * XRsiz: horizontal separation of a sample of ith component with respect to the reference grid
   */
  Unsigned32 dx = new Unsigned32();
  /** YRsiz: vertical separation of a sample of ith component with respect to the reference grid */
  Unsigned32 dy = new Unsigned32();
  /** data width */
  public Unsigned32 w = new Unsigned32();
  /** data height */
  public Unsigned32 h = new Unsigned32();
  /** x component offset compared to the whole image */
  Unsigned32 x0 = new Unsigned32();
  /** y component offset compared to the whole image */
  Unsigned32 y0 = new Unsigned32();
  /** image depth in bits */
  public Unsigned32 bpp = new Unsigned32();
  /** precision */
  public Unsigned32 prec = new Unsigned32();
  /** signed (1) / unsigned (0) */
  Unsigned32 sgnd = new Unsigned32();
  /** number of decoded resolution */
  Unsigned32 resno_decoded = new Unsigned32();
  /** number of division by 2 of the out image compared to the original size of image */
  Unsigned32 factor = new Unsigned32();
  /** image component data */
  public Pointer data = new Pointer();

  /** alpha channel */
  Unsigned16 alpha = new Unsigned16();

  public opj_image_comp(Runtime runtime) {
    super(runtime, new Alignment(8));
  }
}
