package org.flowgrid.android.graphics;

import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;

import android.graphics.Color;

public class Colors {

  public enum Brightness {DARKEST, DARKER, REGULAR, BRIGHTER, BRIGHTEST};
  
  public static final int[] BLUE = {
    0xff0099cc, 0xff1da9da, 0xff33b5e5, 0xff8ad5f0, 0xffe2f4fb, 
  };
  
  public static final int[] VIOLET = {
    0xff9933cc, 0xffb368d9, 0xffc58be2, 0xffd6adeb, 0xfff5eafa, 
  };
  
  public static final int[] GREEN = {
    0xff669900, 0xff83b600, 0xff99cc00, 0xffc5e26d, 0xfff0f8db,
  };
  
  public static final int[] ORANGE = {
    0xffff8a00, 0xffffa00e, 0xffffbd21, 0xffffd980, 0xfffff6df
  };
  
  public static final int[] RED = {
    0xffcc0000, 0xffe21d1d, 0xffff4444, 0xffff9494, 0xffffe4e4
  };
 
  public static final int[] GRAY = {
    Color.DKGRAY, Color.GRAY, Color.LTGRAY, 0xffe8e8e8, Color.WHITE
  };
  
  
  public static int brightenChannel(int value, boolean brighten) {
    return brighten ? Math.min(255, (255 - value) / 2 + value) : value;
  }

  public static int typeColor(Type type, boolean ready) {
    return typeColor(type, ready ? Colors.Brightness.BRIGHTEST : Colors.Brightness.REGULAR);
  }

  public static int typeColor(Type type, Brightness brightness) {
    if (type == null) {
      return Color.GRAY;
    }
    if (type instanceof ArrayType) {
      type = ((ArrayType) type).elementType;
    }
    
    int index = brightness.ordinal();
    if (type == PrimitiveType.NUMBER) {
      return BLUE[index];
    }
    if (type == PrimitiveType.BOOLEAN) {
      return ORANGE[index];
    }
    if (type == PrimitiveType.TEXT) {
      return VIOLET[index];
    }
    if (type instanceof Classifier) {
      return GREEN[index];
    }
    return GRAY[index];
  }

}
