package org.flowgrid.swt.classifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Property;
import org.flowgrid.swt.ArtifactComposite;
import org.flowgrid.swt.Strings;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.widget.MenuAdapter;
import org.flowgrid.swt.widget.MenuSelectionHandler;

public class ClassifierEditor implements MenuSelectionHandler {

    ScrolledComposite scrolledComposite;
    Composite propertyPanel;
    Composite operationPanel;
    MenuAdapter menuAdapter = new MenuAdapter(this);

    public ClassifierEditor(final SwtFlowgrid flowgrid, Classifier classifier) {

        scrolledComposite = new ScrolledComposite(flowgrid.shell(), SWT.NONE);
        Composite contentPanel = new Composite(scrolledComposite, SWT.NONE);

        flowgrid.shell().setText(classifier.name() + " - FlowGrid");

        propertyPanel = new Composite(contentPanel, SWT.NONE);
        GridLayout propertyLayout = new GridLayout(1, false);
        propertyLayout.marginHeight = 0;
        propertyLayout.marginWidth = 0;
        propertyPanel.setLayout(propertyLayout);
        propertyPanel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true));

        operationPanel = new Composite(contentPanel, SWT.NONE);
        GridLayout operationLayout = new GridLayout(1, false);
        operationLayout.marginHeight = 0;
        operationLayout.marginWidth = 0;
        operationPanel.setLayout(operationLayout);
        operationPanel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

        Callback<Artifact> propertyCallback = new Callback<Artifact>() {
            @Override
            public void run(Artifact value) {
                System.out.println("TBD: Property dialog");
            }
        };
        Callback<Artifact> operationCallback = new Callback<Artifact>() {
            @Override
            public void run(Artifact value) {
                flowgrid.openArtifact(value);
            }
        };

        for (Artifact artifact: classifier) {
            final boolean isProperty = artifact instanceof Property;
            ArtifactComposite artifactComposite = new ArtifactComposite(isProperty ? propertyPanel : operationPanel, flowgrid.colors, artifact, false);
            artifactComposite.setListener(isProperty ? propertyCallback : operationCallback);
        }

        GridLayout contentLayout = new GridLayout(2, false);
        contentPanel.setLayout(contentLayout);

        contentPanel.layout(true, true);
        scrolledComposite.setContent(contentPanel);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        flowgrid.shell().layout(true, true);

        Menu menuBar = flowgrid.createMenuBar();
        MenuItem classifierMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        classifierMenuItem.setText(classifier.isInterface() ? "Interface" : "Class");
        Menu classifierMenu = new Menu(classifierMenuItem);
        menuAdapter.addItem(classifierMenu, Strings.MENU_ITEM_ADD_PROPERTY);
        menuAdapter.addItem(classifierMenu, Strings.MENU_ITEM_ADD_METHOD);

        flowgrid.shell().setMenuBar(menuBar);
    }

    @Override
    public void menuItemSelected(MenuItem menuItem) {

    }
}
