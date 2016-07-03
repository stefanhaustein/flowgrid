package org.kobjects.filesystem.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Simple file system interface.
 */
public interface Filesystem {

  void addListener(FilesystemListener listener);
  
  /**
   * Throws an IO exception if the file does not exist or if it is a 
   * directory.
   */
  InputStream load(String name) throws IOException;
  
  /**
   * Provides metadata such as the file size and time stamp.
   * Does not throw an exception if the file does not exist. 
   * In this case, null will be returned.
   */
  Stat stat(String name) throws IOException;
  
  /** 
   * Provides an output stream for writing to the file at the 
   * given path. If the file or any directory on the path do not exist,
   * they will be created implicitly.
   */
  OutputStream save(String name) throws IOException;
  
  /** 
   * Provides an output stream for writing to the file at the 
   * given path. If the file or any directory on the path do not exist,
   * they will be created implicitly.
   */
  OutputStream save(String name, long timestamp, FilesystemListener... dontNotify) throws IOException;
  
  /**
   * Provides a list of files in the given directory. Subdirectories
   * are denoted by a slash appended to their name.
   */
  String[] list(String path) throws IOException;

  /**
   * Delete the given file.
   */
  void delete(String name, FilesystemListener... dontNotify) throws IOException;
  
  
  /**
   * Sets the modification date for the given file.
   * Useful in synced file system to keep track of the original 
   * modification time.

  public void setLastModified(String name, long timestamp) throws IOException;
   */
  void removeListener(FilesystemListener listener);
}
