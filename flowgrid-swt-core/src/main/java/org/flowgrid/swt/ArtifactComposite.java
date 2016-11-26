package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Module;
import org.flowgrid.model.Operation;
import org.flowgrid.model.Property;
import org.flowgrid.swt.graphics.ArtifactIcon;

public class ArtifactComposite extends Composite {
    Artifact artifact;
    ArtifactIcon icon;
    Label label;

    public ArtifactComposite(Composite parent, Colors colors, Artifact artifact, boolean up) {
        super(parent, SWT.NONE);
        this.artifact = artifact;
        RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
        rowLayout.fill = true;
        setLayout(rowLayout);

        ArtifactIcon.Kind kind;
        String text = null;
        if (artifact instanceof Module) {
            if (up) {
                text = "<";
                kind = ArtifactIcon.Kind.NO_ICON;
            } else {
                kind = ArtifactIcon.Kind.MODULE;
            }
        } else if (artifact instanceof Classifier) {
            kind = ArtifactIcon.Kind.CLASSIFIER;
            text = ((Classifier) artifact).isInterface() ? "I" : "C";
        } else if (artifact instanceof Property){
            kind = ArtifactIcon.Kind.PROPERTY;
            text = "p";
        } else if (artifact instanceof Operation) {
            kind = ArtifactIcon.Kind.OPERATION; // ((Operation) artifact).
            if (artifact instanceof CustomOperation) {
                text = ((CustomOperation) artifact).classifier != null ? "m" : "op";
                if (((CustomOperation) artifact).asyncInput()) {
                    kind = ArtifactIcon.Kind.CONTINUOUS_OPERATION;
                }
            }
        } else {
            kind = null;
        }
        if (kind != null) {
            icon = new ArtifactIcon(this, colors, kind, text);
        }
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
