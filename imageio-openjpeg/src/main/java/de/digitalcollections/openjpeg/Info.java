package de.digitalcollections.openjpeg;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Info {
  private int numComponents;
  private int numResolutions;
  private int tileWidth;
  private int tileHeight;
  private int tileOriginX;
  private int tileOriginY;
  private int numTilesX;
  private int numTilesY;
  private int width;
  private int height;

  void setNumComponents(int numComponents) {
    this.numComponents = numComponents;
  }

  void setNumResolutions(int numResolutions) {
    this.numResolutions = numResolutions;
  }

  void setTileWidth(int tileWidth) {
    this.tileWidth = tileWidth;
  }

  void setTileHeight(int tileHeight) {
    this.tileHeight = tileHeight;
  }

  void setNumTilesX(int numTilesX) {
    this.numTilesX = numTilesX;
  }

  void setNumTilesY(int numTilesY) {
    this.numTilesY = numTilesY;
  }

  void setWidth(int width) {
    this.width = width;
  }

  void setHeight(int height) {
    this.height = height;
  }

  public void setTileOriginX(int tileOriginX) {
    this.tileOriginX = tileOriginX;
  }

  public void setTileOriginY(int tileOriginY) {
    this.tileOriginY = tileOriginY;
  }

  public int getNumComponents() {
    return numComponents;
  }

  public int getNumResolutions() {
    return numResolutions;
  }

  public Dimension getNativeSize() {
    return new Dimension(this.width, this.height);
  }

  public Dimension getTileSize() {
    return new Dimension(tileWidth, tileHeight);
  }

  public Point getTileOrigin() {
    return new Point(tileOriginX, tileOriginY);
  }

  public int getNumTilesX() {
    return numTilesX;
  }

  public int getNumTilesY() {
    return numTilesY;
  }

  public int getNumTiles() {
    return numTilesX + numTilesY;
  }

  public double[] getScaleFactors() {
    return IntStream.range(0, this.numResolutions)
        .mapToDouble(n -> Math.pow(2, n))
        .toArray();
  }

  public List<Dimension> getAvailableImageSizes() {
    return Arrays.stream(getScaleFactors())
        .mapToObj(factor -> new Dimension((int) (this.width / factor), (int) (this.height / factor)))
        .collect(Collectors.toList());
  }

  public List<Dimension> getAvailableTileSizes() {
    return Arrays.stream(getScaleFactors())
        .mapToObj(factor -> new Dimension((int) (this.tileWidth / factor), (int) (this.tileHeight / factor)))
        .collect(Collectors.toList());
  }
}
