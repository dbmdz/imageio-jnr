package de.digitalcollections.openjpeg.lib.structs;

import de.digitalcollections.openjpeg.lib.enums.PROG_ORDER;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class opj_poc extends Struct {
  /** Resolution num start, Component num start, given by POC */
  Unsigned32 resno0 = new Unsigned32();
  Unsigned32 compno0 = new Unsigned32();
  /** Layer num end,Resolution num end, Component num end, given by POC */
  Unsigned32 layno1 = new Unsigned32();
  Unsigned32 resno1 = new Unsigned32();
  Unsigned32 compno1 = new Unsigned32();
  /** Layer num start,Precinct num start, Precinct num end */
  Unsigned32 layno0 = new Unsigned32();
  Unsigned32 precno0 = new Unsigned32();
  Unsigned32 precno1 = new Unsigned32();
  /** Progression order enum*/
  Enum<PROG_ORDER> prg1 = new Enum<>(PROG_ORDER.class);
  Enum<PROG_ORDER> prg = new Enum<>(PROG_ORDER.class);
  /** Progression order string*/
  String progorder = new AsciiString(5);
  /** Tile number */
  Unsigned32 tile = new Unsigned32();
  /** Start and end values for Tile width and height*/
  Signed32 tx0 = new Signed32();
  Signed32 tx1 = new Signed32();
  Signed32 ty0 = new Signed32();
  Signed32 ty1 = new Signed32();
  /** Start value, initialised in pi_initialise_encode*/
  Unsigned32 layS = new Unsigned32();
  Unsigned32 resS = new Unsigned32();
  Unsigned32 compS = new Unsigned32();
  Unsigned32 prcS = new Unsigned32();
  /** End value, initialised in pi_initialise_encode */
  Unsigned32 layE = new Unsigned32();
  Unsigned32 resE = new Unsigned32();
  Unsigned32 compE = new Unsigned32();
  Unsigned32 prcE = new Unsigned32();
  /** Start and end values of Tile width and height, initialised in pi_initialise_encode*/
  Unsigned32 txS = new Unsigned32();
  Unsigned32 txE = new Unsigned32();
  Unsigned32 tyS = new Unsigned32();
  Unsigned32 tyE = new Unsigned32();
  Unsigned32 dx = new Unsigned32();
  Unsigned32 dy = new Unsigned32();
  /** Temporary values for Tile parts, initialised in pi_create_encode */
  Unsigned32 lay_t = new Unsigned32();
  Unsigned32 res_t = new Unsigned32();
  Unsigned32 comp_t = new Unsigned32();
  Unsigned32 prc_t = new Unsigned32();
  Unsigned32 tx0_t = new Unsigned32();
  Unsigned32 ty0_t = new Unsigned32();

  public opj_poc(Runtime runtime) {
    super(runtime);
  }
}
