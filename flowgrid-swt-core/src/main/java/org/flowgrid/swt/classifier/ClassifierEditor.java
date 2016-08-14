package org.flowgrid.swt.classifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Property;
import org.flowgrid.swt.ArtifactComposite;
import org.flowgrid.swt.SwtFlowgrid;

public class ClassifierEditor extends ScrolledComposite {

    Composite propertyPanel;
    Composite operationPanel;

    public ClassifierEditor(SwtFlowgrid flowgrid, Classifier classifier) {
        super(flowgrid.shell(), SWT.NONE);

        Composite contentPanel = new Composite(this, SWT.NONE);

        propertyPanel = new Composite(contentPanel, SWT.NONE);
        GridLayout propertyLayout = new GridLayout(1, false);
        propertyLayout.marginHeight = 0;
        propertyLayout.marginWidth = 0;
        propertyPanel.setLayout(propertyLayout);

        operationPanel = new Composite(contentPanel, SWT.NONE);
        GridLayout operationLayout = new GridLayout(1, false);
        operationLayout.marginHeight = 0;
        operationLayout.marginWidth = 0;
        operationPanel.setLayout(operationLayout);

        for (Artifact artifact: classifier) {
            ArtifactComposite artifactComposite = new ArtifactComposite(artifact instanceof Property ? propertyPanel : operationPanel, flowgrid.colors, artifact);
        }

        GridLayout contentLayout = new GridLayout(2, false);
        contentPanel.setLayout(contentLayout);

        contentPanel.layout(true, true);
        setContent(contentPanel);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        flowgrid.shell().layout(true, true);
    }
}
