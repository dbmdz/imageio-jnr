package de.digitalcollections.openjpeg;

import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_seek_fn;
import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_skip_fn;
import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_write_fn;
import de.digitalcollections.openjpeg.lib.libopenjp2;
import java.io.IOException;
import jnr.ffi.Pointer;

public abstract class OutStreamWrapper {
  private libopenjp2 lib;
  private Pointer stream;

  // NOTE: We cannot use method references, since their evaluation creates a temporary instance of the functional
  // interface. That is, if we set the callbacks in the constructor without storing the method references, the
  // temporary instances would get garbage-collected at some point, which would lead to bad things.
  private opj_stream_write_fn writeCallback;
  private opj_stream_skip_fn skipCallback;
  private opj_stream_seek_fn seekCallback;

  protected OutStreamWrapper(libopenjp2 lib) {
    this.lib = lib;
    this.stream = lib.opj_stream_create(libopenjp2.OPJ_J2K_STREAM_CHUNK_SIZE, false);
    this.skipCallback = this::skip;
    this.seekCallback = this::seek;
    this.writeCallback = this::write;
    lib.opj_stream_set_write_function(stream, writeCallback);
    lib.opj_stream_set_skip_function(stream, skipCallback);
    lib.opj_stream_set_seek_function(stream, seekCallback);
    // NOTE: This should not be 0 and >= the size of the actual file. However, if we set it to the maximum value,
    //       it works with streams of any length without any drawbacks (as far as I could tell...)
    lib.opj_stream_set_user_data_length(stream, (long) Math.pow(2, 32));
  }

  public Pointer getNativeStream() {
    return stream;
  }

  protected abstract long write(Pointer inBuffer, long numBytes, Pointer userData);

  protected abstract long skip(long numBytes, Pointer userData);

  protected abstract boolean seek(long numBytes, Pointer userData);

  public void close() throws IOException {
    this.lib.opj_stream_destroy(this.stream);
    this.stream = null;
  }
}
