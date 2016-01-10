package org.flowgrid.android;

import org.flowgrid.model.hutn.Hutn;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;
import org.flowgrid.model.hutn.HutnWriter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.StringWriter;

public class Settings {
  private static final String BOOT_COMMAND = "bootCommand";
  private static final String BOOT_COMMAND_PATH = "bootCommandPath";
  private static final String DEVELOPER_MODE = "developerMode";
  private static final String STORAGE_CONNECTIONS = "storageConnections";

  enum BootCommand {
    INITIALIZE_FILESYSTEM,
    RESTORE_MISSING_FILES,
    RESTORE_CHANGED_FILES,
    DELETE_AND_RESTORE,
    NONE,
    CLEAR_METATDATA,
 //   FORCE_SYNC
  }

  SharedPreferences preferences;

  Settings(Context context) {
    preferences = PreferenceManager.getDefaultSharedPreferences(context);
    
  }

  public BootCommand bootCommand() {
    return BootCommand.values()[preferences.getInt(BOOT_COMMAND, 0)];
  }

  public String bootCommandPath() {
    return preferences.getString(BOOT_COMMAND_PATH, "");
  }

  public boolean developerMode() {
    return preferences.getBoolean(DEVELOPER_MODE, false);
  }

  public void setDeveloperMode(boolean dm) {
    preferences.edit().putBoolean(DEVELOPER_MODE, dm).commit();
  }
  
  public void setBootCommand(BootCommand bootCommand, String path) {
    preferences.edit().putInt(BOOT_COMMAND, bootCommand.ordinal()).
        putString(BOOT_COMMAND_PATH, path == null ? "" : path).commit();
  }
  
  public void setStorageConnections(HutnObject json) {
    StringWriter sw = new StringWriter();
    HutnSerializer serializer = new HutnWriter(sw);
    Hutn.serialize(serializer, json);
    preferences.edit().putString(STORAGE_CONNECTIONS, sw.toString()).commit();
  }

  public HutnObject storageConnections() {
    String json = preferences.getString(STORAGE_CONNECTIONS, "{}");
    System.out.println("storage connection: " + json);
    HutnObject result;
    try {
      result = (HutnObject) Hutn.parse(json);
    } catch (Exception e) {
      e.printStackTrace();
      result = new HutnObject();
    }
    return result;
  }
  
}
