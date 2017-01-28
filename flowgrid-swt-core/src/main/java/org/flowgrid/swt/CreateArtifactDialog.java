package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Module;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class CreateArtifactDialog {
    AlertDialog alert;
    Text nameField;
    Combo typeField;

    public CreateArtifactDialog(final SwtFlowgrid flowgrid, final Module module) {
        alert = new AlertDialog(flowgrid.shell);
        alert.setTitle("Create Artifact");

        alert.getContentContainer().setLayout(new GridLayout(2, false));

        Label moduleLabel = new Label(alert.getContentContainer(), SWT.NONE);
        moduleLabel.setText("In " + module.qualifiedName());
        moduleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));

        new Label(alert.getContentContainer(), SWT.NONE).setText("Type:");
        typeField = new Combo(alert.getContentContainer(), SWT.DROP_DOWN | SWT.READ_ONLY);
        typeField.add("Module");
        typeField.add("Operation");
        typeField.add("Class");
        typeField.add("Interface");
        typeField.select(0);

        new Label(alert.getContentContainer(), SWT.NONE).setText("Name:");
        nameField = new Text(alert.getContentContainer(), SWT.NONE);
        nameField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameField.getText();
                if (name.isEmpty()) {
                    Dialogs.info(flowgrid.shell, "Invalid Name", "The name must not be empty.");
                } else {
                    Artifact artifact;
                    switch (typeField.getSelectionIndex()) {
                        case 0:
                            artifact = new Module(flowgrid.model(), module, nameField.getText(), false);
                            break;
                        case 1:
                            artifact = new CustomOperation(module, name, true);
                            break;
                        case 2:
                            artifact = new Classifier(module, name, Classifier.Kind.CLASS);
                            break;
                        case 3:
                            artifact = new Classifier(module, name, Classifier.Kind.INTERFACE);
                            break;
                        default:
                            return;
                    }
                    module.addArtifact(artifact);
                    flowgrid.openArtifact(artifact);
                    artifact.save();
                }
            }
        });
    }


    public void show() {
        alert.show();
    }

}
