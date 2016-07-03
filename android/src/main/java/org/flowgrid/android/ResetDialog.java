package org.flowgrid.android;


import android.app.AlertDialog;
import android.content.DialogInterface;

public class ResetDialog {
  private static final String[] OPTIONS = {
      "Restore deleted files only", "Overwrite all local changes", "Also delete added examples"};

  private static final Settings.BootCommand[] BOOT_COMMANDS = {
      Settings.BootCommand.RESTORE_MISSING_FILES,
      Settings.BootCommand.RESTORE_CHANGED_FILES,
      Settings.BootCommand.DELETE_AND_RESTORE,
  };

  public static void show(final MainActivity platform, final String path) {
    AlertDialog.Builder alert = new AlertDialog.Builder(platform);
    alert.setTitle("Restore " + path);
    final int[] option = new int[1];
    alert.setSingleChoiceItems(OPTIONS, 0, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        option[0] = which;
      }
    });
    alert.setNegativeButton("Cancel", null);
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        platform.reboot(BOOT_COMMANDS[option[0]], path);
      }
    });
    alert.show();
  }

}
