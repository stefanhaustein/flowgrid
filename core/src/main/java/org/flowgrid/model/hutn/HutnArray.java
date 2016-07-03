package org.flowgrid.model.hutn;

import java.util.ArrayList;

public class HutnArray extends ArrayList<Object> {
  public boolean getBoolean(int index) {
    return (Boolean) get(index);
  }

  public boolean getBoolean(int index, boolean dflt) {
    Object value = get(index);
    return value != null ? ((Boolean) value).booleanValue() : dflt;
  }

  public double getDouble(int index) {
    return ((Number) get(index)).doubleValue();
  }

  public double getDouble(int index, double dflt) {
    Object value = get(index);
    return value != null ? ((Number) value).doubleValue() : dflt;
  }

  public int getInt(int index) {
    return ((Number) get(index)).intValue();
  }

  public int getInt(int index, int dflt) {
    Object value = get(index);
    return value != null ? ((Number) value).intValue() : dflt;
  }

  public HutnObject getJsonObject(int index) {
    return ((HutnObject) get(index));
  }

  public HutnArray getJsonArray(int index) {
    return ((HutnArray) get(index));
  }

  public long getLong(int index) {
    return ((Number) get(index)).longValue();
  }

  public long getLong(int index, long dflt) {
    Object value = get(index);
    return value != null ? ((Number) value).longValue() : dflt;
  }

  public String getString(int index) {
    return (String) get(index);
  }

  public String getString(int index, String dflt) {
    Object value = get(index);
    return value != null ? (String) value : dflt;
  }
}
