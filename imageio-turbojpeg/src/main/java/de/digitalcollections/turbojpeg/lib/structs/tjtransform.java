package de.digitalcollections.turbojpeg.lib.structs;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class tjtransform extends Struct {
  public final tjregion r;
  public final Signed32 op;
  public final Signed32 options;
  public final Pointer data;
  public final Pointer customFilter;

  public tjtransform(Runtime runtime) {
    super(runtime);
    // NOTE: We run the initializers in the constructor since we need to access the runtime
    r = inner(new tjregion(runtime));
    op = new Signed32();
    options = new Signed32();
    data = new Pointer();
    customFilter = new Pointer();
  }
}
