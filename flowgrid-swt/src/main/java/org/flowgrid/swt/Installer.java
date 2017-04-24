package org.flowgrid.swt;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import org.flowgrid.model.io.Files;
import org.flowgrid.model.io.StatusListener;
import org.flowgrid.swt.dialog.ProgressDialog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Installer {
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

        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
                "/install/allfiles.txt"), "UTF-8"));

        while (true) {
            String localName = reader.readLine();
            if (localName == null) {
                break;
            }
            if (localName.equals("allfiles.txt") || localName.startsWith(".") || localName.equals("create_list.sh")) {
                continue;
            }
            String resourceName = "/install/" + localName;
            InputStream resourceInputStream = getClass().getResourceAsStream(resourceName);
            if (resourceInputStream == null) {
                System.err.println("Resource not found: '" +resourceName + "'. Update allfiles.txt?");
            }
            if (command == Settings.BootCommand.RESTORE_MISSING_FILES && Files.exists(storageRootDir, localName)) {
                continue;
            }
            message(localName);
            File target = new File(storageRootDir, localName);
            target.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(new File(storageRootDir, localName));
            Files.copyStream(resourceInputStream, os);
            resourceInputStream.close();
            os.close();
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
