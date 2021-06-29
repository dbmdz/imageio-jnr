package de.digitalcollections.openjpeg.imageio;

import de.digitalcollections.openjpeg.OpenJpeg;
import de.digitalcollections.openjpeg.lib.enums.PROG_ORDER;
import de.digitalcollections.openjpeg.lib.structs.opj_cparameters;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.imageio.ImageWriteParam;

/** Parameters for encoding JPEG2000 images */
public class OpenJp2ImageWriteParam extends ImageWriteParam {

  public enum ProgressionOrder {
    LRCP(0),
    RLCP(1),
    RPCL(2),
    PCRL(3),
    CPRL(4);

    private final int val;

    ProgressionOrder(int val) {
      this.val = val;
    }

    PROG_ORDER toNative() {
      // Exception should never been thrown since this is static, but unchecked
      // Optional.get() triggers warnings
      return Arrays.stream(PROG_ORDER.values()).filter(v -> v.value == this.val).findFirst().orElseThrow(RuntimeException::new);
    }
  }

  public static final String COMPRESS_TYPE_LOSSY = "lossy";
  public static final String COMPRESS_TYPE_LOSSLESS = "lossless";

  /** Use irreversible wavelet compression (= lossy) * */
  boolean compressLossy = false;

  /** Write SOP marker before each packet * */
  boolean writeSOPMarkers = false;

  /** Write EPH marker after each header packet * */
  boolean writeEPHMarkers = false;

  /** Number of resolutions to encode * */
  int numResolutions = 6;

  /** Progession order. Defaults to LRCP. * */
  ProgressionOrder progOrder = ProgressionOrder.LRCP;

  protected opj_cparameters toNativeParams(OpenJpeg lib) {
    opj_cparameters params = new opj_cparameters(lib.runtime);
    lib.lib.opj_set_default_encoder_parameters(params);

    // Tiling
    if (this.getTilingMode() == MODE_EXPLICIT) {
      params.tile_size_on.set(true);
      params.cp_tx0.set(this.getTileGridXOffset());
      params.cp_ty0.set(this.getTileGridYOffset());
      params.cp_tdx.set(this.getTileWidth());
      params.cp_tdy.set(this.getTileHeight());
    }

    params.numresolution.set(this.numResolutions);
    params.prog_order.set(progOrder.toNative());
    if (writeSOPMarkers) {
      params.csty.set(params.csty.intValue() | 0x02);
    }
    if (writeEPHMarkers) {
      params.csty.set(params.csty.intValue() | 0x04);
    }

    params.tcp_mct.set(1);
    params.tcp_numlayers.set(1);
    params.cp_disto_alloc.set(1);
    if (compressLossy) {
      params.irreversible.set(1);
    }

    if (getCompressionMode() == MODE_EXPLICIT) {
      params.tcp_rates[0].set(Math.max(100f - getCompressionQuality() * 100f, 0f));
    } else {
      params.tcp_rates[0].set(0);
    }
    params.tcp_mct.set(1);
    return params;
  }

  @Override
  public boolean canWriteTiles() {
    return true;
  }

  @Override
  public boolean canOffsetTiles() {
    return true;
  }

  @Override
  public boolean canWriteProgressive() {
    return true;
  }

  @Override
  public boolean canWriteCompressed() {
    return true;
  }

  @Override
  public String[] getCompressionTypes() {
    return new String[] {"lossless", "lossy"};
  }

  /** Set the compression type. Must be 'lossless' (default) or 'lossy'. */
  @Override
  public void setCompressionType(String compressionType) {
    if (Stream.of(COMPRESS_TYPE_LOSSLESS, COMPRESS_TYPE_LOSSY).noneMatch(compressionType::equals)) {
      throw new IllegalArgumentException("Unknown compression type");
    }
    this.compressLossy = compressionType.equals(COMPRESS_TYPE_LOSSY);
  }

  @Override
  public String getCompressionType() {
    if (compressLossy) {
      return COMPRESS_TYPE_LOSSY;
    } else {
      return COMPRESS_TYPE_LOSSLESS;
    }
  }

  @Override
  public void unsetCompression() {
    super.unsetCompression();
    this.compressLossy = false;
  }

  @Override
  public boolean isCompressionLossless() {
    super.isCompressionLossless();
    return !this.compressLossy;
  }

  public boolean shouldWriteSOPMarkers() {
    return writeSOPMarkers;
  }

  /**
   * Write SOP markers after each packet.
   *
   * @param writeSOPMarkers flag if sop markers should be written
   */
  public void setWriteSOPMarkers(boolean writeSOPMarkers) {
    this.writeSOPMarkers = writeSOPMarkers;
  }

  public boolean shouldWriteEPHMarkers() {
    return writeEPHMarkers;
  }

  /**
   * Write EPH marker after each header packet.
   *
   * @param writeEPHMarkers flag if eph markers should be written
   */
  public void setWriteEPHMarkers(boolean writeEPHMarkers) {
    this.writeEPHMarkers = writeEPHMarkers;
  }

  /**
   * Set the compression quality. Automatically switches compression type to lossy. {@link
   * ImageWriteParam#setCompressionType} must have been set to {@link
   * ImageWriteParam#MODE_EXPLICIT}.
   *
   * <p>Quality must be between 0.0 (worst) and 1.0 (best).
   */
  @Override
  public void setCompressionQuality(float quality) {
    super.setCompressionQuality(quality);
    this.compressLossy = true;
  }

  public int getNumResolutions() {
    return numResolutions;
  }

  /**
   * Set the number of resolutions to encode in the output image.
   *
   * <p>Each resolution will be 2^num times smaller than the native resolution.
   *
   * @param numResolutions the num resolutions
   */
  public void setNumResolutions(int numResolutions) {
    this.numResolutions = numResolutions;
  }

  public ProgressionOrder getProgressionOrder() {
    return progOrder;
  }

  /**
   * Set the progression order of the encoded image.
   *
   * @param progOrder the progression order
   */
  public void setProgressionOrder(ProgressionOrder progOrder) {
    this.progOrder = progOrder;
  }
}
