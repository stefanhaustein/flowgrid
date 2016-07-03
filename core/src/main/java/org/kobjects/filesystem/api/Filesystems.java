package org.kobjects.filesystem.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Some static filesystem utilities.
 */
public class Filesystems {
  private static final int BUFFER_SIZE = 65536;
  
  public static void deleteAll(Filesystem fs, String path, StatusListener statusListener) throws IOException {
    if (path.endsWith("/")) {
      if (statusListener != null) {
        statusListener.log("Deleting " + path);
      }
      for (String s: fs.list(path)) {
        deleteAll(fs, path + s, statusListener);
      }
    }
    fs.delete(path);
  }


  public static void copyAll(Filesystem sourceFs, String sourcePath, Filesystem targetFs, String targetPath, StatusListener statusListener) throws IOException {
    if (sourcePath.endsWith("/")) {
      if (statusListener != null) {
        statusListener.log("Copying " + sourcePath + " to " + targetPath);
      }
      if (!targetPath.endsWith("/")) {
        targetPath += "/";
      }
      for (String s: sourceFs.list(sourcePath)) {
        copyAll(sourceFs, sourcePath + s, targetFs, targetPath + s, statusListener);
      }
    } else {
      InputStream is = sourceFs.load(sourcePath);
      OutputStream os = targetFs.save(targetPath);
      copyStream(is, os);
      close(os);
      close(is);
    }
  }

  
  /**
   * None of the streams will be closed. 
   * @throws IOException 
   */
  public static void copyStream(InputStream is, OutputStream os) throws IOException {
    byte[] buf = new byte[BUFFER_SIZE];
    while (true) {
      int count = is.read(buf);
      if (count <= 0) {
        break;
      }
      os.write(buf, 0, count);
    }
  }

  public static void close(InputStream is) {
    if (is == null) {
      return;
    }
    try {
      is.close();
    } catch(IOException e) {
    }
  }

  public static void close(OutputStream os) {
    if (os == null) {
      return;
    }
    try {
      os.close();
    } catch(IOException e) {
    }
  }

  public static long streamLength(InputStream is) throws IOException {
    long total = 0;
    while (true) {
      long skipped = is.skip(BUFFER_SIZE);  // MAX_INT doesn't work :(
      if (skipped == 0) {
        break;
      }
      total += skipped;
    }
    is.close();
    return total;
  }
  
  public static String normalizeDirectoryPath(String path) {
    path = normalizePath(path);
    return !path.isEmpty() && !path.endsWith("/") ? path + "/" : path;
  }
  
  public static String normalizeFilePath(String path) {
    path = normalizePath(path);
    return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
  }

  // Remove leading slashes and double slashes. Keep slashes at the end
  // (unless they are also in the front)
  // "/foo/" -> "foo/" , "///" -> "", "///foo" -> "foo"
  public static String normalizePath(String path) {
    int pos = 0;
    int len = path.length();
    while (pos < len && path.charAt(pos) == '/') {
      pos++;
    }
    path = path.substring(pos);
    do {
      len = path.length();
      path = path.replace("//", "/");
    } while(path.length() != len);
    return path;
  }


  public static byte[] loadFully(InputStream is) throws IOException {
    byte[] buf = new byte[8096];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while (true) {
      int count = is.read(buf);
      if (count <= 0) {
        break;
       }
      baos.write(buf, 0, count);
    }
    return baos.toByteArray();
  }

}
