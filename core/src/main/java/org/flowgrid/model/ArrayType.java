package org.flowgrid.model;

import java.util.List;

public class ArrayType extends AbstractType {
  public static final ArrayType ANY = new ArrayType(Type.ANY);
  public static final ArrayType BOOLEAN = new ArrayType(PrimitiveType.BOOLEAN);
  public static final ArrayType NUMBER = new ArrayType(PrimitiveType.NUMBER);
  public static final ArrayType STRING = new ArrayType(PrimitiveType.TEXT);
  
  public final Type elementType;
  public final int length;  // -1: Unconstrained
  
  static String dimensionToString(int len) {
    return len == -1 ? "[]" : ("[" + len + "]");
  }
  
  public ArrayType(Type elementType, int dimension) {
    super(elementType.name() + dimensionToString(dimension), List.class);
    this.elementType = elementType;
    this.length = dimension;
  }

  public ArrayType(Type elementType) {
    this(elementType, -1);
  }
  
  public boolean isAssignableFrom(Type t2) {
    if (!(t2 instanceof ArrayType)) {
      return false;
    }
    ArrayType source = (ArrayType) t2;
    if (length != source.length && length != -1) {
      return false;
    }
    return elementType.isAssignableFrom(source.elementType);
  }


  public String qualifiedName() {
    return elementType.qualifiedName() + dimensionToString(length);
  }

}
