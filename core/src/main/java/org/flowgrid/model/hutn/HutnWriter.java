package org.flowgrid.model.hutn;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by haustein on 02.01.16.
 */
public class HutnWriter implements HutnSerializer {
  private static final String[] SPACES = {
      " ", "  ", "   ", "    ", "     ", "      ", "       ", "        "};

  private enum State {
    OBJECT, ARRAY, ROOT
  }

  private final ArrayList<State> stack = new ArrayList<>();
  private final Writer writer;
  private State state = State.ROOT;
  private boolean firstElement = true;

  public HutnWriter(Writer writer) {
    this.writer = writer;
  }

  private void writeQuoted(String s) throws IOException {
    writer.write('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\n':
          writer.write("\\n");
          break;
        case '\r':
          writer.write("\\r");
          break;
        case '"':
          writer.write("\\\"");
          break;
        case '\\':
          writer.write("\\\\");
        default:
          writer.write(c);
      }
    }
    writer.write('"');
  }

  public void assertLevel(int level) {
    if (level != stack.size()) {
      throw new IllegalStateException("Expected nesting level: " + level + " actual: " + stack.size());
    }
  }

  @Override
  public int level() {
    return stack.size();
  }

  private void indent() throws IOException {
    writer.write('\n');
    int indent = stack.size();
    int tabs = indent / 8;
    int spaces = indent % 8;
    for (int i = 0; i < tabs; i++) {
      writer.write('\t');
    }
    writer.write(SPACES[indent % 8]);
  }

  private void writeKey(String key) throws IOException {
    if (state != State.OBJECT) {
      throw new IllegalStateException("Expected OBJECT state, got: " + state);
    }
    if (firstElement) {
      firstElement = false;
    } else {
      writer.write(',');
    }
    indent();
    writeQuoted(key);
    writer.write(": ");
  }

  @Override
  public void writeBoolean(String name, boolean value) {
    try {
      writeKey(name);
      writer.write(String.valueOf(value));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeLong(String name, long value) {
    try {
      writeKey(name);
      writer.write(String.valueOf(value));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeString(String name, String value) {
    if (value != null) {
      try {
        writeKey(name);
        writeQuoted(value);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeDouble(String name, double value) {
    try {
      writeKey(name);
      writer.write(String.valueOf(value));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void startArray(String name) {
    try {
      writeKey(name);
      writer.write('[');
      stack.add(state);
      state = State.ARRAY;
      firstElement = true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void startObject(String name) {
    startTypedObject(name, null);
  }

  public void startTypedObject(String name, String type) {
    try {
      writeKey(name);
      if (type != null) {
        writeQuoted(type);
        writer.write(' ');
      }
      writer.write('{');
      stack.add(state);
      state = State.OBJECT;
      firstElement = true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void next() throws IOException {
    if (state != State.ARRAY && state != State.ROOT) {
      throw new IllegalStateException("Expected ARRAY or ROOT state, got: " + state);
    }
    if (firstElement) {
      firstElement = false;
    } else {
      writer.write(',');
    }
    indent();
  }

  @Override
  public void writeBoolean(boolean value) {
    try {
      next();
      writer.write(String.valueOf(value));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeLong(long value) {
    try {
      next();
      writer.write(String.valueOf(value));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeString(String value) {
    try {
      next();
      if (value == null) {
        writer.write("null");
      } else {
        writeQuoted(value);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeDouble(double value) {
    try {
      next();
      writer.write(String.valueOf(value));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public void startArray() {
    try {
      next();
      writer.write('[');
      stack.add(state);
      state = State.ARRAY;
      firstElement = true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void startObject() {
    startTypedObject(null);
  }

  @Override
  public void startTypedObject(String type) {
    try {
      next();
      if (type != null) {
        writeQuoted(type);
        writer.write(' ');
      }
      writer.write('{');
      stack.add(state);
      state = State.OBJECT;
      firstElement = true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void endArray() {
    try {
      if (state != State.ARRAY) {
        throw new IllegalArgumentException();
      }
      state = stack.remove(stack.size() - 1);
      writer.write('\n');
      indent();
      writer.write(']');
      firstElement = false;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void endObject() {
    try {
      if (state != State.OBJECT) {
        throw new IllegalArgumentException();
      }
      state = stack.remove(stack.size() - 1);
      indent();
      writer.write('}');
      firstElement = false;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Writer close() {
    try {
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return writer;
  }
}
