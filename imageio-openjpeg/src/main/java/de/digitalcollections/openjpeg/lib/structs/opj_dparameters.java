package de.digitalcollections.openjpeg.lib.structs;

import de.digitalcollections.openjpeg.lib.libopenjp2;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class opj_dparameters extends Struct {
  public final Unsigned32 cp_reduce = new Unsigned32();
  public final Unsigned32 cp_layer = new Unsigned32();
  public final AsciiString infile = new AsciiString(libopenjp2.STR_LEN);
  public final AsciiString outfile = new AsciiString(libopenjp2.STR_LEN);
  public final Signed32 index_on = new Signed32();
  public final AsciiString index = new AsciiString(libopenjp2.STR_LEN);
  /** subimage encoding: origin image offset in x direction */
  public final Signed32 image_offset_x0 = new Signed32();
  /** subimage encoding: origin image offset in y direction */
  public final Signed32 image_offset_y0 = new Signed32();
  /** subsampling value for dx */
  public final Signed32 subsampling_dx = new Signed32();
  /** subsampling value for dy */
  public final Signed32 subsampling_dy = new Signed32();
  /** input file format 0: PGX, 1: PxM, 2: BMP 3:TIF*/
  public final Signed32 decod_format = new Signed32();
  /** output file format 0: J2K, 1: JP2, 2: JPT */
  public final Signed32 cod_format = new Signed32();

  /* NOT YET USED IN THE V2 VERSION OF OPENJPEG */
  /* JPWL encoding parameters */
  /** enables writing of EPC in MH, thus activating JPWL */
  public final Boolean jpwl_epc_on = new Boolean();
  /** error protection method for MH (0,1,16,32,37-128) */
  public final Signed32 jpwl_hprot_MH = new Signed32();
  /** tile number of header protection specification (>=0) */
  public final Signed32[] jpwl_hprot_TPH_tileno = array(new Signed32[libopenjp2.JPWL_MAX_NO_TILESPECS]);
  /** error protection methods for TPHs (0,1,16,32,37-128) */
  public final Signed32[] jpwl_hprot_TPH = array(new Signed32[libopenjp2.JPWL_MAX_NO_TILESPECS]);
  /** tile number of packet protection specification (>=0) */
  public final Signed32[] jpwl_pprot_tileno = array(new Signed32[libopenjp2.JPWL_MAX_NO_PACKSPECS]);
  /** packet number of packet protection specification (>=0) */
  public final Signed32[] jpwl_pprot_packn = array(new Signed32[libopenjp2.JPWL_MAX_NO_PACKSPECS]);
  /** error protection methods for packets (0,1,16,32,37-128) */
  public final Signed32[] jpwl_pprot = array(new Signed32[libopenjp2.JPWL_MAX_NO_PACKSPECS]);
  /** enables writing of ESD, (0=no/1/2 bytes) */
  public final Signed32 jpwl_sens_size = new Signed32();
  /** sensitivity addressing size (0=auto/2/4 bytes) */
  public final Signed32 jpwl_sens_addr = new Signed32();
  /** sensitivity range (0-3) */
  public final Signed32 jpwl_sens_range = new Signed32();
  /** sensitivity method for MH (-1=no,0-7) */
  public final Signed32 jpwl_sens_MH = new Signed32();
  /** tile number of sensitivity specification (>=0) */
  public final Signed32[] jpwl_sens_TPH_tileno = array(new Signed32[libopenjp2.JPWL_MAX_NO_TILESPECS]);
  /** sensitivity methods for TPHs (-1=no,0-7) */
  public final Signed32[] jpwl_sens_TPH = array(new Signed32[libopenjp2.JPWL_MAX_NO_TILESPECS]);

  /**
    * DEPRECATED: use RSIZ, OPJ_PROFILE_* and MAX_COMP_SIZE instead
    * Digital Cinema compliance 0-not compliant, 1-compliant
    * */
  public final Signed32 cp_cinema = new Signed32();
  /**
    * Maximum size (in bytes) for each component.
    * If == 0, component size limitation is not considered
    * */
  public final Signed32 max_comp_size = new Signed32();
  /**
    * DEPRECATED: use RSIZ, OPJ_PROFILE_* and OPJ_EXTENSION_* instead
    * Profile name
    * */
  public final Signed32 cp_rsiz = new Signed32();
  /** Tile part generation*/
  public final Signed32 tp_on = new Signed32();
  /** Flag for Tile part generation*/
  public final Signed32 tp_flag = new Signed32();
  /** MCT (multiple component transform) */
  public final Signed32 tcp_mct = new Signed32();
  /** Enable JPIP indexing*/
  public final Boolean jpip_on = new Boolean();
  /** Naive implementation of MCT restricted to a single reversible array based
       encoding without offset concerning all the components. */
  public final Pointer mct_data = new Pointer();
  /**
    * Maximum size (in bytes) for the whole codestream.
    * If == 0, codestream size limitation is not considered
    * If it does not comply with tcp_rates, max_cs_size prevails
    * and a warning is issued.
   * */
  public final Signed32 max_cs_size = new Signed32();
  /** RSIZ value
      To be used to combine OPJ_PROFILE_*, OPJ_EXTENSION_* and (sub)levels values. */
  public final Unsigned16 rsiz = new Unsigned16();

  public opj_dparameters(Runtime runtime) {
    super(runtime);
  }
}
