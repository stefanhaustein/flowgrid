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
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.DisplayType;
import org.flowgrid.model.Operation;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.VirtualOperation;
import org.flowgrid.swt.ArtifactEditor;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Property;
import org.flowgrid.swt.ArtifactComposite;
import org.flowgrid.swt.Dialogs;
import org.flowgrid.swt.Strings;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.type.TypeFilter;
import org.flowgrid.swt.type.TypeMenu;
import org.flowgrid.swt.widget.ContextMenu;

import java.util.List;

public class ClassifierEditor extends ArtifactEditor {

    final SwtFlowgrid flowgrid;
    final Classifier classifier;
    final ScrolledComposite scrolledComposite;
    final Composite propertyPanel;
    final Composite operationPanel;

    public ClassifierEditor(final SwtFlowgrid flowgrid, Classifier classifier) {
        this.flowgrid = flowgrid;
        this.classifier = classifier;

        flowgrid.shell().setText(classifier.name() + " - FlowGrid");
        flowgrid.shell().setLayout(new FillLayout());

        scrolledComposite = new ScrolledComposite(flowgrid.shell(), SWT.NONE);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        Composite contentPanel = new Composite(scrolledComposite, SWT.NONE);
        GridLayout contentLayout = new GridLayout(4, true);
        contentLayout.marginHeight = 0;
        contentLayout.marginWidth = 0;
        contentPanel.setLayout(contentLayout);

        scrolledComposite.setContent(contentPanel);

        propertyPanel = new Composite(contentPanel, SWT.NONE);
        propertyPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
        GridLayout propertyLayout = new GridLayout(1, true);
      //  propertyLayout.marginHeight = 0;
      //  propertyLayout.marginWidth = 0;
        propertyPanel.setLayout(propertyLayout);

        operationPanel = new Composite(contentPanel, SWT.NONE);
        operationPanel.setBackground(flowgrid.resourceManager.background);
        operationPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        GridLayout operationLayout = new GridLayout(2, true);
        operationLayout.marginHeight = 0;
        operationLayout.marginWidth = 0;
        operationPanel.setLayout(operationLayout);

        refresh();

        contentPanel.layout(true, true);
        flowgrid.shell().layout(true, true);
    }


    void refresh() {
        for (Control control: propertyPanel.getChildren()) {
            control.dispose();
        }
        for (Control control: operationPanel.getChildren()) {
            control.dispose();
        }

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
        Callback<Artifact> openCallback = new Callback<Artifact>() {
            @Override
            public void run(Artifact value) {
                flowgrid.openArtifact(value);
            }
        };
        for (final Operation operation: classifier.operations(null)) {
            ArtifactComposite artifactComposite = new ArtifactComposite(
                    operationPanel, flowgrid.resourceManager, operation);
            artifactComposite.setListener(openCallback);
            artifactComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        }

    }

    @Override
    public boolean onContextMenuItemClick(ContextMenu.Item menuItem) {
        final String title = menuItem.getTitle();
        if (Strings.MENU_ITEM_ADD_PROPERTY.equals(title)) {
            Dialogs.promptIdentifier(flowgrid.shell(), "Add property", "Name", "", new Callback<String>() {
                @Override
                public void run(String propertyName) {
                    Property property = new Property(classifier, propertyName, PrimitiveType.NUMBER, 0);
                    classifier.addProperty(property);
                    classifier.save();
                    refresh();
                    flowgrid.openProperty(property);
                }
            });
            return true;
        }
        if (Strings.MENU_ITEM_ADD_METHOD.equals(title)) {
            Dialogs.promptIdentifier(flowgrid.shell(), title, "Name", "", new Callback<String>() {
                @Override
                public void run(String value) {
                    Operation operation = classifier.isInterface()
                            ? new VirtualOperation(value, classifier) :
                            new CustomOperation(classifier, value, true);
                    classifier.addOperation(operation);
                    classifier.save();
                    refresh();
                    flowgrid().openOperation(operation);
                }
            });
            return true;
        }
        if ("Implement Interface...".equals(title)) {
            new TypeMenu(flowgrid(), operationPanel, classifier.module(), Type.ANY, TypeFilter.Category.INTERFACE, new Callback<Type>() {
                @Override
                public void run(Type value) {
                    Classifier implement = (Classifier) value;
                    List<String> problems = classifier.implementInterface(implement);
                    if (problems.size() > 0) {
                        StringBuilder sb = new StringBuilder("The following members have incompatible signatures:");

                        for (String problem: problems) {
                            sb.append("\n\n" + problem + "\n");
                            sb.append("  Expected: ").append(implement.artifact(problem).toString(DisplayType.DETAILED)).append("\n");
                            sb.append("  Found: ").append(classifier.artifact(problem).toString(DisplayType.DETAILED)).append("\n");
                        }

                        Dialogs.info(flowgrid.shell(), "Cannot implement " + ((Classifier) value).name(),
                                sb.toString());
                    } else {
                        classifier.save();
                        refresh();
                    }
                }
            }).show();
            return true;
        }
        return super.onContextMenuItemClick(menuItem);
    }

    @Override
    public String getMenuTitle() {
        return classifier.isInterface() ? "Interface" : "Class";
    }


    @Override
    public void fillMenu(Menu menu) {
        ContextMenu menuAdapter = new ContextMenu(menu);
        menuAdapter.setOnMenuItemClickListener(this);

        menuAdapter.addCheckable(Strings.MENU_ITEM_PUBLIC).setChecked(classifier.isPublic());

        menuAdapter.add(Strings.MENU_ITEM_ADD_PROPERTY);
        menuAdapter.add(Strings.MENU_ITEM_ADD_METHOD);

        menuAdapter.add(Strings.MENU_ITEM_RENAME_MOVE);
        menuAdapter.add(Strings.MENU_ITEM_DELETE);
    }

    @Override
    public Classifier getArtifact() {
        return classifier;
    }

    @Override
    public SwtFlowgrid flowgrid() {
        return flowgrid;
    }
}
