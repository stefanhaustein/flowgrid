package org.flowgrid.swt.module;

import org.eclipse.swt.widgets.Control;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Module;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.widget.ContextMenu;

public class ModuleMenu implements ContextMenu.ItemClickListener {

    private final SwtFlowgrid platform;
    private final Control anchor;
    private final Callback<Module> callback;
    private final Artifact exclude;
    private Module currentModule;

    public ModuleMenu(SwtFlowgrid platform, Control anchor, Artifact exclude, Callback<Module> callback) {
        this.platform = platform;
        this.anchor = anchor;
        this.callback = callback;
        this.exclude = exclude;
    }

    public void show() {
        show(platform.model().rootModule);
    }

    public void show(Module module) {
        currentModule = module;
        ContextMenu menu = new ContextMenu(anchor);
        menu.setOnMenuItemClickListener(this);

        menu.add("Select " + module.name());
        for (Artifact entry: module) {
            if (entry instanceof Module && !entry.isBuiltin() && entry != exclude) {
                menu.add(entry.toString()).setHelp(entry.documentationCallable());
            }
        }
        menu.show();
    }

    @Override
    public boolean onContextMenuItemClick(ContextMenu.Item item) {
        String label = item.getTitle().toString();
        System.out.println("onMenuItemClick: " + item);

        if (label.endsWith("/")) {
            show(currentModule.module(label.substring(0, label.length() - 1)));
        } else {
            callback.run(currentModule);
        }
        return true;
    }
}
