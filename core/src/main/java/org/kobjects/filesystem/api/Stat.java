package org.kobjects.filesystem.api;

import java.util.Date;
import java.util.Locale;

public class Stat {
  public enum Type {DIR, FILE, ERROR}
  private final Type type;
  private final long modify;
  private final long size;
  private final long localModify;
  
  public Stat(Type type, long size, long modify) {
    this.type = type;
    this.size = size;
    this.modify = modify;
    this.localModify = modify;
  }

  public Stat(Type type, long size, long modify, long localModify) {
    this.type = type;
    this.size = size;
    this.modify = modify;
    this.localModify = localModify;
  }

  public long length() {
    return size;
  }

  public long lastModified() {
    return modify;
  }

  public long lastModifiedLocally() {
    return localModify;
  }

  public boolean isDirectory() {
    return type == Type.DIR;
  }

  public boolean isFile() {
    return type == Type.FILE;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Type=").append(type.toString().toLowerCase(Locale.US)).append(';');
    if (type != Type.ERROR) {
      sb.append("Modify=").append(Mlsd.DATE_FORMAT.format(new Date(modify))).append(';');
      if (localModify != modify && localModify != 0) {
        sb.append("X.LocalModify=").append(Mlsd.DATE_FORMAT.format(new Date(localModify))).append(';');
      }
      if (type == Type.FILE) {
        sb.append("Size=").append(size).append(';');
      }
    }
    return sb.toString();
  }
}
