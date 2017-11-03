package de.digitalcollections.turbojpeg.lib.structs;

import de.digitalcollections.turbojpeg.TurboJpeg;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public class tjtransform extends Struct {
  public tjregion r = inner(new tjregion(TurboJpeg.RUNTIME));
  public Signed32 op = new Signed32();
  public Signed32 options = new Signed32();
  public Pointer data = new Pointer();
  public Pointer customFilter = new Pointer();

  public tjtransform(Runtime runtime) {
    super(runtime);
  }
}
