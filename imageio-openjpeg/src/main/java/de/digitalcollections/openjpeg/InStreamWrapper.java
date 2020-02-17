package de.digitalcollections.openjpeg;

import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_read_fn;
import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_skip_fn;
import de.digitalcollections.openjpeg.lib.libopenjp2;
import jnr.ffi.Pointer;

public abstract class InStreamWrapper {
  private Pointer stream;
  private libopenjp2 lib;

  // NOTE: We cannot use method references, since their evaluation creates a temporary instance of
  // the functional
  // interface. That is, if we set the callbacks in the constructor as we should, the temporary
  // instances would get
  // garbage-collected at some point, which would lead to bad things.
  private opj_stream_read_fn readCallback;
  private opj_stream_skip_fn skipCallback;

  protected InStreamWrapper(libopenjp2 lib) {
    this.lib = lib;
    this.stream = lib.opj_stream_create(libopenjp2.OPJ_J2K_STREAM_CHUNK_SIZE, true);
    this.skipCallback = this::skip;
    this.readCallback = this::read;
    lib.opj_stream_set_read_function(stream, readCallback);
    lib.opj_stream_set_skip_function(stream, skipCallback);
    // NOTE: This should not be 0 and >= the size of the actual file. However, if we set it to the
    // maximum value,
    //       it works with streams of any length without any drawbacks (as far as I could tell...)
    lib.opj_stream_set_user_data_length(stream, (long) Math.pow(2, 32));
  }

  public Pointer getNativeStream() {
    return stream;
  }

  protected abstract long read(Pointer outBuffer, long numBytes, Pointer userData);

  protected abstract long skip(long numBytes, Pointer userData);

  public void close() {
    lib.opj_stream_destroy(this.stream);
    this.stream = null;
  }
}
