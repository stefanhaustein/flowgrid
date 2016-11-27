package org.flowgrid.swt.classifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.flowgrid.model.Operation;
import org.flowgrid.model.Type;
import org.flowgrid.swt.ArtifactEditor;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Property;
import org.flowgrid.swt.ArtifactComposite;
import org.flowgrid.swt.Strings;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.widget.MenuAdapter;
import org.flowgrid.swt.widget.MenuSelectionHandler;

import java.util.Collections;
import java.util.Iterator;

public class ClassifierEditor implements ArtifactEditor, MenuSelectionHandler {

    Classifier classifier;
    ScrolledComposite scrolledComposite;
    Composite propertyPanel;
    Composite operationPanel;
    MenuAdapter menuAdapter = new MenuAdapter(this);

    public ClassifierEditor(final SwtFlowgrid flowgrid, Classifier classifier) {
        this.classifier = classifier;

        flowgrid.shell().setText(classifier.name() + " - FlowGrid");
        flowgrid.shell().setLayout(new FillLayout());

        scrolledComposite = new ScrolledComposite(flowgrid.shell(), SWT.NONE);
        scrolledComposite.setExpandHorizontal(true);

        Composite contentPanel = new Composite(scrolledComposite, SWT.NONE);
        GridLayout contentLayout = new GridLayout(4, true);
        contentLayout.marginHeight = 0;
        contentLayout.marginWidth = 0;
        contentPanel.setLayout(contentLayout);

        scrolledComposite.setContent(contentPanel);

        Composite propertyPanel = new Composite(contentPanel, SWT.NONE);
        propertyPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
        GridLayout propertyLayout = new GridLayout(1, true);
      //  propertyLayout.marginHeight = 0;
      //  propertyLayout.marginWidth = 0;
        propertyPanel.setLayout(propertyLayout);

        for (final Property property: classifier.properties(null)) {
            Label label = new Label(propertyPanel, SWT.NONE);
            label.setText(property.name());
            label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    flowgrid.openArtifact(property);
                }
            });
        }

        Composite operationPanel = new Composite(contentPanel, SWT.NONE);
        operationPanel.setBackground(flowgrid.colors.white);
        operationPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
        GridLayout operationLayout = new GridLayout(2, true);
        operationLayout.marginHeight = 0;
        operationLayout.marginWidth = 0;
        operationPanel.setLayout(operationLayout);

        Callback<Artifact> openCallback = new Callback<Artifact>() {
            @Override
            public void run(Artifact value) {
                flowgrid.openArtifact(value);
            }
        };

        for (final Operation operation: classifier.operations(null)) {
            ArtifactComposite artifactComposite = new ArtifactComposite(
                    operationPanel, flowgrid.colors, operation, false);
            artifactComposite.setBackground(flowgrid.colors.white);
            artifactComposite.setListener(openCallback);
            artifactComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        }

        contentPanel.layout(true, true);
        flowgrid.shell().layout(true, true);
    }

    @Override
    public void menuItemSelected(MenuItem menuItem) {

    }

    @Override
    public void addArtifactMenu(Menu menuBar) {
        MenuItem classifierMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        classifierMenuItem.setText(classifier.isInterface() ? "Interface" : "Class");
        Menu classifierMenu = new Menu(classifierMenuItem);
        menuAdapter.addItem(classifierMenu, Strings.MENU_ITEM_ADD_PROPERTY);
        menuAdapter.addItem(classifierMenu, Strings.MENU_ITEM_ADD_METHOD);

    }

    @Override
    public Classifier getArtifact() {
        return classifier;
    }
}
