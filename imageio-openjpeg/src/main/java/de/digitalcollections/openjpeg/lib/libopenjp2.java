package de.digitalcollections.openjpeg.lib;

import de.digitalcollections.openjpeg.lib.callbacks.opj_msg_callback;
import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_read_fn;
import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_seek_fn;
import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_skip_fn;
import de.digitalcollections.openjpeg.lib.callbacks.opj_stream_write_fn;
import de.digitalcollections.openjpeg.lib.enums.CODEC_FORMAT;
import de.digitalcollections.openjpeg.lib.enums.COLOR_SPACE;
import de.digitalcollections.openjpeg.lib.structs.opj_codestream_info_v2;
import de.digitalcollections.openjpeg.lib.structs.opj_cparameters;
import de.digitalcollections.openjpeg.lib.structs.opj_dparameters;
import de.digitalcollections.openjpeg.lib.structs.opj_image;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Direct;
import jnr.ffi.annotations.Out;
import jnr.ffi.byref.AddressByReference;
import jnr.ffi.byref.PointerByReference;
import jnr.ffi.types.size_t;
import jnr.ffi.types.u_int32_t;
import jnr.ffi.types.u_int64_t;

public interface libopenjp2 {
  int OPJ_PATH_LEN = 4096;
  int STR_LEN = 4096;
  int JPWL_MAX_NO_TILESPECS = 16;
  int JPWL_MAX_NO_PACKSPECS = 16;
  int OPJ_J2K_MAXRLVLS = 33 ;
  int OPJ_J2K_MAXBANDS = (3*OPJ_J2K_MAXRLVLS-2);
  long OPJ_J2K_STREAM_CHUNK_SIZE = 0x100000;

  String opj_version();

  /* Stream functions */
  Pointer opj_stream_create_default_file_stream(String filename, boolean isReadStream);
  Pointer opj_stream_create(@size_t long bufSize, boolean isInput);
  void opj_stream_set_read_function(Pointer stream, opj_stream_read_fn read_fn);
  void opj_stream_set_write_function(Pointer stream, opj_stream_write_fn write_fn);
  void opj_stream_set_skip_function(Pointer stream, opj_stream_skip_fn skip_fn);
  void opj_stream_set_seek_function(Pointer stream, opj_stream_seek_fn seek_fn);
  void opj_stream_set_user_data_length(Pointer stream, @u_int64_t long dataLength);
  void opj_stream_destroy(Pointer stream);

  /* Decoding functions */
  Pointer opj_create_decompress(CODEC_FORMAT fmt);
  void opj_destroy_codec(Pointer codec);
  boolean opj_read_header(Pointer stream, Pointer codec, @Out PointerByReference imgPtr);
  boolean opj_setup_decoder(Pointer codec, opj_dparameters params);
  void opj_set_default_decoder_parameters(opj_dparameters params);
  boolean opj_set_decode_area(Pointer codec, Pointer image, int startX, int startY, int endX, int endY);
  boolean opj_decode(Pointer codec, Pointer stream, Pointer img);

  /* Encoding functions */
  Pointer opj_create_compress(CODEC_FORMAT fmt);
  void opj_set_default_encoder_parameters(@Direct opj_cparameters params);
  boolean opj_setup_encoder(Pointer codec, opj_cparameters params, opj_image img);
  boolean opj_start_compress(Pointer codec, opj_image img, Pointer stream);
  boolean opj_end_compress(Pointer codec, Pointer stream);
  boolean opj_encode(Pointer codec, Pointer stream);

  /* opj_image functions */
  Pointer opj_image_create(@u_int32_t int numcmpts, Pointer cmtparms, COLOR_SPACE clrspc);
  void opj_image_destroy(Pointer image);

  /* Info functions */
  opj_codestream_info_v2 opj_get_cstr_info(Pointer codec);
  void opj_destroy_cstr_info(AddressByReference infoPtr);

  /* Logging functions */
  boolean opj_set_info_handler(Pointer codec, opj_msg_callback msg_fn);
  boolean opj_set_warning_handler(Pointer codec, opj_msg_callback msg_fn);
  boolean opj_set_error_handler(Pointer codec, opj_msg_callback msg_fn);
}
