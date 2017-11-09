package de.digitalcollections.openjpeg.imageio;

import de.digitalcollections.openjpeg.OpenJpeg;
import de.digitalcollections.openjpeg.OutStreamWrapper;
import java.io.IOException;
import javax.imageio.stream.ImageOutputStream;
import jnr.ffi.Pointer;

class ImageOutputStreamWrapper extends OutStreamWrapper {
  private ImageOutputStream os;

  public ImageOutputStreamWrapper(ImageOutputStream os, OpenJpeg lib) {
    super(lib.lib);
    this.os = os;
  }

  protected long write(Pointer inBuffer, long numBytes, Pointer userData) {
    byte[] buf = new byte[(int) numBytes];
    inBuffer.get(0, buf, 0, (int) numBytes);
    try {
      os.write(buf);
    } catch (IOException e) {
      return -1;
    }
    return numBytes;
  }

  protected long skip(long numBytes, Pointer userData) {
    try {
      return this.os.skipBytes(numBytes);
    } catch (IOException e) {
      return -1;
    }
  }

  protected boolean seek(long numBytes, Pointer userData) {
    try {
      this.os.seek(numBytes);
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
