package de.digitalcollections.openjpeg.lib.callbacks;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;

public interface opj_msg_callback {
  @Delegate
  void func(String msg, Pointer client_data);
}
