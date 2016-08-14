package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Module;
import org.flowgrid.model.Operation;
import org.flowgrid.swt.graphics.ArtifactIcon;

public class ArtifactComposite extends Composite {

    public ArtifactComposite(Composite parent, Colors colors, Artifact artifact) {
        super(parent, SWT.NONE);
        RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
        rowLayout.fill = true;
        setLayout(rowLayout);

        ArtifactIcon.Kind kind;
        if (artifact instanceof Module) {
            kind = ArtifactIcon.Kind.MODULE;
        } else if (artifact instanceof Classifier) {
            kind = ArtifactIcon.Kind.CLASSIFIER;
        } else {
            kind = ArtifactIcon.Kind.OPERATION;
        }
        new ArtifactIcon(this, colors, kind, null);
        new Label(this, SWT.NONE).setText(artifact.name());
    }
}
