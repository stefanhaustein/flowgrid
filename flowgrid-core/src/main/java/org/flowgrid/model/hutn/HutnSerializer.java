package org.flowgrid.model.hutn;

public interface HutnSerializer {
  void assertLevel(int level);

  void endArray();

  void endObject();

  int level();

  void startArray();

  void startArray(String name);

  void startObject();

  void startObject(String name);

  void startTypedObject(String type);

  void startTypedObject(String name, String type);

  void writeBoolean(boolean value);

  void writeBoolean(String name, boolean value);

  void writeDouble(double value);

  void writeDouble(String name, double value);

  void writeLong(long value);

  void writeLong(String name, long value);

  void writeString(String value);

  void writeString(String name, String value);
}
