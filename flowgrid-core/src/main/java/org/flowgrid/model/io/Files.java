package org.flowgrid.model.io;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Files {
    private static final int BUFFER_SIZE = 65536;
    private static Map<String,Stat> mlsdCache;
    private static File mlsdCacheDir;


    public static void delete(File file) {
        if (file.isDirectory()) {
            for (File child: file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }

    public static void delete(File root, String path) {
        delete(new File(root, path));
    }

    public static InputStream load(File root, String name) throws IOException {
        File file = new File(root, name);

        if (!file.exists()) {
            throw new FileNotFoundException("File '" + name + "' does not exist.");
        }
        if (file.isDirectory()) {
            throw new IOException("File '" + name + "' is a directory.");
        }
        return new BufferedInputStream(new FileInputStream(file));
    }

    public static OutputStream save(File rootDir, String name) throws IOException {
        return save(rootDir, name, 0);
    }

    public static OutputStream save(final File rootDir, final String name, final long timestamp) throws IOException {
        final File file = new File(rootDir, name);
        if (!file.exists()) {
            File dir = file.getParentFile();
            dir.mkdirs();
        }
        return new BufferedOutputStream(new FileOutputStream(file)) {
            boolean closed = false;
            public void close() throws IOException {
                if (closed) {
                    return;
                }
                super.close();
                closed = true;
                setLastModified(rootDir, name, timestamp == 0 ? file.lastModified() : timestamp);
            }
        };
    }

    public static String[] list(File root, String path) {
        Set<String> fileNames = new TreeSet<String>();
        File dir = new File(root, path);
        if (dir.exists() && dir.isDirectory()) {
            for (File file: dir.listFiles()) {
                fileNames.add(file.isDirectory() ? file.getName() + "/" : file.getName());
            }
        }
        String[] result = new String[fileNames.size()];
        fileNames.toArray(result);
        return result;
    }

    private static synchronized Map<String,Stat> mlsd(File dir) throws IOException {
        if (dir.equals(mlsdCacheDir)) {
            return mlsdCache;
        }
        mlsdCacheDir = dir;
        mlsdCache = Mlsd.read(dir);
        return mlsdCache;
    }


    public static void copyAll(File sourceRoot, String sourcePath, File targetRoot, String targetPath, StatusListener statusListener) throws IOException {
      if (sourcePath.endsWith("/")) {
        if (statusListener != null) {
          statusListener.log("Copying " + sourcePath + " to " + targetPath);
        }
        if (!targetPath.endsWith("/")) {
          targetPath += "/";
        }
        for (String s: list(sourceRoot, sourcePath)) {
          copyAll(sourceRoot, sourcePath + s, targetRoot, targetPath + s, statusListener);
        }
      } else {
        InputStream is = load(sourceRoot, sourcePath);
        OutputStream os = save(targetRoot, targetPath);
        copyStream(is, os);
        close(os);
        close(is);
      }
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

    public static void deleteAll(File root, String path, StatusListener statusListener) throws IOException {
      if (path.endsWith("/")) {
        if (statusListener != null) {
          statusListener.log("Deleting " + path);
        }
        for (String s: list(root, path)) {
          deleteAll(root, path + s, statusListener);
        }
      }
      delete(root, path);
    }

    public static boolean exists(File root, String path) {
        return new File(root, path).exists();
    }

    public static Stat stat(File rootDir, String path) throws IOException {
        File file = new File(rootDir, path);
        File dir = file.getParentFile();
        if (dir == null) {
            return new Stat(Stat.Type.DIR, 0, 0);
        }
        Map<String, Stat> mlsd = mlsd(dir);
        return mlsd.get(file.getName());

    }


    public static void setLastModified(final File rootDir, final String name, final long timestamp) throws IOException {
        File file = new File(rootDir, name);
        long originalTimestamp = file.lastModified();
        boolean set = file.setLastModified(timestamp);
        long localTimestamp = new File(rootDir, name).lastModified();
        if (!set || Math.abs(localTimestamp - timestamp) > 4000) {
            System.err.println("Error setting timestamp to " + new Date(timestamp) + " original: " + new Date(originalTimestamp) + " set: " + set + " read value: " + new Date(localTimestamp));
            File dir = file.getParentFile();
            Map<String, Stat> mlsd = mlsd(dir);
            mlsd.put(file.getName(), new Stat(Stat.Type.FILE, file.length(), timestamp, localTimestamp));
            Mlsd.write(mlsd, new FileOutputStream(new File(dir, Mlsd.FILENAME)));
        }
    }
}
