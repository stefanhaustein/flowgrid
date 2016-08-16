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
import org.flowgrid.model.Model;
import org.flowgrid.model.Module;
import org.flowgrid.model.Platform;
import org.flowgrid.model.Sound;
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
    GridLayout shellLayout;

    File flowgridRoot = new File(new File(System.getProperty("user.home")), "flowgrid");
    File storageRoot = new File(flowgridRoot, "files");
    File cacheRoot = new File(flowgridRoot, "cache");

    final MenuAdapter menuAdapter = new MenuAdapter(this);

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


    void openArtifactDialog(String title, String moduleName) {
        Module module = (Module) model.artifact(moduleName);
        module.ensureLoaded();  // Move into to the iterator call?
        new ArtifactDialog(this, title, module);
    }

    void clear() {
        for(Control control: shell.getChildren()) {
            control.dispose();
        }
    }

    @Override
    public void log(String message) {

    }

    @Override
    public Image image(InputStream is) throws IOException {
        return null;
    }

    @Override
    public Sound sound(InputStream is) throws IOException {
        return null;
    }

    @Override
    public Callback<Model> platformApiSetup() {
        return null;
    }

    @Override
    public File storageRoot() {
        return storageRoot;
    }

    @Override
    public File cacheRoot() {
        return cacheRoot;
    }

    @Override
    public IOCallback<Void> defaultIoCallback(String message) {
        return null;
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
        clear();
        new OperationEditor(this, operation);
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
}