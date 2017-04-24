package org.flowgrid.swt;

import org.flowgrid.model.io.Files;
import org.flowgrid.model.io.StatusListener;
import org.flowgrid.swt.dialog.ProgressDialog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Installer {
    private static final String[] MASTER_FILE_NAMES = {
            "flowgrid-examples-master.zip",
            "flowgrid-missions-master.zip",
            "flowgrid-myname-master.zip"
    };

    SwtFlowgrid flowgrid;
    Settings settings;
    ProgressDialog progressDialog;

    Installer(SwtFlowgrid flowgrid) {
        this.flowgrid = flowgrid;
        this.settings = flowgrid.settings();
    }


    private void clearMetadata() throws IOException {
        Files.deleteAll(flowgrid.cacheRoot(), "/", new StatusListener() {
            @Override
            public void log(String message) {
                message(message);
            }
        });
    }


    private void initializeFilesystem(Settings.BootCommand command, String path) throws IOException {
        clearMetadata();

        File storageRootDir = flowgrid.storageRoot();
        storageRootDir.mkdirs();

        if (command == Settings.BootCommand.DELETE_AND_RESTORE) {
            StatusListener statusListener = new StatusListener() {
                @Override
                public void log(String message) {
                    message("Deleting " + message);
                }
            };
            Files.deleteAll(storageRootDir, path, statusListener);
        }

        for (String master : MASTER_FILE_NAMES) {
            String resourceName = "/install/" + master;
            InputStream resourceInputStream = getClass().getResourceAsStream(resourceName);
            if (resourceInputStream == null) {
                throw new RuntimeException("Resource not found: '" + resourceName + "'");
            }
            ZipInputStream zis = new ZipInputStream(resourceInputStream);

            String prefix = path.startsWith("/") ? path.substring(1) : path;
            while(true) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }
                if (entry.isDirectory()) {
                    continue;
                }
                String localName = entry.getName();
                int cut = localName.indexOf('/');
                if (localName.startsWith("flowgrid-") && localName.substring(0, cut).endsWith("-master")) {
                    localName = localName.substring(9, cut - 7) + localName.substring(cut);
                }
                if (!localName.startsWith(prefix)) {
                    continue;
                }
                if (command == Settings.BootCommand.RESTORE_MISSING_FILES) {
                    if (Files.exists(storageRootDir, localName)) {
                        continue;
                    }
                }
                message(localName);
                OutputStream os = Files.save(storageRootDir, localName, entry.getTime());
                Files.copyStream(zis, os);
                os.close();
            }
        }

    }

    private void message(final String message) {
        flowgrid.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(message);
            }
        });
    }

    public void start() {
        final Settings.BootCommand bootCommand = settings.bootCommand();
        final String bootCommandPath = settings.bootCommandPath();
        if (bootCommand == null || bootCommand == Settings.BootCommand.NONE) {
            flowgrid.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    flowgrid.start();
                }
            });
        }
        settings.setBootCommand(Settings.BootCommand.NONE, null);
        progressDialog = new ProgressDialog(flowgrid.shell());
        progressDialog.setTitle("Boot Command: " + bootCommand);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (bootCommand) {
                        case INITIALIZE_FILESYSTEM:
                        case RESTORE_MISSING_FILES:
                        case RESTORE_CHANGED_FILES:
                        case DELETE_AND_RESTORE:
                            initializeFilesystem(bootCommand, bootCommandPath);
                            break;
                        case CLEAR_METATDATA:
                            clearMetadata();
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                flowgrid.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        flowgrid.start();
                    }
                });
            }
        }).start();
    }
}
