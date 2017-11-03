package de.digitalcollections.openjpeg.imageio;

import de.digitalcollections.openjpeg.InStreamWrapper;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;
import jnr.ffi.Pointer;

public class ImageInputStreamWrapper extends InStreamWrapper {
  private ImageInputStream is;

  ImageInputStreamWrapper(ImageInputStream is) {
    super();
    this.is = is;
  }

  protected long read(Pointer outBuffer, long numBytes, Pointer userData) {
    byte[] buf = new byte[(int) numBytes];
    try {
      int read = is.read(buf, 0, (int) numBytes);
      if (read > 0) {
        outBuffer.put(0, buf, 0, read);
      } else {
        return read;
      }
      return read;
    } catch (IOException e) {
      return -1;
    }
  }

  protected long skip(long numBytes, Pointer userData) {
    try {
      return this.is.skipBytes(numBytes);
    } catch (IOException e) {
      return -1;
    }
  }
}
