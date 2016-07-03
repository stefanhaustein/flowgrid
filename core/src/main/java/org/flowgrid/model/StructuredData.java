package org.flowgrid.model;

import java.util.List;

public interface StructuredData {
  Object get(String name);
  Type type(String name);
  void set(String name, Object value);

  class ListWrapper implements StructuredData {
    List list;
    Type elementType;
    public ListWrapper(List list, Type listType) {
      this.list = list;
      elementType = listType instanceof ArrayType ? ((ArrayType) listType).elementType : Type.ANY;
    }

    @Override
    public Object get(String name) {
      int index = Integer.parseInt(name);
      return index == list.size() ? null : list.get(index);
    }

    @Override
    public Type type(String name) {
      return elementType;
    }

    @Override
    public void set(String name, Object value) {
      list.set(Integer.parseInt(name), value);
    }
  }

  class Interleaved implements StructuredData {
    private final Object[] data;
    public Interleaved(Object... data) {
      this.data = data;
    }
    private int find(String name) {
      for (int i = 0; i < data.length; i += 3) {
        if (data[i].equals(name)) {
          return i;
        }
      }
      return -3;
    }
    public Object get(String name) {
      return data[find(name) + 2];
    }
    public Type type(String name) {
      return (Type) data[find(name) + 1];
    }
    public void set(String name, Object value) {
      data[find(name) + 2] = value;
    }
  }

}
