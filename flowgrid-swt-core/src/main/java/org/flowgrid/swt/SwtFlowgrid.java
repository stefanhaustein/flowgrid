package org.flowgrid.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Image;
import org.flowgrid.model.Member;
import org.flowgrid.model.Model;
import org.flowgrid.model.Module;
import org.flowgrid.model.Platform;
import org.flowgrid.model.Property;
import org.flowgrid.model.Sound;
import org.flowgrid.model.StructuredData;
import org.flowgrid.model.Type;
import org.flowgrid.model.TypeAndValue;
import org.flowgrid.model.Types;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.io.IOCallback;
import org.flowgrid.swt.classifier.ClassifierEditor;
import org.flowgrid.swt.operation.OperationEditor;
import org.flowgrid.swt.widget.MenuAdapter;
import org.flowgrid.swt.widget.MenuSelectionHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SwtFlowgrid implements Platform, MenuSelectionHandler {
    Model model;
    Display display;
    Shell shell;

    File flowgridRoot = new File(new File(System.getProperty("user.home")), "flowgrid");
    File storageRoot = new File(flowgridRoot, "files");
    File cacheRoot = new File(flowgridRoot, "cache");

    final MenuAdapter menuAdapter = new MenuAdapter(this);
    private HutnObject editBuffer;

    public final Colors colors;

    public SwtFlowgrid(Display display) {
        this.display = display;
        colors = new Colors(display, false);

        model = new Model(this);

        shell = new Shell(display);
        shell.setText("FlowGrid");

        shell.setMenuBar(createMenuBar());

        openArtifact(model.artifact("examples/algorithm/factorial"));

        shell.pack();
        shell.open();
    }


    @Override
    public File cacheRoot() {
        return cacheRoot;
    }

    public Menu createMenuBar() {
        Menu menuBar = new Menu(shell);
        MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        fileMenuItem.setText("File");
        Menu fileMenu = new Menu(fileMenuItem);

        menuAdapter.addItem(fileMenu, "About");
        menuAdapter.addItem(fileMenu, "Open");
        menuAdapter.addItem(fileMenu, "Tutorials");
        menuAdapter.addItem(fileMenu, "Examples");
        return menuBar;
    }

    void clear() {
        for(Control control: shell.getChildren()) {
            control.dispose();
        }
    }

    @Override
    public IOCallback<Void> defaultIoCallback(String message) {
        return null;
    }

    public HutnObject editBuffer() {
        return editBuffer;
    }

    public void editStructuredDataValue(final Member owner, final String[] path, Control anchor, final Callback<TypeAndValue> callback) {
        System.out.println("FIXME: editStructuredDataValue()");
        /*
        StructuredData data = owner.structuredData(path);                 FIXME
        String name = path[path.length - 1];
        Object value = data.get(name);
        Type type = data.type(name);
        if (value == null && Types.isAbstract(type)) {
            new TypeMenu(this, anchor, owner.module, data.type(name), TypeFilter.INSTANTIABLE, new Callback<Type>() {
                @Override
                public void run(Type type) {
                    editStructuredDataValue(owner, path, type, callback);
                }
            }).show();
        } else {
            // If we already have an object, we use the object type, if it has one.
            if (!(value instanceof List)) {
                type = model.type(value);
            }
            editStructuredDataValue(owner, path, type, callback);
        }
        */
    }

    private void editStructuredDataValue(Member owner, String[] path, final Type type, final Callback<TypeAndValue> callback) {
        System.out.println("FIXME: editStructuredDataValue()");
        /*if (Types.isPrimitive(type)) {
            DataDialog.show(this, owner, type, new Callback<Object>() {
                @Override
                public void run(Object value) {
                    callback.run(new TypeAndValue(type, value));
                }
            }, path);
        } else {
            StructuredData data = owner.structuredData(path);
            String name = path[path.length - 1];
            Object value = data.get(name);
            if (value == null) {
                if (type instanceof ArrayType) {
                    value = new ArrayList<Object>();
                } else if (type instanceof  Classifier) {
                    value = ((Classifier) type).newInstance();
                } else {
                    error("Can't create a value of type " + type, null);
                    callback.cancel();
                    return;
                }
                data.set(name, value);
            }
            // Lists don't have an implicit type
            callback.run(new TypeAndValue(type, value));

            openData(owner, true, path);
        }*/
    }


    @Override
    public Image image(InputStream is) throws IOException {
        return null;
    }

    @Override
    public void log(String message) {

    }

    void openArtifactDialog(String title, String moduleName) {
        Module module = (Module) model.artifact(moduleName);
        module.ensureLoaded();  // Move into to the iterator call?
        new ArtifactDialog(this, title, module);
    }

    @Override
    public Callback<Model> platformApiSetup() {
        return null;
    }

    @Override
    public Sound sound(InputStream is) throws IOException {
        return null;
    }

    @Override
    public File storageRoot() {
        return storageRoot;
    }

    @Override
    public String platformId() {
        return null;
    }

    @Override
    public void error(String message, Exception e) {

    }

    @Override
    public void info(String message, Exception e) {

    }

    @Override
    public void log(String message, Exception e) {

    }

    public void openArtifact(Artifact artifact) {
        if (artifact instanceof CustomOperation) {
            openOperation((CustomOperation) artifact);
        } else if (artifact instanceof Classifier) {
            openClassifier((Classifier) artifact);
        }
    }

    public void openClassifier(Classifier classifier) {
        clear();
        new ClassifierEditor(this, classifier);
    }

    private void openOperation(CustomOperation operation) {
        openOperation(operation, true);
    }

    public void openOperation(CustomOperation operation, boolean editable) {
        clear();

        if (!editable) {
            System.out.println("FIXME: SwtFlowgrid.openOperation for run mode");
        }

        new OperationEditor(this, operation);
    }

    public void openProperty(Property p) {
        System.out.println("FIXME: SwtFlowgrid.openProperty();");   // FIXME
    }

    public Shell shell() {
        return shell;
    }

    public Display display() {
        return display;
    }

    public Model model() {
        return model;
    }

    public void runOnUiThread(Runnable runnable) {
        display.asyncExec(runnable);
    }

    @Override
    public void menuItemSelected(MenuItem menuItem) {
        String label = menuItem.getText();
        if ("Examples".equals(label)) {
            openArtifactDialog("Open Example", "examples");
        } else if ("Open".equals(label)) {
            openArtifactDialog("Open", "myname");
        } else if ("Tutorials".equals(label)) {
            openArtifactDialog("Open Tutorial", "missions");
        }
    }

    public int dpToPx(float dp) {
        return Math.round(dp);
    }
}