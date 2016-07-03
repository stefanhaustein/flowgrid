package org.kobjects.filesystem.api;

public interface FilesystemListener {
  public enum Action {
    ADDED, CHANGED, DELETED
  }
  public void fileChanged(String name, Action action, long timestamp);
}
