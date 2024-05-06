package de.digitalcollections.turbojpeg.lib;

import de.digitalcollections.turbojpeg.lib.enums.TJPF;
import de.digitalcollections.turbojpeg.lib.enums.TJSAMP;
import de.digitalcollections.turbojpeg.lib.structs.tjtransform;
import java.nio.Buffer;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Direct;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.byref.NativeLongByReference;
import jnr.ffi.byref.PointerByReference;
import jnr.ffi.types.u_int32_t;

public interface libturbojpeg {
  int tjCompress2(
      Pointer handle,
      Buffer srcBuf,
      int width,
      int pitch,
      int height,
      TJPF pixelFormat,
      @In PointerByReference jpegBuf,
      NativeLongByReference jpegSize,
      TJSAMP jpegSubsamp,
      int jpegQual,
      int flags);

  int tjDecompress2(
      Pointer handle,
      @In Buffer jpegBuf,
      @u_int32_t long jpegSize,
      @Out Buffer dstBuf,
      int width,
      int pitch,
      int height,
      TJPF pixelFormat,
      int flags);

  int tjDecompressHeader3(
      Pointer handle,
      Buffer jpegBuf,
      @u_int32_t long jpegSize,
      @Out @In IntByReference width,
      @Out @In IntByReference height,
      @Out @In IntByReference jpegSubsamp,
      @Out @In IntByReference jpegColorspace);

  int tjTransform(
      Pointer handle,
      Buffer jpegBuf,
      @u_int32_t long jpegSize,
      int n,
      PointerByReference outBuf,
      NativeLongByReference dstSizes,
      @Direct tjtransform transform,
      int flags);

  int tjDestroy(Pointer handle);

  void tjFree(Pointer bufPtr);

  String tjGetErrorStr();

  Pointer tjGetScalingFactors(@Out IntByReference numscalingfactors);

  Pointer tjInitCompress();

  Pointer tjInitDecompress();

  Pointer tjInitTransform();

  Pointer tjAlloc(int bytes);

  long tjBufSize(int width, int height, TJSAMP subsamp);

  int tjGetErrorCode(Pointer handle);
}
