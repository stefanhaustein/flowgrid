package org.flowgrid.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import org.flowgrid.android.module.ModuleMenu;
import org.flowgrid.android.widget.ColumnLayout;
import org.flowgrid.android.widget.CallbackSpinner;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Container;
import org.flowgrid.model.Module;
import org.flowgrid.model.io.StatusListener;

public class MoveDialog {

  public static void show(final ArtifactFragment fragment, final Artifact artifact) {
    final MainActivity platform = fragment.platform();
    AlertDialog.Builder alert = new AlertDialog.Builder(platform);
    ColumnLayout layout = new ColumnLayout(platform);

    final CallbackSpinner<Module> moduleSpinner = (!(artifact.owner() instanceof Module)) ? null
        : new CallbackSpinner<Module>(platform) {
      @Override
      protected void showUi(Callback<Module> callback) {
        new ModuleMenu(platform, this, artifact, callback).show();
      }
    };
    if (moduleSpinner != null) {
      moduleSpinner.setValue((Module) artifact.owner());
      layout.addView(Views.addLabel("To module", moduleSpinner));
      alert.setTitle("Rename / Move '" + artifact.name() + "'");
    } else {
      alert.setTitle("Rename '" + artifact.name() + "'");
    }

    final EditText nameEditText = new EditText(platform);
    nameEditText.setText(artifact.name());
    nameEditText.addTextChangedListener(new IdentifierValidator(nameEditText));
    layout.addView(Views.addLabel("New name", nameEditText));

    Dialogs.setViewWithPadding(alert, layout);

    alert.setNegativeButton("Cancel", null);
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            final Container newContainer = moduleSpinner == null ? artifact.owner() : moduleSpinner.getValue();
            final String newName = nameEditText.getText().toString();

            if (!Artifact.isIdentifier(newName)) {
              Dialogs.info(platform, "Invalid name", Artifact.IDENTIFIER_CONSTRAINT_MESSAGE);
            } else {
              Dialogs.confirm(platform, "Rename / Move", "Rename '" + artifact.qualifiedName() + "' to '"
                      + newContainer.qualifiedName() + "/" + nameEditText.getText().toString() + "'?", new Runnable() {
                    @Override
                    public void run() {
                      move(fragment, artifact, newContainer, newName);
                    }
                  }
              );
            }
          }
    });
    Dialogs.showWithoutKeyboard(alert);
  }


  private static void move(final ArtifactFragment fragment, final Artifact artifact, final Container newContainer, final String newName) {
    final MainActivity platform = fragment.platform();
    final ProgressDialog progressDialog = new ProgressDialog(platform);
    progressDialog.setCancelable(false);
    progressDialog.setTitle("Rename / Move");
    progressDialog.setIndeterminate(true);
    progressDialog.show();
    new Thread(new Runnable() {
        @Override
        public void run() {
          artifact.rename(newContainer, newName, new StatusListener() {
            @Override
            public void log(final String value) {
              platform.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  progressDialog.setMessage(value);
                }
              });
            }
          });
          platform.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              progressDialog.dismiss();
              platform.finishFragment(fragment);
              platform.openArtifact(artifact);
            }
          });
        }
    }).start();
  }
}
