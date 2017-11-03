package de.digitalcollections.openjpeg.lib.callbacks;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.types.size_t;

public interface opj_stream_seek_fn {
  @Delegate
  boolean func(@size_t long numBytes, Pointer userData);
}
