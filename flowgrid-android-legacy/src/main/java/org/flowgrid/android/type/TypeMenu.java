package org.flowgrid.android.type;

import org.flowgrid.android.MainActivity;
import org.flowgrid.model.Callback;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Container;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.Types;
import org.flowgrid.android.widget.ContextMenu;
import org.flowgrid.android.widget.ContextMenu.Item;

import android.view.View;

public class TypeMenu implements ContextMenu.ItemClickListener {
  private final MainActivity platform;
  private final View anchor;
  private final Callback<Type> callback;
  private final Container localModule;
  private final TypeFilter filter;
  private Module currentModule;
  private final Type assignableTo;
  
  public TypeMenu(MainActivity platform, View anchor, Container localModule, Type assignableTo, TypeFilter filter, Callback<Type> callback) {
    this.platform = platform;
    this.anchor = anchor;
    this.localModule = localModule;
    this.assignableTo = assignableTo;
    this.filter = filter;
    this.callback = callback;
  }

  
  boolean filter(Module parent, Artifact artifact) {
    if (artifact instanceof Module) {
      Module module = (Module) artifact;
      if (module.name().equals("system") && module.parent().isRoot()) {
        return false;
      }
      for (Artifact child: module) {
        boolean ok = filter(module, child);
        if (ok) {
          return true;
        }
      }
      return false;
    }   
    if (!(artifact instanceof Type)) {
      return false;
    }
    if (parent != localModule && !artifact.isPublic()) {
      return false;
    }
    Type type = (Type) artifact;
    switch (filter) {
    case ALL:
      break;
    case INSTANTIABLE:
      if (Types.isAbstract(type)) {
        return false;
      }
      break;
    case INTERFACE:
      if (!Types.isInterface(type)) {
        return false;
      }
      break;
    }
    if (!assignableTo.isAssignableFrom(type)) {
      return false;
    }
    return true;
  }

  
  public void show() {
    show(localModule.model().rootModule);
  }
  
  public void show(Module module) {
    currentModule = module;
    ContextMenu menu = new ContextMenu(platform, anchor);
    menu.setOnMenuItemClickListener(this);
    if (currentModule.isRoot()) {
      if (filter == TypeFilter.ALL && assignableTo.isAssignableFrom(Type.ANY)) {
        menu.add("Any");
      }
      if (filter != TypeFilter.INTERFACE) {
        if (assignableTo.isAssignableFrom(PrimitiveType.BOOLEAN)) {
          menu.add("Boolean");
        }
        if (assignableTo.isAssignableFrom(PrimitiveType.NUMBER)) {
          menu.add("Number");
        }
        if (assignableTo.isAssignableFrom(PrimitiveType.TEXT)) {
          menu.add("Text");
        }
        if (assignableTo == Type.ANY || assignableTo instanceof ArrayType) {
          menu.add("Array of...");
        }
      }
    }
    // Sub-modules
    for (Artifact entry: module) {
      if (filter(module, entry)) {
        menu.add(entry.toString()).setHelp(entry.documentationCallable());
      }
    }
    menu.show();
  }

  @Override
  public boolean onContextMenuItemClick(Item item) {
    String label = item.getTitle().toString();
    // TODO: Distinguish modes at top level?
    
    System.out.println("onMenuItemClick: " + item);
    if (currentModule.isRoot() && 
        currentModule.module("system").artifact(label) instanceof PrimitiveType) {
      callback.run((Type) currentModule.module("system").artifact(label));
    } else if (currentModule.isRoot() && "Any".equals(label)) {
      callback.run(Type.ANY);
    } else if (currentModule.isRoot() && "Array of...".equals(label)) {
      ArrayTypeDialog.show(platform, localModule, assignableTo, filter, callback);
    } else if (label.endsWith("/")) {
      show(currentModule.module(label.substring(0, label.length() - 1)));
    } else {
      callback.run((Type) currentModule.artifact(label));
    }
    return true;
  }
}
