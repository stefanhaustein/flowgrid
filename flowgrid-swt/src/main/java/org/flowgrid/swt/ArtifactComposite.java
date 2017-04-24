package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.swt.graphics.ArtifactIconControl;

public class ArtifactComposite extends Composite {
    Artifact artifact;
    ArtifactIconControl icon;
    Label label;

    public ArtifactComposite(Composite parent, ResourceManager resourceManager, Artifact artifact) {
        super(parent, SWT.NONE);
        this.artifact = artifact;
        RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
        rowLayout.fill = true;
        setLayout(rowLayout);
        setBackground(resourceManager.background);


        icon = new ArtifactIconControl(this, resourceManager, artifact);
        label = new Label(this, SWT.NONE);
        label.setText(artifact.name().isEmpty() ? "(home)" : artifact.name());
    }

    public void setListener(final Callback<Artifact> listener) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                listener.run(artifact);
            }
        };
        if (icon != null) {
            icon.addMouseListener(mouseAdapter);
        }
        label.addMouseListener(mouseAdapter);
        addMouseListener(mouseAdapter);
    }

}
