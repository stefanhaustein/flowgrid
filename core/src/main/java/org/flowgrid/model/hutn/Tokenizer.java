package org.flowgrid.model.hutn;

import java.io.IOException;
import java.io.Reader;

/**
 * A tokenizer with the ability to read long strings in parts. The interface
 * is based on java.io.StreamTokenizer.
 */
public class Tokenizer {
  /**
   * Signals the end of the stream.
   */
  public static final int TT_EOF = -1;

  /**
   * A number value has been read to sval.
   */
  public static final int TT_NUMBER = -2;

  /**
   * Initial state before nextToken has been called.
   */
  public static final int TT_BOF = -4;

  /**
   * Identifier (true, false, null etc. or keywords)
   */
  public static final int TT_WORD = -5;

  /**
   * Complete string or last part of a sequence of partial strings.
   */
  public static final int TT_STRING = '"';

  /**
   * A partial string. The next token will be TT_PARTIAL_STRING or TT_STRING.
   */
  public static final int TT_PARTIAL_STRING = '\'';

  /**
   * The current token type. TT_BOF initially.
   */
  public int ttype = TT_BOF;

  /**
   * The current token string value, if applicable, or null for single-character token.
   */
  public String sval;

  private final Reader reader;
  private final int maxStringLength;
  private int colno;
  private int lineno = 1;
  private int nextChar;

  /**
   * Creates a Hutn tokenizer with the given maximum string length. Longer strings will
   * be reported as a sequence of PARTIAL_STRING and a final STRING token.
   */
  public Tokenizer(Reader reader, int maxStringLength) {
    this.reader = reader;
    this.maxStringLength = Math.max(1, maxStringLength);
  }

  /**
   * Creates a Hutn tokenizer without an artificial string length limit. Strings will
   * be reported as a single token.
   */
  public Tokenizer(Reader reader) {
    this(reader, Integer.MAX_VALUE);
  }

  /**
   * Advance to the next token and return its type (which will also be contained in ttype).
   * The string value for multi-character token will be available in sval.
   */
  public int nextToken() throws IOException {
    if (ttype == TT_PARTIAL_STRING) {
      readString();
      return ttype;
    }
    // Skip whitespace
    while (nextChar <= ' ' && nextChar != -1) {
      nextChar = read();
    }
    switch (nextChar) {
      case -1:
        return ttype = TT_EOF;
      case '{':
      case '}':
      case ':':
      case '[':
      case ']':
      case ',':
        sval = null;
        ttype = nextChar;
        nextChar = read();
        break;
      case '"':
        nextChar = read();
        readString();
        break;
      case '-':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        readNumeric();
        break;
      default:
        readIdentifier();
    }
    return ttype;
  }

  /**
   * Returns a new runtime exception with the given text and a description of the current
   * position in the stream.
   */
  public RuntimeException createException(String text) {
    return new RuntimeException(text + " Position " + lineno + ":" + colno + " Char: " + (char) nextChar);
  }

  private int read() throws IOException {
    int i = reader.read();
    if (i == '\n') {
      lineno++;
      colno = 1;
    } else {
      colno++;
    }
    return i;
  }

  private void readIdentifier() throws IOException {
    StringBuilder sb = new StringBuilder();
    while (nextChar >= 'a' && nextChar <= 'z'
        || nextChar >= 'A' && nextChar <= 'Z'
        || nextChar >= '0' && nextChar <= '9'
        || nextChar == '-' || nextChar == '_' || nextChar == '.' || nextChar == '/') {
      sb.append((char) nextChar);
      nextChar = read();
    }
    if (sb.length() == 0) {
      throw createException("Illegal character: '" + nextChar + "'");
    }
    sval = sb.toString();
    ttype = TT_WORD;
  }

  private void readNumeric() throws IOException {
    // TODO(haustein): This accepts too much.
    StringBuilder sb = new StringBuilder();
    do {
      sb.append((char) nextChar);
      nextChar = read();
    } while ((nextChar >= '0' && nextChar <= '9') || nextChar == '-' ||
        nextChar == 'e' || nextChar == 'E' || nextChar == '.');
    sval = sb.toString();
    ttype = TT_NUMBER;
  }

  private void readString() throws IOException {
    StringBuilder sb = new StringBuilder();
    while (nextChar != '"' && sb.length() < maxStringLength) {
      if (nextChar == -1) {
        throw createException("Unexpected EOF inside String literal.");
      }
      if (nextChar < ' ') {
        throw createException("Unexpected control character inside String literal.");
      }
      if (nextChar == '\\') {
        sb.append(readEscaped());
      } else {
        sb.append((char) nextChar);
      }
      nextChar = read();
    }
    sval = sb.toString();
    if (nextChar == '"') {
      ttype = TT_STRING;
      nextChar = read();
    } else {
      ttype = TT_PARTIAL_STRING;
    }
  }

  private char readEscaped() throws IOException {
    int c = read();
    switch (c) {
      case '"': return '"';
      case '\\': return '\\';
      case '/': return '/';
      case 'b': return '\b';
      case 'f': return '\f';
      case 'n': return '\n';
      case 'r': return '\r';
      case '\n': return '\n';
      case 'u':
        char h1 = (char) read();
        char h2 = (char) read();
        char h3 = (char) read();
        char h4 = (char) read();
        return (char) Integer.parseInt("" + h1 + h2 + h3 + h4, 16);
      default:
        throw createException("Unrecognized escape: " + (char) c);
    }
  }
}
