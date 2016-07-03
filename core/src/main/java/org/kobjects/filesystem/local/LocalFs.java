package org.kobjects.filesystem.local;

import org.kobjects.filesystem.api.BaseFilesystem;
import org.kobjects.filesystem.api.FilesystemListener;
import org.kobjects.filesystem.api.FilesystemListener.Action;
import org.kobjects.filesystem.api.Mlsd;
import org.kobjects.filesystem.api.Stat;
import org.kobjects.filesystem.api.Stat.Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Local file system implementation. If the file time stamp cannot be set, it's stored separately
 * in a mlsd file.
 */
public class LocalFs extends BaseFilesystem {

  final private File rootDir;
  Map<String,Stat> mlsdCache;
  File mlsdCacheDir;
  private boolean filestampBug;

  public LocalFs(File rootDir) {
    this.rootDir = rootDir;
  }

  private static boolean exists(File f) {
    File parent = f.getParentFile();
    if (parent != null) {
      if (!exists(parent)) {
        return false;
      }
    }
    return f.exists();
  }

  @Override
  public InputStream load(final String name) throws IOException {
    File file = new File(rootDir, name);
    if (!exists(file)) {
      throw new FileNotFoundException("File '" + name + "' does not exist.");
    }
    if (file.isDirectory()) {
      throw new IOException("File '" + name + "' is a directory.");
    }
    return new FileInputStream(file);
  }

  private Map<String,Stat> mlsd(File dir) throws IOException {
    if (dir.equals(mlsdCacheDir)) {
      return mlsdCache;
    }
    mlsdCacheDir = dir;
    mlsdCache = Mlsd.read(dir);
    return mlsdCache;
  }
  
  @Override
  public Stat stat(final String name) throws IOException {
    File file = new File(rootDir, name);
    File dir = file.getParentFile();
    if (dir == null) {
      return new Stat(Type.DIR, 0, 0);
    }
    Map<String, Stat> mlsd = mlsd(dir);
    return mlsd.get(file.getName());
  }
 
  
  @Override
  public OutputStream save(final String name, final long timestamp, final FilesystemListener... dontNotify) throws IOException {
    final File file = new File(rootDir, name);
    final Action action = file.exists() ? Action.CHANGED : Action.ADDED;
    if (!file.exists()) {
      File dir = file.getParentFile();
      dir.mkdirs();
    }
    return new FileOutputStream(file) {
      boolean closed = false;
      public void close() throws IOException {
        if (closed) {
          return;
        }
        super.close();
        closed = true;
        setLastModified(name, timestamp == 0 ? file.lastModified() : timestamp);

        notifyFileChanged(name, action, timestamp, dontNotify);
      }
    };
  }

  @Override
  public String[] list(final String path) throws IOException {
    Set<String> fileNames = new TreeSet<String>();
    File dir = new File(rootDir, path);
    if (dir.exists() && dir.isDirectory()) {
      for (File file: dir.listFiles()) {
        fileNames.add(file.isDirectory() ? file.getName() + "/" : file.getName());
      }
    }
    String[] result = new String[fileNames.size()];
    fileNames.toArray(result);
    return result;
  }

  @Override
  public void delete(final String name, final FilesystemListener... dontNotify) throws IOException {
    mlsdCacheDir = null;
    File file = new File(rootDir, name);
    if (file.exists()) {
      file.delete();
      notifyFileChanged(name, Action.DELETED, 0, dontNotify);
    }
  }

  void setLastModified(final String name, final long timestamp) throws IOException {
    File file = new File(rootDir, name);
    long originalTimestamp = file.lastModified();
    boolean set = file.setLastModified(timestamp);
    long localTimestamp = new File(rootDir, name).lastModified();
    if (filestampBug || !set || Math.abs(localTimestamp - timestamp) > 4000) {
      System.err.println("Error setting timestamp to " + new Date(timestamp) + " original: " + new Date(originalTimestamp) + " set: " + set + " read value: " + new Date(localTimestamp));
      File dir = file.getParentFile();
      Map<String, Stat> mlsd = mlsd(dir);
      filestampBug = true;
      mlsd.put(file.getName(), new Stat(Type.FILE, file.length(), timestamp, localTimestamp));
      Mlsd.write(mlsd, new FileOutputStream(new File(dir, Mlsd.FILENAME)));
    }
  }
}
