package org.flowgrid.swt;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Container;
import org.flowgrid.model.Module;
import org.flowgrid.model.io.StatusListener;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.dialog.ProgressDialog;
import org.flowgrid.swt.module.ModuleMenu;

public class MoveDialog {


    public static void show(final ArtifactEditor fragment, final Artifact artifact) {
        final SwtFlowgrid platform = fragment.flowgrid();
        AlertDialog alert = new AlertDialog(platform.shell);

        final boolean canMove = artifact.owner() instanceof Module;
        if (canMove) {
            new Label(alert.getContentContainer(), SWT.NONE).setText("Move to module");
        }
        final Button moduleButton = canMove ? new Button(alert.getContentContainer(), SWT.NONE) : null;
        final Container[] selectedContainer = new Container[] {artifact.owner()};
        if (canMove) {
            moduleButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    new ModuleMenu(platform, moduleButton, artifact, new Callback<Module>() {
                        @Override
                        public void run(Module value) {
                            selectedContainer[0] = value;
                            moduleButton.setText(value.name());
                        }
                    }).show();
                }
            });
            moduleButton.setText(artifact.owner().name());
            alert.setTitle("Rename / Move '" + artifact.name() + "'");
        } else {
            alert.setTitle("Rename '" + artifact.name() + "'");
        }

        Label nameLabel = new Label(alert.getContentContainer(), SWT.NONE);
        nameLabel.setText("New name");

        final Text nameEditText = new Text(alert.getContentContainer(), SWT.NONE);
        nameEditText.setText(artifact.name());
        nameEditText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String newName = nameEditText.getText().toString();

                if (!Artifact.isIdentifier(newName)) {
                    Dialogs.info(platform.shell, "Invalid name", Artifact.IDENTIFIER_CONSTRAINT_MESSAGE);
                } else {
                    Dialogs.confirm(platform.shell, "Rename / Move", "Rename '" + artifact.qualifiedName() + "' to '"
                                    + selectedContainer[0].qualifiedName() + "/" + nameEditText.getText().toString() + "'?", new Runnable() {
                                @Override
                                public void run() {
                                    move(fragment, artifact, selectedContainer[0], newName);
                                }
                            }
                    );
                }
            }
        });
        alert.show();
    }


    private static void move(final ArtifactEditor fragment, final Artifact artifact, final Container newContainer, final String newName) {
        final SwtFlowgrid platform = fragment.flowgrid();
        final ProgressDialog progressDialog = new ProgressDialog(platform.shell);
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
                        platform.openArtifact(artifact);
                    }
                });
            }
        }).start();
    }
}
