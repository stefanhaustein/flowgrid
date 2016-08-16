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
import org.flowgrid.swt.widget.Dialogs;

public class ArtifactDialog {
    final SwtFlowgrid flowgrid;
    final ScrolledComposite scrolledComposite;
    final Composite list;
    final Shell shell;

    ArtifactDialog(SwtFlowgrid flowgrid, String title, Module module) {
        this.flowgrid = flowgrid;
        shell = new Shell(flowgrid.shell);
        shell.setText(title);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        shell.setLayout(gridLayout);

        scrolledComposite = new ScrolledComposite(shell, 0);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        list = new Composite(scrolledComposite, 0);
        list.setLayout(new RowLayout(SWT.VERTICAL));

        setModule(module);
        scrolledComposite.setContent(list);

        shell.pack();
        Dialogs.center(shell, flowgrid.shell);
        shell.open();
    }

    void setModule(Module module) {
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
                    shell.dispose();
                }
            }
        };

        if (module.parent() != null && module.parent().parent() != null) {
            ArtifactComposite artifactComposite = new ArtifactComposite(list, flowgrid.colors, module.parent(), true);
            artifactComposite.setListener(callback);
        }

        for (final Artifact artifact: module) {
            ArtifactComposite artifactComposite = new ArtifactComposite(list, flowgrid.colors, artifact, false);
            artifactComposite.setListener(callback);
        }
        list.layout(true, true);
    }
}
