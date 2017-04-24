package org.flowgrid.swt;


import org.flowgrid.model.Artifact;
import org.flowgrid.model.hutn.Hutn;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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

    File file;
    HutnObject data;

    Settings(File file) {
        this.file = file;
        if (file.exists()) {
            try {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
                data = (HutnObject) Hutn.parse(reader);
                reader.close();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        data = new HutnObject();
    }

    public BootCommand bootCommand() {
        return BootCommand.values()[data.getInt(BOOT_COMMAND, 0)];
    }

    public String bootCommandPath() {
        return data.getString(BOOT_COMMAND_PATH, "");
    }

    public boolean developerMode() {
        return data.getBoolean(DEVELOPER_MODE, false);
    }

    public void setDeveloperMode(boolean dm) {
        data.put(DEVELOPER_MODE, dm);
        flush();
    }

    public void setBootCommand(BootCommand bootCommand, String path) {
        data.put(BOOT_COMMAND, bootCommand.ordinal());
        data.put(BOOT_COMMAND_PATH, path == null ? "" : path);
        flush();
    }

    public void setLastUsed(Artifact artifact) {
        data.put(LAST_USED, artifact.qualifiedName());
        flush();
    }

    public String getLastUsed() {
        return data.getString(LAST_USED, "examples/algorithm/factorial");
    }

    void flush() {
        try {
            HutnWriter writer = new HutnWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            Hutn.serialize(writer, data);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
