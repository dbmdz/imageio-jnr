package de.digitalcollections.turbojpeg.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class tjregion extends Struct {
  public final Signed32 x = new Signed32();
  public final Signed32 y = new Signed32();
  public final Signed32 w = new Signed32();
  public final Signed32 h = new Signed32();

  protected tjregion(Runtime runtime) {
    super(runtime);
  }
}
