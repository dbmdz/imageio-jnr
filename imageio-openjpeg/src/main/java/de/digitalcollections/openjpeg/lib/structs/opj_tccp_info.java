package de.digitalcollections.openjpeg.lib.structs;

import de.digitalcollections.openjpeg.lib.libopenjp2;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class opj_tccp_info extends Struct {
  /** component index */
  public Unsigned32 compno = new Unsigned32();
  /** coding style */
  public Unsigned32 csty = new Unsigned32();
  /** number of resolutions */
  public Unsigned32 numresolutions = new Unsigned32();
  /** log2 of code-blocks width */
  public Unsigned32 cblkw = new Unsigned32();
  /** log2 of code-blocks height */
  public Unsigned32 cblkh = new Unsigned32();
  /** code-block coding style */
  public Unsigned32 cblksty = new Unsigned32();
  /** discrete wavelet transform identifier: 0 = 9-7 irreversible, 1 = 5-3 reversible */
  public Unsigned32 qmfbid = new Unsigned32();
  /** quantisation style */
  public Unsigned32 qntsty = new Unsigned32();
  /** stepsizes used for quantization */
  public Unsigned32[] stepsizes_mant = new Unsigned32[libopenjp2.OPJ_J2K_MAXBANDS];
  /** stepsizes used for quantization */
  public Unsigned32[] stepsizes_expn = new Unsigned32[libopenjp2.OPJ_J2K_MAXBANDS];
  /** number of guard bits */
  public Unsigned32 numgbits = new Unsigned32();
  /** Region Of Interest shift */
  Signed32 roishift = new Signed32();
  /** precinct width */
  public Unsigned32[] prcw = new Unsigned32[libopenjp2.OPJ_J2K_MAXRLVLS];
  /** precinct height */
  public Unsigned32[] prch = new Unsigned32[libopenjp2.OPJ_J2K_MAXRLVLS];


  public opj_tccp_info(Runtime runtime) {
    super(runtime);
  }
}
