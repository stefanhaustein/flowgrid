package org.flowgrid.model.hutn;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * Parses to subclasses of regular java lists and maps with direct access methods
 * for simple values.
 */
public class Hutn {

  private static void serializeArrayBody(HutnSerializer writer, List<?> array) {
    for (int i = 0; i < array.size(); i++) {
      Object value = array.get(i);
      if (value instanceof String) {
        writer.writeString((String) value);
      } else if (value instanceof Number) {
        writer.writeDouble(((Number) value).doubleValue());
      } else if (value instanceof Boolean) {
        writer.writeBoolean(((Boolean) value).booleanValue());
      } else if (value instanceof HutnObject) {
        serialize(writer, (HutnObject) value);
      } else if (value instanceof HutnArray) {
        serialize(writer, (HutnArray) value);
      } else {
        throw new RuntimeException("Unrecognized: " + value.getClass());
      }
    }
  }

  private static void serializeObjectBody(HutnSerializer writer, Map<String,?> object) {
    for (Map.Entry<String, ?> entry: object.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof String) {
        writer.writeString(key, (String) value);
      } else if (value instanceof Number) {
        writer.writeDouble(key, ((Number) value).doubleValue());
      } else if (value instanceof Boolean) {
        writer.writeBoolean(key, ((Boolean) value).booleanValue());
      } else if (value instanceof HutnObject) {
        serialize(writer, key, (HutnObject) value);
      } else if (value instanceof HutnArray) {
        serialize(writer, key, (HutnArray) value);
      } else if (value != null) {
        throw new RuntimeException("Unrecognized: " + value.getClass());
      }
    }
  }

  public static void serialize(HutnSerializer writer, List<?> array) {
    writer.startArray();
    serializeArrayBody(writer, array);
    writer.endArray();
  }

  public static void serialize(HutnSerializer writer, String name, List<?> array) {
    if (name == null) {
      writer.startArray();
    } else {
      writer.startArray(name);
    }
    serializeArrayBody(writer, array);
    writer.endArray();
  }

  public static void serialize(HutnSerializer writer, Map<String, ?> jsonObject) {
    writer.startObject();
    serializeObjectBody(writer, jsonObject);
    writer.endObject();
  }

  public static void serialize(HutnSerializer writer, String name, Map<String, ?> jsonObject) {
    if (name == null) {
      writer.startObject();
    } else {
      writer.startObject(name);
    }
    serializeObjectBody(writer, jsonObject);
    writer.endObject();
  }


  public static Object parse(Reader r) {
    try {
      Tokenizer tokenizer = new Tokenizer(r);
      tokenizer.nextToken();
      Object result = new Parser(tokenizer).parseAny();
      if (tokenizer.ttype != StreamTokenizer.TT_EOF) {
        throw new IllegalStateException("EOF expected");
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Object parse(String s) {
    return parse(new StringReader(s));
  }


  private static class Parser {
    Tokenizer tokenizer;

    private Parser(Tokenizer tokenizer) {
      this.tokenizer = tokenizer;
    }

    // Precondition: On value
    // Postcondition: Value consumed.
    public Object parseAny() throws IOException {
      Object result;
      String sval = tokenizer.sval;
      switch (tokenizer.ttype) {
        case '[':
          result = parseArray();
          break;
        case '{':
          result = parseObject(null);
          break;
        case '"':
          tokenizer.nextToken();
          if (tokenizer.ttype == '{') {
            result = parseObject(sval);
          } else {
            result = sval;
          }
          break;
        case Tokenizer.TT_WORD:
          tokenizer.nextToken();
          if (tokenizer.ttype == '{') {
            result = parseObject(sval);
          } else if (sval.equals("true")) {
            result = Boolean.TRUE;
          } else if (sval.equals("false")) {
            result = Boolean.FALSE;
          } else if (sval.equals("null")) {
            result = null;
          } else {
            throw tokenizer.createException("Unexpected identifier: '" + tokenizer.sval);
          }

          break;
        case Tokenizer.TT_NUMBER:
          try {
            result = Long.parseLong(sval);
          } catch(NumberFormatException e) {
            try {
              result = Double.parseDouble(sval);
            } catch (NumberFormatException e2) {
              throw tokenizer.createException("Error parsing number: " + e2.getMessage());
            }
          }
          tokenizer.nextToken();
          break;
        default:
          throw tokenizer.createException("Unexpected token.");
      }
      return result;
    }

    // Precondition: On '{'
    // Postcondition: behind '}'
    private HutnObject parseObject(String type) throws IOException {
      HutnObject result = new HutnObject(type);
      tokenizer.nextToken();  // Skip '{';
      while (tokenizer.ttype != '}') {
        if (tokenizer.ttype != '"') {
          throw tokenizer.createException("Quote expected.");
        }
        String key = tokenizer.sval;
        if (tokenizer.nextToken() != ':') {
          throw tokenizer.createException("Colon expected after '" + key);
        }
        tokenizer.nextToken();
        Object value = parseAny();
        result.put(key, value);
        if (tokenizer.ttype != ',') {
          break;
        }
        tokenizer.nextToken();
      }
      if (tokenizer.ttype != '}') {
        throw tokenizer.createException("'}' expected.");
      }
      tokenizer.nextToken();
      return result;
    }

    // Precondition: On '['
    // Postcondition: behind ']'
    private HutnArray parseArray() throws IOException {
      HutnArray result = new HutnArray();
      tokenizer.nextToken();  // Skip '[';
      while (tokenizer.ttype != ']') {
        Object value = parseAny();
        result.add(value);
        if (tokenizer.ttype != ',') {
          break;
        }
        tokenizer.nextToken();
      }
      if (tokenizer.ttype != ']') {
        throw new IllegalStateException("']' expected, got " + tokenizer.ttype);
      }
      tokenizer.nextToken(); // consume ']'
      return result;
    }
  }

}
