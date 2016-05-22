package org.flowgrid.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class AboutDialog {
  public static void show(final MainActivity platform) {
    AlertDialog.Builder alert = new AlertDialog.Builder(platform);
    alert.setTitle("About FlowGrid");
    TextView content = new TextView(platform);
    int padding = Views.px(platform, 24);
    final boolean[] restart = new boolean[1];
    content.setPadding(padding, padding, padding, padding);
    content.setText(platform.documentation("Copyright"));
    content.setOnClickListener(new OnClickListener() {
      int requiredClicks = 7;

      @Override
      public void onClick(View v) {
        if (--requiredClicks == 0 && !platform.settings().developerMode()) {
          platform.settings().setDeveloperMode(true);
          Toast toast = Toast.makeText(platform, "Developer mode enabled", Toast.LENGTH_LONG);
          restart[0] = true;
          toast.show();
        }
      }
    });
    alert.setView(content);
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (restart[0]) {
          platform.reboot(Settings.BootCommand.NONE, null);
        }
      }
    });
    if (platform.settings().developerMode()) {
      alert.setNeutralButton("Developer off", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          platform.settings().setDeveloperMode(false);
          platform.reboot(Settings.BootCommand.NONE, null);
        }
      });
    }
    alert.show();
  }
}
