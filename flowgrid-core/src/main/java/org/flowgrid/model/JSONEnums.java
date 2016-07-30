package org.flowgrid.model;

import java.util.EnumSet;

import org.flowgrid.model.hutn.HutnObject;

public class JSONEnums {

  public static <E extends Enum<E>> E optEnum(HutnObject jsonObject, String name, E dflt) {
    String value = jsonObject.getString(name, dflt.name());
    Class<E> clazz = dflt.getDeclaringClass();
    for (E en : EnumSet.allOf(clazz)) {
      if (en.name().equals(value)) {
        return en;
      }
    }
    return dflt;
  }
}
