package org.flowgrid.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import org.kobjects.filesystem.api.Filesystems;
import org.kobjects.filesystem.api.StatusListener;
import org.kobjects.filesystem.local.LocalFs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BootActivity extends Activity {
  Settings settings;
  ProgressDialog progressDialog;

  private void clearMetadata() throws IOException {
    File metadataRootDir = new File(getExternalFilesDir(null), "metadata");
    LocalFs metadataFilesystem = new LocalFs(metadataRootDir);
    Filesystems.deleteAll(metadataFilesystem, "/", new StatusListener() {
      @Override
      public void log(String message) {
        message(message);
      }
    });
  }


  private void initializeFilesystem(Settings.BootCommand command, String path) throws IOException {
    clearMetadata();

    File storageRootDir = new File(getExternalFilesDir(null), "flowgrid");
    LocalFs localFilesystem = new LocalFs(storageRootDir);

    if (command == Settings.BootCommand.DELETE_AND_RESTORE) {
      StatusListener statusListener = new StatusListener() {
        @Override
        public void log(String message) {
          message("Deleting " + message);
        }
      };
      Filesystems.deleteAll(localFilesystem, path, statusListener);
    }

    ZipInputStream zis = new ZipInputStream(getAssets().open("flowgrid.zip"));

    String prefix = path.startsWith("/") ? path.substring(1) : path;
    while(true) {
      ZipEntry entry = zis.getNextEntry();
      if (entry == null) {
        break;
      }
      if (entry.isDirectory() || !entry.getName().startsWith(prefix)) {
        continue;
      }
      if (command == Settings.BootCommand.RESTORE_MISSING_FILES) {
        if (localFilesystem.stat(entry.getName()) != null) {
          continue;
        }
      }
      message(entry.getName());
      OutputStream os = localFilesystem.save(entry.getName(), entry.getTime());
      Filesystems.copyStream(zis, os);
      os.close();
    }

  }

  private void message(final String message) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        progressDialog.setMessage(message);
      }
    });
  }

  protected void onCreate(final Bundle savedState) {
    super.onCreate(savedState);

    settings = new Settings(this);
    final Settings.BootCommand bootCommand = settings.bootCommand();
    final String bootCommandPath = settings.bootCommandPath();
    if (bootCommand == null || bootCommand == Settings.BootCommand.NONE) {
      startMainActivity();
      return;
    }
    settings.setBootCommand(Settings.BootCommand.NONE, null);
    progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Boot Command: " + bootCommand);
    progressDialog.setIndeterminate(true);
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
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            progressDialog.dismiss();
            startMainActivity();
          }
        });
      }
    }).start();
  }

  private void startMainActivity() {
    Intent intent = new Intent(BootActivity.this, MainActivity.class);
    startActivity(intent);
    finish();
  }

}
