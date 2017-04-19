package org.flowgrid.android.module;

import org.flowgrid.android.MainActivity;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Module;
import org.flowgrid.android.widget.ContextMenu;
import org.flowgrid.android.widget.ContextMenu.Item;

import android.view.View;

public class ModuleMenu implements ContextMenu.ItemClickListener {
  private final MainActivity platform;
  private final View anchor;
  private final Callback<Module> callback;
  private final Artifact exclude;
  private Module currentModule;

  public ModuleMenu(MainActivity platform, View anchor, Artifact exclude, Callback<Module> callback) {
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
    ContextMenu menu = new ContextMenu(platform, anchor);
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
  public boolean onContextMenuItemClick(Item item) {
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