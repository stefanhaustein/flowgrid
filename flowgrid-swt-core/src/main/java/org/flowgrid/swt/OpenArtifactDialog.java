package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Module;
import org.flowgrid.swt.dialog.AlertDialog;
import org.flowgrid.swt.dialog.DialogInterface;

public class OpenArtifactDialog {
    final SwtFlowgrid flowgrid;
    final ScrolledComposite scrolledComposite;
    final Composite list;
    AlertDialog alertDialog;
    Module module;

    OpenArtifactDialog(final SwtFlowgrid flowgrid, Module initialModule) {
        this.flowgrid = flowgrid;
        this.module = initialModule;
        alertDialog = new AlertDialog(flowgrid.shell);

        scrolledComposite = new ScrolledComposite(alertDialog.getContentContainer(), 0);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        list = new Composite(scrolledComposite, 0);
        list.setLayout(new RowLayout(SWT.VERTICAL));

        setModule(module);
        scrolledComposite.setContent(list);

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

    }

    void show() {
        alertDialog.show();
    }

    void setModule(Module module) {
        this.module = module;
        alertDialog.setTitle(module == flowgrid.model.rootModule ? "Open" : ("Open - " + module.name()));

        for(Control control: list.getChildren()) {
            control.dispose();
        }
        Callback callback = new Callback<Artifact>() {
            @Override
            public void run(Artifact artifact) {
                if (artifact instanceof Module) {
                    setModule((Module) artifact);
                } else {
                    flowgrid.openArtifact(artifact);
                    alertDialog.dismiss();
                }
            }
        };

        if (module.parent() != null) {
            ArtifactComposite artifactComposite = new ArtifactComposite(list, flowgrid.colors, module.parent(), true);
            artifactComposite.setListener(callback);
        }

        for (final Artifact artifact: module) {
            if (!artifact.isBuiltin()) {
                ArtifactComposite artifactComposite = new ArtifactComposite(list, flowgrid.colors, artifact, false);
                artifactComposite.setListener(callback);
            }
        }
        list.layout(true, true);
        scrolledComposite.layout(true, true);
    }
}
