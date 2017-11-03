package de.digitalcollections.openjpeg;

import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_read_fn;
import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_skip_fn;
import de.digitalcollections.openjpeg.lib.libopenjp2;
import java.io.IOException;
import jnr.ffi.Pointer;

public abstract class InStreamWrapper {
  private Pointer stream;

  // NOTE: We cannot use method references, since their evaluation creates a temporary instance of the functional
  // interface. That is, if we set the callbacks in the constructor as we should, the temporary instances would get
  // garbage-collected at some point, which would lead to bad things.
  private opj_stream_read_fn read_cb;
  private opj_stream_skip_fn skip_cb;

  protected InStreamWrapper() {
    stream = OpenJpeg.LIB.opj_stream_create(libopenjp2.OPJ_J2K_STREAM_CHUNK_SIZE, true);
    this.skip_cb = this::skip;
    this.read_cb = this::read;
    OpenJpeg.LIB.opj_stream_set_read_function(stream, read_cb);
    OpenJpeg.LIB.opj_stream_set_skip_function(stream, skip_cb);
    // NOTE: This should not be 0 and >= the size of the actual file. However, if we set it to the maximum value,
    //       it works with streams of any length without any drawbacks (as far as I could tell...)
    OpenJpeg.LIB.opj_stream_set_user_data_length(stream, (long) Math.pow(2, 32));
  }

  public Pointer getNativeStream() {
    return stream;
  }

  protected abstract long read(Pointer outBuffer, long numBytes, Pointer userData);

  protected abstract long skip(long numBytes, Pointer userData);

  public void close() throws IOException {
    OpenJpeg.LIB.opj_stream_destroy(this.stream);
    this.stream = null;
  }
}
