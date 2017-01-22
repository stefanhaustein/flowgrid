package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Module;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.graphics.ArtifactIcon;

public class OpenArtifactDialog {
    final SwtFlowgrid flowgrid;
    // final ScrolledComposite scrolledComposite;
    // final Composite list;
    final Table table;
    AlertDialog alertDialog;
    Module module;
    final Composite pathComposite;

    OpenArtifactDialog(final SwtFlowgrid flowgrid, Module initialModule) {
        this.flowgrid = flowgrid;
        this.module = initialModule;
        alertDialog = new AlertDialog(flowgrid.shell);

        RowLayout pathLayout = new RowLayout();
        pathLayout.spacing = 0;
        pathComposite = new Composite(alertDialog.getContentContainer(), SWT.NONE);
        pathComposite.setLayout(pathLayout);

        table = new Table(alertDialog.getContentContainer(), SWT.NONE);
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);

        Point shellSize = flowgrid.shell.getSize();

        tableGridData.minimumHeight = shellSize.y * 2 / 3;
        table.setLayoutData(tableGridData);
        setModule(module);

        alertDialog.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Dialogs.confirm(flowgrid.shell, "Confirm Deletion", "Delete module " + OpenArtifactDialog.this.module.qualifiedName(),
                        new Runnable() {
                            @Override
                            public void run() {
                                Module parent = OpenArtifactDialog.this.module.parent();
                                OpenArtifactDialog.this.module.delete();
                                flowgrid.openArtifact(parent);
                            }
                        });
            }
        });

        alertDialog.setNeutralButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new CreateArtifactDialog(flowgrid, OpenArtifactDialog.this.module).show();
            }
        });
        alertDialog.setNegativeButton("Cancel", null);

        table.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem selected = table.getItem(table.getSelectionIndex());
                Artifact artifact = (Artifact) selected.getData();
                if (artifact instanceof Module) {
                    setModule((Module) artifact);
                } else {
                    flowgrid.openArtifact(artifact);
                    alertDialog.dismiss();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    void show() {
        alertDialog.show();
    }

    int addPath(final Module module, int topDistance) {
        if (module == null) {
            return 0;
        }
        int rootDistance = addPath(module.parent(), topDistance + 1);
        if (rootDistance < 2 || topDistance < 2) {
            Label label = new Label(pathComposite, SWT.NONE);
            label.setData(module);
            label.setText(module.parent() == null ? "root" : module.name());
            if (topDistance != 0) {
                label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseUp(MouseEvent e) {
                        setModule(module);
                    }
                });
                new Label(pathComposite, SWT.NONE).setText(" > ");
            }
        } else if (topDistance == 2) {
            new Label(pathComposite, SWT.NONE).setText("... > ");
        }
        return rootDistance + 1;
    }

    void setModule(Module module) {
        this.module = module;
        alertDialog.setTitle("Open / Create");

        for (Control part : pathComposite.getChildren()) {
            part.dispose();
        }

        addPath(module, 0);

        table.removeAll();

        /*
        if (module.parent() != null) {
            TableItem backItem = new TableItem(table, SWT.NONE);
            backItem.setText(0, module.parent().parent() == null ? "(root)" : module.parent().name());
//            ArtifactComposite artifactComposite = new ArtifactComposite(list, flowgrid.resourceManager, module.parent(), true);
  //          artifactComposite.setListener(callback);
            backItem.setData(module.parent());
        }
        */

        for (final Artifact artifact: module) {
            if (!artifact.isBuiltin()) {
                TableItem item = new TableItem(table, SWT.NONE);

                item.setImage(ArtifactIcon.create(flowgrid.resourceManager, artifact).createImage(flowgrid.resourceManager.dpToPx(24)));
                item.setText(0, artifact.name());
//                ArtifactComposite artifactComposite = new ArtifactComposite(list, flowgrid.resourceManager, artifact, false);
  //              artifactComposite.setListener(callback);
                item.setData(artifact);
            }
        }
    //    list.layout(true, true);
      //  scrolledComposite.layout(true, true);
    }
}
