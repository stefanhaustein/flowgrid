package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class OperationHelpDialog {
    public static void show(final OperationEditor fragment) {
        SwtFlowgrid flowgrid = fragment.flowgrid();
        AlertDialog alert = new AlertDialog(flowgrid.shell());

        String editorHelpText = flowgrid.documentation("Operation Editor");


        if (fragment.tutorialMode &&
                (fragment.landscapeMode || !fragment.operation.hasDocumentation())) {
            alert.setMessage(editorHelpText);
            alert.setTitle("Editor Help");
            alert.setPositiveButton("Ok", null);
        } else {
            TabFolder tabFolder = new TabFolder(alert.getContentContainer(), SWT.TOP);
            String documentationText = fragment.operation.documentation();

            if (fragment.operation.isTutorial()) {
                int index = 1;
                for(String paragraph : documentationText.split("\n\n")) {
                    TabItem hintTabItem = new TabItem(tabFolder, 0);
                    Label label = new Label(tabFolder, 0);
                    label.setText(paragraph);
                    hintTabItem.setText("Hint " + (index++));
                    hintTabItem.setControl(label);
                    //  textView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
                }
            }

            TabItem editorHelpItem = new TabItem(tabFolder, 0);
            editorHelpItem.setText("Editor Help");

            final Text documentation = new Text(tabFolder, 0);
            if (!fragment.tutorialMode) {
                TabItem operationDocumentationItem = new TabItem(tabFolder, 0);
                operationDocumentationItem.setText("Operation Documentation");
                operationDocumentationItem.setControl(documentation);
                if (documentationText != null && !documentationText.trim().isEmpty()) {
                    tabFolder.setSelection(1);
                }
                alert.setNegativeButton("Cancel", null);
            }

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fragment.operationCanvas.beforeChange();
                    fragment.operation.setDocumentation(documentation.getText().toString());
                    // fragment.tutorialHelpView.setText(fragment.operation.documentation());
                    fragment.operationCanvas.afterChange();
                }
            });
        }


        alert.show();
    }
}
