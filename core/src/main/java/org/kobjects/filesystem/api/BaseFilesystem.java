package org.kobjects.filesystem.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;

public abstract class BaseFilesystem implements Filesystem {
  protected HashSet<FilesystemListener> listeners = new HashSet<FilesystemListener>();
  
  @Override
  public void addListener(FilesystemListener listener) {
    listeners.add(listener);
  }
  
  protected void notifyFileChanged(String name, FilesystemListener.Action action, long timestamp, FilesystemListener... exclude) {
    for (FilesystemListener l: listeners) {
      boolean skip = false;
      for (FilesystemListener e: exclude) {
        if (e == l) {
          skip = true;
          break;
        }
      }
      if (!skip) {
        l.fileChanged(name, action, timestamp > 0 ? timestamp : System.currentTimeMillis());
      }
    }
  }

  @Override
  public void removeListener(FilesystemListener listener) {
    listeners.remove(listener);
  }
  
  @Override
  public OutputStream save(String name) throws IOException {
    return save(name, 0L);
  }
}
