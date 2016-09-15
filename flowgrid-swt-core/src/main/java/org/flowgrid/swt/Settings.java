package org.flowgrid.swt;


import org.flowgrid.model.Artifact;

import java.util.prefs.Preferences;

public class Settings {

    private static final String BOOT_COMMAND = "bootCommand";
    private static final String BOOT_COMMAND_PATH = "bootCommandPath";
    private static final String DEVELOPER_MODE = "developerMode";
    private static final String STORAGE_CONNECTIONS = "storageConnections";
    private static final String LAST_USED = "lastUsed";

    enum BootCommand {
        INITIALIZE_FILESYSTEM,
        RESTORE_MISSING_FILES,
        RESTORE_CHANGED_FILES,
        DELETE_AND_RESTORE,
        NONE,
        CLEAR_METATDATA,
        //   FORCE_SYNC
    }

    Preferences preferences;

    Settings() {
        preferences = Preferences.userNodeForPackage(SwtFlowgrid.class);
    }

    public BootCommand bootCommand() {
        return BootCommand.values()[preferences.getInt(BOOT_COMMAND, 0)];
    }

    public String bootCommandPath() {
        return preferences.get(BOOT_COMMAND_PATH, "");
    }

    public boolean developerMode() {
        return preferences.getBoolean(DEVELOPER_MODE, false);
    }

    public void setDeveloperMode(boolean dm) {
        preferences.putBoolean(DEVELOPER_MODE, dm);
    }

    public void setBootCommand(BootCommand bootCommand, String path) {
        preferences.putInt(BOOT_COMMAND, bootCommand.ordinal());
        preferences.put(BOOT_COMMAND_PATH, path == null ? "" : path);
    }

    public void setLastUsed(Artifact artifact) {
        preferences.put(LAST_USED, artifact.qualifiedName());
    }

    public String getLastUsed() {
        return preferences.get(LAST_USED, "examples/algorithm/factorial");
    }

    /*
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
    */
}
