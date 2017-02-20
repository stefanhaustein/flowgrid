package org.flowgrid.swt;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Image;
import org.flowgrid.model.Objects;
import org.flowgrid.model.ResourceFile;
import org.flowgrid.model.Sound;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.flowgrid.model.Artifact;
import org.flowgrid.swt.api.ImageImpl;
import org.flowgrid.swt.widget.ColumnLayout;

import java.io.IOException;

public class ResourceEditor extends ArtifactEditor {

    private final ResourceFile resource;
    private final SwtFlowgrid flowgrid;

    ResourceEditor(SwtFlowgrid flowgrid, ResourceFile resource) {
        this.flowgrid = flowgrid;
        this.resource = resource;
        GridLayout shellLayout = new GridLayout(4, true);
        shellLayout.marginWidth = 0;
        shellLayout.marginHeight = 0;
        flowgrid.shell().setLayout(shellLayout);

        Composite left = new Composite(flowgrid.shell, SWT.NONE);
        GridLayout leftLayout = new GridLayout(1, false);
        leftLayout.marginWidth = 0;
        leftLayout.marginHeight = 0;
        left.setLayout(leftLayout);
        left.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        Composite main = new Composite(flowgrid.shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginWidth = 0;
        mainLayout.marginHeight = 0;
        main.setLayout(mainLayout);
        main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        main.setBackground(flowgrid.resourceManager.background);

        Label imageLabel = new Label(main, SWT.NONE);
        imageLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

        if (resource.kind == ResourceFile.Kind.IMAGE) {
            try {
                ImageImpl img = (ImageImpl) resource.resource();
                imageLabel.setImage(img.bitmap());

                new Label(left, SWT.NONE).setText("Width:");
                Text widthText = new Text(left, SWT.READ_ONLY);
                widthText.setText(String.valueOf(img.width()));
                widthText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
                new Label(left, SWT.NONE).setText("Height:");
                Text heightText = new Text(left, SWT.READ_ONLY);
                heightText.setText(String.valueOf(img.height()));
                heightText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            } catch (IOException e) {
                imageLabel.setText(e.getMessage());
            }
        }
    }

    @Override
    public Artifact getArtifact() {
        return resource;
    }

    @Override
    public SwtFlowgrid flowgrid() {
        return flowgrid();
    }

    @Override
    public void fillMenu(Menu menu) {

    }

    @Override
    public String getMenuTitle() {
        return resource.kind == ResourceFile.Kind.IMAGE ? "Image" : "Sound";
    }
}
