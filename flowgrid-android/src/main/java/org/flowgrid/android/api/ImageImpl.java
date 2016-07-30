package org.flowgrid.android.api;

import org.flowgrid.model.Image;

import android.graphics.Bitmap;

public class ImageImpl implements Image {
  private static int count;
  private final Bitmap bitmap;
  private final int id = ++count;
  
  public ImageImpl(Bitmap bitmap) {
    this.bitmap = bitmap;
  }
  
  public Bitmap bitmap() {
    return bitmap;
  }

  @Override
  public int width() {
    return bitmap.getWidth();
  }

  @Override
  public int height() {
    return bitmap.getHeight();
  }
  
  public String toString() {
    return "Image#" + id;
  }
}
