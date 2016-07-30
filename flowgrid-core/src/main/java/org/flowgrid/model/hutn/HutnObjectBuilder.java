package org.flowgrid.model.hutn;

import java.util.ArrayList;

public class HutnObjectBuilder implements HutnSerializer {
  ArrayList<Object> stack = new ArrayList<Object>();
  HutnObject currentObject;
  HutnArray currentArray;
  Object root;

  public void assertLevel(int level) {
    if (level != stack.size()) {
      throw new IllegalStateException("Expected nesting level: " + level + " actual: " + stack.size());
    }
  }

  public Object build() {
    if (stack.size() != 0) {
      throw new IllegalStateException("Stack size: " + stack.size());
    }
    return root;
  }

  @Override
  public void endArray() {
    if (currentArray == null) {
      throw new IllegalStateException();
    }
    Object current = stack.remove(stack.size() - 1);
    if (current instanceof HutnObject) {
      currentObject = (HutnObject) current;
      currentArray = null;
    } else if (current instanceof HutnArray) {
      currentObject = null;
      currentArray = (HutnArray) current;
    } else if (current == null && stack.size() == 0) {
      currentArray = null;
      currentObject = null;
    } else {
      throw new IllegalStateException("Current: " + current + " level: " + stack.size() + " root: " + root + " currentArray: " + currentArray);
    }
  }

  @Override
  public void endObject() {
    if (currentObject == null) {
      throw new IllegalStateException();
    }
    Object current = stack.remove(stack.size() - 1);
    if (current instanceof HutnObject) {
      currentObject = (HutnObject) current;
      currentArray = null;
    } else if (current instanceof HutnArray) {
      currentObject = null;
      currentArray = (HutnArray) current;
    } else if (current == null && stack.size() == 0) {
      currentArray = null;
      currentObject = null;
    } else {
      throw new IllegalStateException("Current: " + current + " level: " + stack.size());
    }
  }

  @Override
  public int level() {
    return stack.size();
  }

  @Override
  public void writeBoolean(boolean value) {
    if (stack.size() == 0) {
      if (root != null) {
        throw new IllegalStateException("Root assigned already: " + root);
      }
      root = value;
    } else {
      currentArray.add(value);
    }
  }

  @Override
  public void writeBoolean(String name, boolean value) {
    currentObject.put(name, value);
  }

  @Override
  public void writeDouble(double value) {
    if (stack.size() == 0) {
      if (root != null) {
        throw new IllegalStateException("Root assigned already: " + root);
      }
      root = value;
    } else {
      currentArray.add(value);
    }
  }

  @Override
  public void writeDouble(String name, double value) {
    currentObject.put(name, value);
  }

  @Override
  public void writeLong(long value) {
    if (stack.size() == 0) {
      if (root != null) {
        throw new IllegalStateException("Root assigned already: " + root);
      }
      root = value;
    } else {
      currentArray.add((double) value);
    }
  }

  @Override
  public void writeLong(String name, long value) {
    currentObject.put(name, value);
  }

  @Override
  public void writeString(String value) {
    if (stack.size() == 0) {
      if (root != null) {
        throw new IllegalStateException("Root assigned already: " + root);
      }
      root = value;
    } else {
      currentArray.add(value);
    }
  }

  @Override
  public void writeString(String name, String value) {
    currentObject.put(name, value);
  }

  @Override
  public void startArray() {
    HutnArray array = new HutnArray();
    if (stack.size() == 0) {
      if (root != null) {
        throw new IllegalStateException("Root assigned already: " + root);
      }
      root = array;
    } else {
      currentArray.add(array);
    }
    stack.add(currentArray);
    currentArray = array;
  }

  @Override
  public void startArray(String name) {
    HutnArray array = new HutnArray();
    currentObject.put(name, array);
    stack.add(currentObject);
    currentObject = null;
    currentArray = array;
  }

  @Override
  public void startObject() {
    startTypedObject(null);
  }

  public void startTypedObject(String type) {
    HutnObject object = new HutnObject(type);
    if (stack.size() == 0) {
      if (root != null) {
        throw new IllegalStateException("Root assigned already: " + root);
      }
      root = object;
    } else {
      currentArray.add(object);
    }
    stack.add(currentArray);
    currentObject = object;
    currentArray = null;
  }

  @Override
  public void startObject(String name) {
    startTypedObject(name, null);
  }

  public void startTypedObject(String name, String type) {
    HutnObject object = new HutnObject();
    currentObject.put(name, object);
    stack.add(currentObject);
    currentObject = object;
  }
}
