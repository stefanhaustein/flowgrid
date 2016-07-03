package org.kobjects.filesystem.api;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * A collection of methods for handling the RFC 3659 MLSD format.
 */
public class Mlsd {
  public static final String FILENAME = "dir.mlsd";
  static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
  static {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
  
  
  public static Map<String,Stat> read(File dir) throws IOException {
    File mlsdFile = new File(dir, FILENAME);
    Map<String, Stat> result = new TreeMap<String, Stat>();
    File[] files = dir.listFiles();
    if (files != null) {
      Map<String, Stat> mlsd = mlsdFile.exists() ? parse(new BufferedInputStream(new FileInputStream(mlsdFile))) : null;
      for (File file: dir.listFiles()) {
        if (!file.getName().equals(FILENAME) && (file.isFile() || file.isDirectory())) {
          long timestamp = file.lastModified();
          if (mlsd != null) {
            Stat mlsdStat = mlsd.get(file.getName());
            if (mlsdStat != null) {
              if (timestamp != mlsdStat.lastModifiedLocally()) {
                System.err.println("mlsd timestamp mismatch for " + file.getAbsolutePath() + " actual timestamp: " + new Date(timestamp) + " != mlsd modified locally: " + new Date(mlsdStat.lastModifiedLocally()));
              }
              if (file.length() != mlsdStat.length()) {
                System.err.println("mlsd timestamp for " + file.getAbsolutePath() + " ignored b/c actual size: " + file.length() + " != mlsd size: " + mlsdStat.length());
              } else {
                timestamp = mlsdStat.lastModified();
              }
            }
          }
          result.put(file.getName(), new Stat(
                  file.isDirectory() ? Stat.Type.DIR : Stat.Type.FILE, file.length(), timestamp, file.lastModified()));
        }
      }
    }
    return result;
  }
  
  
  /**
   * Writes a MLSD-formatted directory listing for the given directory to
   * the given stream.
   */
  public static void write(Map<String,Stat> mlsd, OutputStream os) throws IOException {
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
    for (Map.Entry<String, Stat> entry: mlsd.entrySet()) {
      writer.write(entry.getValue().toString() + " " + entry.getKey() + "\r\n");
    }
    writer.close();
  }
  
  
  /**
   * Recursively generates ".mlsd" files for the given directory and 
   * its sub-directories.
   */
  public static void generate(File dir) throws IOException {
    System.out.println("Generating " + FILENAME + " for " + dir.getAbsolutePath());
    for (File f: dir.listFiles()) {
      if (f.isDirectory()) {
        generate(f);
      }
    }
    write(read(dir), new FileOutputStream(new File(dir, FILENAME)));
  }
  
  /**
   * Parses the "facts" part of an MLSD directory entry.
   */
  public static Stat parseFacts(String facts) {
    long modify = 0;
    long localModify = 0;
    Stat.Type type = Stat.Type.ERROR;
    long size = 0;
    
    for (String fact: facts.split(";")) {
      int cut = fact.indexOf('=');
      if (cut != -1) {
        String key = fact.substring(0, cut);
        String value = fact.substring(cut + 1);
        if ("Type".equals(key)) {
          if (value.equals("dir")) {
            type = Stat.Type.DIR;
          } else if (value.equals("file")) {
            type = Stat.Type.FILE;
          }
        } else if ("Modify".equals(key) || "X.LocalModify".equals(key)) {
          int dot = value.indexOf('.');
          if (dot != -1) {
            value = value.substring(0, dot);
          }
          try {
            long m = DATE_FORMAT.parse(value).getTime();
            if ("Modify".equals(key)) {
              modify = m;
            } else {
              localModify = m;
            }
          } catch (ParseException e) {
          }
        } else if ("Size".equals(key)) {
          try {
            size = Integer.parseInt(value);
          } catch (Exception e) {
          }
        }
      }
    }
    return new Stat(type, size, modify, localModify == 0 ? modify : localModify);
  }
  
  
  public static Map<String,Stat> parse(InputStream is) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    TreeMap<String,Stat> result = new TreeMap<String, Stat>();
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        return result;
      }
      line = line.trim();
      int cut = line.indexOf(' ');
      if (cut != -1) {
        String name = line.substring(cut + 1).trim();
        String facts = line.substring(0, cut);
        Stat stat = parseFacts(facts);
        result.put(name, stat);
      }
    }
  }
  
  public static void main(String[] args) throws IOException {
    generate(new File(args[0]));
  }
}
