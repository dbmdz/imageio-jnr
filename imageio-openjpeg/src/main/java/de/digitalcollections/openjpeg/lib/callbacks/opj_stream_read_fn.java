package de.digitalcollections.openjpeg.lib.callbacks;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.types.size_t;

public interface opj_stream_read_fn {
  @Delegate
  @size_t long func(Pointer buffer, @size_t long numBytes, Pointer userData);
}
