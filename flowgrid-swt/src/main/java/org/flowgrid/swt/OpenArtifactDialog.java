package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Module;
import org.flowgrid.model.ResourceFile;
import org.flowgrid.swt.api.ImageImpl;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;
import org.flowgrid.swt.graphics.ArtifactIcon;

import java.io.IOException;
import java.util.ArrayList;

public class OpenArtifactDialog {
    final SwtFlowgrid flowgrid;
    // final ScrolledComposite scrolledComposite;
    // final Composite list;
    final Table table;
    AlertDialog alertDialog;
    Module module;
    final Combo pathCombo;
    final SelectionListener pathSelectionListener;

    OpenArtifactDialog(final SwtFlowgrid flowgrid, Module initialModule) {
        this.flowgrid = flowgrid;
        alertDialog = new AlertDialog(flowgrid.shell);

        RowLayout pathLayout = new RowLayout();
        pathLayout.spacing = 0;
        pathCombo = new Combo(alertDialog.getContentContainer(), SWT.DROP_DOWN);
        pathSelectionListener = new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int up = pathCombo.getItemCount() - 1 - pathCombo.getSelectionIndex();
                System.out.println("itemCount: " + pathCombo.getItemCount() + " sel idx: " + pathCombo.getSelectionIndex() + " up: " + up + " current: " + module);
                Module current = module;
                while (up > 0 && current.parent() != null){
                    current = current.parent();
                    up--;
                }
                setModule(current);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        };

        table = new Table(alertDialog.getContentContainer(), SWT.NONE);
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.minimumHeight = flowgrid.getMinimumListHeight();
        table.setLayoutData(tableGridData);

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

        setModule(initialModule);
    }

    void show() {
        alertDialog.show();
    }


    void setModule(Module module) {
        if (module == this.module) {
            return;
        }
        this.module = module;
        alertDialog.setTitle("Open / Create");

        pathCombo.removeSelectionListener(pathSelectionListener);
        pathCombo.removeAll();
        Module current = module;
        do {
            String name = current.name();
            pathCombo.add(name.isEmpty() ? "<root>" : name, 0);
            current = current.parent();
        } while (current != null);
        pathCombo.select(pathCombo.getItemCount() - 1);
        pathCombo.addSelectionListener(pathSelectionListener);

        table.removeAll();

        int iconSize = flowgrid.resourceManager.dpToPx(24);
        for (final Artifact artifact: module) {
            if (!artifact.isBuiltin()) {
                TableItem item = new TableItem(table, SWT.NONE);

                if (artifact instanceof ResourceFile && ((ResourceFile) artifact).kind == ResourceFile.Kind.IMAGE) {
                    try {
                        Image original = ((ImageImpl) ((ResourceFile) artifact).resource()).bitmap();
                        Image icon = new Image(flowgrid.display, iconSize, iconSize);
                        Rectangle ob = original.getBounds();

                        int max = Math.max(ob.width, ob.height);
                        int scaledWidth = iconSize * ob.width / max;
                        int scaledHeight = iconSize * ob.height / max;

                        new GC(icon).drawImage(original, 0, 0, ob.width, ob.height, (iconSize - scaledWidth) / 2, (iconSize - scaledHeight) / 2, scaledWidth, scaledHeight);

                        item.setImage(icon);
                    } catch (IOException e) {
                        item.setImage(ArtifactIcon.create(flowgrid.resourceManager, artifact).createImage(iconSize));
                    }
                } else {
                    item.setImage(ArtifactIcon.create(flowgrid.resourceManager, artifact).createImage(iconSize));
                }
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
