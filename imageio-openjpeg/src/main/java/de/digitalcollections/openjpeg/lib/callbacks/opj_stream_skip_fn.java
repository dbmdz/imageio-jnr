package de.digitalcollections.openjpeg.lib.callbacks;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.types.int64_t;
import jnr.ffi.types.size_t;

public interface opj_stream_skip_fn {
  @Delegate
  @int64_t long func(@size_t long numBytes, Pointer userData);
}
