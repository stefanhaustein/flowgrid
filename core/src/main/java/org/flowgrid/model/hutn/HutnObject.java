package org.flowgrid.model.hutn;

import java.util.LinkedHashMap;

public class HutnObject extends LinkedHashMap<String, Object> {

  String type;

  public HutnObject(String type) {
    this.type = type;
  }

  public HutnObject() {
    this(null);
  }

  public boolean getBoolean(String name) {
    return ((Boolean) get(name));
  }

  public boolean getBoolean(String name, boolean dflt) {
    Object value = get(name);
    return value != null ? ((Boolean) value).booleanValue() : dflt;
  }

  public double getDouble(String name) {
    return ((Number) get(name)).doubleValue();
  }

  public double getDouble(String name, int dflt) {
    Object value = get(name);
    return value != null ? ((Number) value).doubleValue() : dflt;
  }

  public int getInt(String name) {
    return ((Number) get(name)).intValue();
  }

  public int getInt(String name, int dflt) {
    Object value = get(name);
    return value != null ? ((Number) value).intValue() : dflt;
  }

  public HutnArray getJsonArray(String name) {
    return (HutnArray) get(name);
  }

  public HutnObject getJsonObject(String name) {
    return (HutnObject) get(name);
  }

  public long getLong(String name) {
    return ((Number) get(name)).longValue();
  }

  public long getLong(String name, long dflt) {
    Object value = get(name);
    return value != null ? (((Number) value).longValue()) : dflt;
  }

  public String getString(String name) {
    return (String) get(name);
  }

  public String getString(String name, String dflt) {
    Object value = get(name);
    return value != null ? ((String) value) : dflt;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String type() {
    return type;
  }

  public HutnArray toJsonArray(Iterable<String> names) {
    HutnArray result = new HutnArray();
    for (String name : names) {
      result.add(get(name));
    }
    return result;
  }
}
