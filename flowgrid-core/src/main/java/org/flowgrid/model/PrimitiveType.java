package org.flowgrid.model;


import org.flowgrid.model.api.Color;

/**
 * Note that the owner is null for primitive types, so they can be static.
 */
public class PrimitiveType extends AbstractType {
  public static final PrimitiveType BOOLEAN = new PrimitiveType("/system/Boolean", Boolean.class);
  public static final PrimitiveType NUMBER = new PrimitiveType("/system/Number", Double.class);
  public static final PrimitiveType TEXT = new PrimitiveType("/system/Text", String.class);
  public static final PrimitiveType COLOR = new PrimitiveType("/graphics/color/Color", Color.class);
  public static final PrimitiveType[] ALL = {BOOLEAN, NUMBER, TEXT, COLOR};

  private final String qualifiedName;

  private PrimitiveType(String qualifiedName, Class<?> javaClass) {
    super(qualifiedName.substring(qualifiedName.lastIndexOf('/') + 1), javaClass);
    this.qualifiedName = qualifiedName;
    this.isPublic = true;
  }
  
  public String qualifiedName() {
    return qualifiedName;
  }
}
