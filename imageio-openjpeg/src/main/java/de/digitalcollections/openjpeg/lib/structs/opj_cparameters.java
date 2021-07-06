package de.digitalcollections.openjpeg.lib.structs;

import de.digitalcollections.openjpeg.lib.enums.PROG_ORDER;
import de.digitalcollections.openjpeg.lib.libopenjp2;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class opj_cparameters extends Struct {
  /** size of tile: tile_size_on = false (not in argument) or = true (in argument) */
  public Boolean tile_size_on = new Boolean();
  /** XTOsiz */
  public Signed32 cp_tx0 = new Signed32();
  /** YTOsiz */
  public Signed32 cp_ty0 = new Signed32();
  /** XTsiz */
  public Signed32 cp_tdx = new Signed32();
  /** YTsiz */
  public Signed32 cp_tdy = new Signed32();
  /** allocation by rate/distortion */
  public Signed32 cp_disto_alloc = new Signed32();
  /** allocation by fixed layer */
  public Signed32 cp_fixed_alloc = new Signed32();
  /** add fixed_quality */
  public Signed32 cp_fixed_quality = new Signed32();
  /** fixed layer */
  public Pointer cp_matrice = new Pointer();
  /** comment for coding */
  public String cp_comment = new AsciiStringRef();
  /** csty : coding style */
  public Signed32 csty = new Signed32();
  /** progression order (default OPJ_LRCP) */
  public Enum<PROG_ORDER> prog_order = new Enum<>(PROG_ORDER.class);
  /** progression order changes */
  public opj_poc[] POC = array(new opj_poc[32]);
  /** number of progression order changes (POC), default to 0 */
  public Unsigned32 numpocs = new Unsigned32();
  /** number of layers */
  public Signed32 tcp_numlayers = new Signed32();
  /**
   * rates of layers - might be subsequently limited by the max_cs_size field. Should be decreasing.
   * 1 can be used as last value to indicate the last layer is lossless.
   */
  public final Float[] tcp_rates = array(new Float[100]);
  /**
   * different psnr for successive layers. Should be increasing. 0 can be used as last value to
   * indicate the last layer is lossless.
   */
  public Float[] tcp_distoratio = array(new Float[100]);
  /** number of resolutions */
  public Signed32 numresolution = new Signed32();
  /** initial code block width, default to 64 */
  public Signed32 cblockw_init = new Signed32();
  /** initial code block height, default to 64 */
  public Signed32 cblockh_init = new Signed32();
  /** mode switch (cblk_style) */
  public Signed32 mode = new Signed32();
  /** 1 : use the irreversible DWT 9-7, 0 : use lossless compression (default) */
  public Signed32 irreversible = new Signed32();
  /** region of interest: affected component in [0..3], -1 means no ROI */
  public Signed32 roi_compno = new Signed32();
  /** region of interest: upshift value */
  public Signed32 roi_shift = new Signed32();
  /* number of precinct size specifications */
  public Signed32 res_spec = new Signed32();
  /** initial precinct width */
  public Signed32[] prcw_init = array(new Signed32[libopenjp2.OPJ_J2K_MAXRLVLS]);
  /** initial precinct height */
  public Signed32[] prch_init = array(new Signed32[libopenjp2.OPJ_J2K_MAXRLVLS]);

  /* @name command line encoder parameters (not used inside the library) */
  /*@{*/
  /** input file name */
  String infile = new AsciiString(libopenjp2.OPJ_PATH_LEN);
  /** output file name */
  String outfile = new AsciiString(libopenjp2.OPJ_PATH_LEN);
  /**
   * DEPRECATED. Index generation is now handeld with the opj_encode_with_info() function. Set to
   * NULL
   */
  public Signed32 index_on = new Signed32();
  /**
   * DEPRECATED. Index generation is now handeld with the opj_encode_with_info() function. Set to
   * NULL
   */
  String index = new AsciiString(libopenjp2.OPJ_PATH_LEN);
  /** subimage encoding: origin image offset in x direction */
  Signed32 image_offset_x0 = new Signed32();
  /** subimage encoding: origin image offset in y direction */
  Signed32 image_offset_y0 = new Signed32();
  /** subsampling value for dx */
  Signed32 subsampling_dx = new Signed32();
  /** subsampling value for dy */
  Signed32 subsampling_dy = new Signed32();
  /** input file format 0: PGX, 1: PxM, 2: BMP 3:TIF */
  Signed32 decod_format = new Signed32();
  /** output file format 0: J2K, 1: JP2, 2: JPT */
  Signed32 cod_format = new Signed32();
  /*@}*/

  /* UniPG>> */
  /* NOT YET USED IN THE V2 VERSION OF OPENJPEG */
  /* @name JPWL encoding parameters */
  /*@{*/
  /** enables writing of EPC in MH, thus activating JPWL */
  public Boolean jpwl_epc_on = new Boolean();
  /** error protection method for MH (0,1,16,32,37-128) */
  Signed32 jpwl_hprot_MH = new Signed32();
  /** tile number of header protection specification (>=0) */
  Signed32[] jpwl_hprot_TPH_tileno = array(new Signed32[libopenjp2.JPWL_MAX_NO_TILESPECS]);
  /** error protection methods for TPHs (0,1,16,32,37-128) */
  Signed32[] jpwl_hprot_TPH = array(new Signed32[libopenjp2.JPWL_MAX_NO_TILESPECS]);
  /** tile number of packet protection specification (>=0) */
  Signed32[] jpwl_pprot_tileno = array(new Signed32[libopenjp2.JPWL_MAX_NO_PACKSPECS]);
  /** packet number of packet protection specification (>=0) */
  Signed32[] jpwl_pprot_packno = array(new Signed32[libopenjp2.JPWL_MAX_NO_PACKSPECS]);
  /** error protection methods for packets (0,1,16,32,37-128) */
  Signed32[] jpwl_pprot = array(new Signed32[libopenjp2.JPWL_MAX_NO_PACKSPECS]);
  /** enables writing of ESD, (0=no/1/2 bytes) */
  Signed32 jpwl_sens_size = new Signed32();
  /** sensitivity addressing size (0=auto/2/4 bytes) */
  Signed32 jpwl_sens_addr = new Signed32();
  /** sensitivity range (0-3) */
  Signed32 jpwl_sens_range = new Signed32();
  /** sensitivity method for MH (-1=no,0-7) */
  public Signed32 jpwl_sens_MH = new Signed32();
  /** tile number of sensitivity specification (>=0) */
  Signed32[] jpwl_sens_TPH_tileno = array(new Signed32[libopenjp2.JPWL_MAX_NO_TILESPECS]);
  /** sensitivity methods for TPHs (-1=no,0-7) */
  Signed32[] jpwl_sens_TPH = array(new Signed32[libopenjp2.JPWL_MAX_NO_TILESPECS]);
  /*@}*/
  /* <<UniPG */

  /**
   * DEPRECATED: use RSIZ, OPJ_PROFILE_* and MAX_COMP_SIZE instead Digital Cinema compliance 0-not
   * compliant, 1-compliant
   */
  Signed32 cp_cinema = new Signed32();
  /**
   * Maximum size (in bytes) for each component. If == 0, component size limitation is not
   * considered
   */
  Signed32 max_comp_size = new Signed32();
  /** DEPRECATED: use RSIZ, OPJ_PROFILE_* and OPJ_EXTENSION_* instead Profile name */
  Signed32 cp_rsiz = new Signed32();
  /** Tile part generation */
  Unsigned8 tp_on = new Unsigned8();
  /** Flag for Tile part generation */
  Unsigned8 tp_flag = new Unsigned8();
  /** MCT (multiple component transform) */
  public Unsigned8 tcp_mct = new Unsigned8();
  /** Enable JPIP indexing */
  Boolean jpip_on = new Boolean();
  /**
   * Naive implementation of MCT restricted to a single reversible array based encoding without
   * offset concerning all the components.
   */
  Pointer mct_data = new Pointer();
  /**
   * Maximum size (in bytes) for the whole codestream. If == 0, codestream size limitation is not
   * considered If it does not comply with tcp_rates, max_cs_size prevails and a warning is issued.
   */
  Signed32 max_cs_size = new Signed32();
  /** RSIZ value To be used to combine OPJ_PROFILE_*, OPJ_EXTENSION_* and (sub)levels values. */
  Unsigned16 rsiz = new Unsigned16();

  public opj_cparameters(Runtime runtime) {
    super(runtime);
    // FIXME: For some reason this is neccessary, even though the `array(...)` call does exactly
    // this.
    // Removing it causes a double free crash
    for (int i = 0; i < this.POC.length; i++) {
      this.POC[i] = inner(new opj_poc(runtime));
    }
  }
}
