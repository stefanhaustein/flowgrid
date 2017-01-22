package org.flowgrid.swt.type;

import org.eclipse.swt.widgets.Control;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Container;
import org.flowgrid.model.Module;
import org.flowgrid.model.Type;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.widget.ContextMenu;

public class TypeMenu implements ContextMenu.ItemClickListener {
    private final SwtFlowgrid platform;
    private final Control anchor;
    private final Callback<Type> callback;
    private final TypeFilter typeFilter;
    private Module currentModule;

    public TypeMenu(SwtFlowgrid platform, Control anchor, TypeFilter typeFilter, Callback<Type> callback) {
        this.platform = platform;
        this.anchor = anchor;
        this.typeFilter = typeFilter;
        this.callback = callback;
    }

    public TypeMenu(SwtFlowgrid platform, Control anchor, Container localModule, Type assignableTo, TypeFilter.Category category, Callback<Type> callback) {
        this(platform, anchor, new TypeFilter.Builder().setLocalModule(localModule).setAssignableTo(assignableTo).setCategory(category).build(), callback);
    }

    public void show() {
        show(typeFilter.localModule.model().rootModule);
    }

    public void show(Module module) {
        currentModule = module;
        ContextMenu menu = new ContextMenu(anchor);
        menu.setOnMenuItemClickListener(this);
        for (String label : typeFilter.listNames(currentModule)) {
            menu.add(label);
        }
        menu.show();
    }

    @Override
    public boolean onContextMenuItemClick(ContextMenu.Item item) {
        String label = item.getTitle().toString();
        // TODO: Distinguish modes at top level?

        Artifact artifact = typeFilter.artifactForName(currentModule, label);

        if (artifact instanceof Module) {
            show((Module) artifact);
        } else if (artifact == null) {
            ArrayTypeDialog.show(platform, typeFilter.localModule, typeFilter.assignableTo, typeFilter.category, callback);
        } else {
            callback.run((Type) artifact);
        }

        return true;
    }
}