package org.flowgrid.swt;

import org.eclipse.swt.SWT;
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
import org.flowgrid.model.Type;
import org.flowgrid.model.TypeAndValue;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.io.IOCallback;
import org.flowgrid.swt.api.ImageImpl;
import org.flowgrid.swt.api.SwtApiSetup;
import org.flowgrid.swt.classifier.ClassifierEditor;
import org.flowgrid.swt.classifier.PropertyDialog;
import org.flowgrid.swt.operation.OperationEditor;
import org.flowgrid.swt.widget.MenuAdapter;
import org.flowgrid.swt.widget.MenuSelectionHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

public class SwtFlowgrid implements Platform, MenuSelectionHandler {
    Model model;
    Display display;
    Shell shell;

    File flowgridRoot;
    File storageRoot;
    File cacheRoot;

    final MenuAdapter menuAdapter = new MenuAdapter(this);
    private HutnObject editBuffer;
    private Settings settings = new Settings();
    private LinkedHashMap<String, String> documentation = new LinkedHashMap<>();
    private ArtifactEditor currentEditor;

    public final Colors colors;

    public SwtFlowgrid(Display display, File flowgridRootDir) {
        this.display = display;

        flowgridRoot = flowgridRootDir;
        storageRoot = new File(flowgridRoot, "files");
        cacheRoot = new File(flowgridRoot, "cache");

        colors = new Colors(display, false);
        shell = new Shell(display);
        shell.setText("FlowGrid");
    }

    public void start() {
        Settings.BootCommand bootCommand = settings.bootCommand();

        if (!storageRoot().exists() || storageRoot().list().length == 0) {
            storageRoot().mkdirs();
            cacheRoot().mkdirs();
            bootCommand = Settings.BootCommand.INITIALIZE_FILESYSTEM;
            settings().setBootCommand(bootCommand, null);
        }

        if (bootCommand != null && bootCommand != Settings.BootCommand.NONE) {
            new Installer(this).start();
            return;
        }

        model = new Model(this);
        loadDocumentation();

        openArtifact(model.artifact(settings.getLastUsed()));

        updateMenu();

        shell.pack();
        shell.open();
    }


    @Override
    public File cacheRoot() {
        return cacheRoot;
    }

    public void updateMenu() {
        Menu menuBar = new Menu(shell);
        MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        fileMenuItem.setText("File");
        Menu fileMenu = new Menu(fileMenuItem);

        menuAdapter.addItem(fileMenu, "About");
        menuAdapter.addItem(fileMenu, "Open");

        if (currentEditor != null) {
            currentEditor.addArtifactMenu(menuBar);
        }

        shell.setMenuBar(menuBar);
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
        return new ImageImpl(new org.eclipse.swt.graphics.Image(display, is));
    }

    private void loadDocumentation() {
        try {
            loadDocumentation("documentation.md");
            loadDocumentation("ui.md");
            loadDocumentation("api.md");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void loadDocumentation(String name) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/documentation/" + name), "UTF-8"));
        String title = null;
        StringBuilder body = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null || line.startsWith("#")) {
                if (title != null) {
                    int pos = 0;
                    while (pos < body.length() - 1) {
                        if (body.charAt(pos) == '\n') {
                            if (body.charAt(pos + 1) == '\n') {
                                pos += 2;
                            } else {
                                body.setCharAt(pos, ' ');
                            }
                        } else {
                            pos++;
                        }
                    }

                    String text = body.toString().trim();
                    documentation.put(title, text);
                    Artifact artifact = model.artifact(title);
                    if (artifact != null) {
                        artifact.setDocumentation(text);
                    }
                }
                if (line == null) {
                    break;
                }
                int cut = 1;
                while (line.charAt(cut) == '#') {
                    cut++;
                }
                title = line.substring(cut).trim();
                body.setLength(0);
            } else {
                body.append(line);
                body.append('\n');
            }
        }
    }

    @Override
    public void log(String message) {
        System.out.println("FIXME: log: " + message);
    }

    void openArtifactDialog(String title, String moduleName) {
        Module module = (Module) model.artifact(moduleName);
        module.ensureLoaded();  // Move into to the iterator call?
        new OpenArtifactDialog(this, module);
    }

    @Override
    public Callback<Model> platformApiSetup() {
        return new SwtApiSetup(this);
    }

    public Settings settings() {
        return settings;
    }

    @Override
    public Sound sound(InputStream is) throws IOException {
        System.out.println("FIXME: SwtFlowgrid.sound() returning null.");  // FIXME.
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
        System.err.println("error: " + message + " -- " + e);
    }

    @Override
    public void info(String message, Exception e) {
        System.err.println("info: " + message + " -- " + e);
    }

    @Override
    public void log(String message, Exception e) {
        System.err.println("log: " + message + " -- " + e);
    }

    public void openArtifact(Artifact artifact) {
        if (artifact instanceof CustomOperation) {
            openOperation((CustomOperation) artifact);
        } else if (artifact instanceof Classifier) {
            openClassifier((Classifier) artifact);
        } else if (artifact instanceof Property) {
            openProperty((Property) artifact);
        }
    }

    public void openClassifier(Classifier classifier) {
        clear();
        currentEditor = new ClassifierEditor(this, classifier);
    }

    private void openOperation(CustomOperation operation) {
        openOperation(operation, true);
    }

    public void openOperation(CustomOperation operation, boolean editable) {
        clear();

        if (!editable) {
            System.out.println("FIXME: SwtFlowgrid.openOperation for run mode");
        }

        settings.setLastUsed(operation);

        currentEditor = new OperationEditor(this, operation);
    }

    public void openProperty(Property p) {
        PropertyDialog.show(this, p);
    }

    public Shell shell() {
        return shell;
    }

    public void setEditBuffer(HutnObject json) {
        this.editBuffer = json;
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
        } else if ("About".equals(label)) {
            AboutDialog.show(this);
        }
    }

    public int dpToPx(float dp) {
        return Math.round(dp);
    }

    public String documentation(String s) {
        return documentation.get(s);
    }

    public void reboot(Settings.BootCommand none, Object o) {
        System.out.println("FIXME: SwtFlowgrid.reboot()");  // FIXME
    }
}