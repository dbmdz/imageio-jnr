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
  private opj_stream_write_fn write_cb;
  private opj_stream_skip_fn skip_cb;
  private opj_stream_seek_fn seek_cb;

  protected OutStreamWrapper(libopenjp2 lib) {
    this.lib = lib;
    this.stream = lib.opj_stream_create(libopenjp2.OPJ_J2K_STREAM_CHUNK_SIZE, false);
    this.skip_cb = this::skip;
    this.seek_cb = this::seek;
    this.write_cb = this::write;
    lib.opj_stream_set_write_function(stream, write_cb);
    lib.opj_stream_set_skip_function(stream, skip_cb);
    lib.opj_stream_set_seek_function(stream, seek_cb);
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
