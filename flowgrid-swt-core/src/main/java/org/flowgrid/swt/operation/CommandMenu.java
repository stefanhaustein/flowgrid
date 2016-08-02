package org.flowgrid.swt.operation;


import org.eclipse.swt.widgets.Control;
import org.flowgrid.model.ActionFactory;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Command;
import org.flowgrid.model.Container;
import org.flowgrid.model.Module;
import org.flowgrid.model.Operation;
import org.flowgrid.model.Property;
import org.flowgrid.model.Type;
import org.flowgrid.model.Types;
import org.flowgrid.model.api.BranchOperation;
import org.flowgrid.swt.widget.ContextMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommandMenu implements ContextMenu.ItemClickListener {
    enum Mode {TYPE, MODULE, SELECT_ACTION, SELECT_MEMBER}
    private final Control anchor;
    private final Callback<Command> callback;
    private Artifact artifact;
    private List<Type> parameterTypes;
    private ActionFactory.Action commandAction;
    private boolean implicitInstance;
    private Container localModule;
    private Mode mode = Mode.MODULE;
    private ContextMenu.Item parentItem;

    enum Match {
        MATCH, MISMATCH, HIDE
    }

    private static String normalizeName(String name) {
        int cut = name.indexOf('(');
        if (cut != -1) {
            name = name.substring(0, cut);
        }
        cut = name.indexOf('\u2b50');
        if (cut != -1) {
            name = name.substring(0, cut);
        }
        cut = name.indexOf(':');
        if (cut != -1) {
            name = name.substring(0, cut);
        }
        return name.trim();
    }

    CommandMenu(Control anchor, ContextMenu.Item parentItem, Container localModule, List<Type> parameterTypes, Callback<Command> callback) {
        this.parentItem = parentItem;
        this.anchor = anchor;
        this.callback = callback;
        this.parameterTypes = parameterTypes;
        this.localModule = localModule;
    }

    Artifact artifact() {
        return artifact;
    }

    Match filter(Container owner, Artifact artifact) {
        if (artifact instanceof Module) {
            Match best = Match.HIDE;
            Container sub = (Container) artifact;
            for (Artifact child: sub) {
                Match match = filter(sub, child);
                if (match == Match.MATCH) {
                    return match;
                }
                if (match == Match.MISMATCH) {
                    best = Match.MISMATCH;
                }
            }
            return best;
        }

        if (owner != localModule && !artifact.isPublic()) {
            return Match.HIDE;
        }

        if (parameterTypes.size() == 0) {
            return Match.MATCH;
        }
        if (artifact instanceof ActionFactory) {
            ActionFactory op = (ActionFactory) artifact;
            if (op.matches(parameterTypes)) {
                return Match.MATCH;
            };
        }
        return Match.MISMATCH;
    }

    public void showType(Type type, boolean implicitInstance) {
        this.artifact = (Artifact) type;
        this.mode = Mode.TYPE;
        if (implicitInstance && !this.implicitInstance) {
            ArrayList<Type> npt = new ArrayList<Type>(parameterTypes);
            npt.add(0, type);
            parameterTypes = npt;
        }

        this.implicitInstance = implicitInstance;
        ContextMenu menu = new ContextMenu(anchor);
        menu.setOnMenuItemClickListener(this);
        if (parentItem != null) {
            parentItem.propagateDisabledState(menu);
        }

        if (implicitInstance) {
            menu.add("this");
        } else {
            if (type instanceof Classifier && ((Classifier) type).isInstantiable()) {
                menu.add("New " + type.name());
            }
            menu.add("Filter " + type.name());
        }
        if (type instanceof Classifier) {
            Classifier classifier = (Classifier) type;
            List<Property> setProperties = classifier.properties(parameterTypes.size() > 1 ? parameterTypes.get(1) : null);
            List<Property> getProperties = classifier.properties(null);
            ArrayList<Property> hasProperties = new ArrayList<>();
            for (Property p: getProperties) {
                if (!Types.isPrimitive(p.type()) && !Types.isArray(p.type())) {
                    hasProperties.add(p);
                }
            }

            if (getProperties.size() > 0) {
                ContextMenu clearMenu = menu.addSubMenu("Clear property\u2026").getSubMenu();
                for (Property p: getProperties) {
                    clearMenu.add(p.name());
                }
                ContextMenu getMenu = menu.addSubMenu("Get property\u2026").getSubMenu();
                for (Property p: getProperties) {
                    getMenu.add(p.name());
                }
            }
            if (hasProperties.size() > 0) {
                ContextMenu hasMenu = menu.addSubMenu("Has property\u2026").getSubMenu();
                for (Property p: setProperties) {
                    hasMenu.add(p.name());
                }
            }
            if (setProperties.size() > 0) {
                ContextMenu setMenu = menu.addSubMenu("Set property\u2026").getSubMenu();
                for (Property p: setProperties) {
                    setMenu.add(p.name());
                }
            }

            List<Operation> operations = classifier.operations(parameterTypes);
            if (operations.size() > 0) {
                ContextMenu operationMenu = menu.addSubMenu("Call\u2026").getSubMenu();
                for (Operation op : operations) {
                    operationMenu.add(op.name() + "()");
                }
            }
        }
        menu.show();
    }

    public void showModule(Module module) {
        showModule(module, false);
    }

    public void showModule(Module module, boolean positive, String... filter) {
        this.artifact = module;
        ContextMenu menu = new ContextMenu(anchor);
        menu.setOnMenuItemClickListener(this);
        if (parentItem != null) {
            parentItem.propagateDisabledState(menu);
        }

        // Sub-modules
        for (Artifact entry: module) {
            Match match = filter(module, entry);
            if (match == Match.HIDE) {
                continue;
            }
            ContextMenu.Item item;
            if (entry instanceof Module) {
                boolean inFilter = false;
                for (String f: filter) {
                    if (f.equals(entry.name())) {
                        inFilter = true;
                        break;
                    }
                }
                if (inFilter != positive) {
                    continue;
                }
                item = menu.add(entry.toString());
            } else if (entry instanceof Type) {
                item = menu.add(entry.toString() + "\u2026");
            } else if (entry instanceof ActionFactory && ((ActionFactory) entry).actions().length > 1) {
                item = menu.addSubMenu(entry.toString());
                ActionFactory.Action[] actions = ((ActionFactory) entry).actions();
                for (ActionFactory.Action action : actions) {
                    item.getSubMenu().add(action.toString().charAt(0) + action.toString().substring(1).toLowerCase(Locale.US));
                }
            } else {
                item = menu.add(entry.toString());
                if (entry instanceof BranchOperation) {
                    BranchOperation cmd = (BranchOperation) entry;
                    /*                                                         FIXME
                    if (cmd.outputType(0, null) == null) {
                        item.setIcon(new ArtifactDrawable(anchor.getContext(), ArtifactDrawable.Kind.BRANCH_RIGHT, null));
                    } else if (cmd.outputType(1, null) == null) {
                        item.setIcon(new ArtifactDrawable(anchor.getContext(), ArtifactDrawable.Kind.BRANCH_LEFT_AND_RIGTH, null));
                    } else if (cmd.outputType(2, null) == null) {
                        item.setIcon(new ArtifactDrawable(anchor.getContext(), ArtifactDrawable.Kind.BRANCH_LEFT, null));
                    } else {
                        item.setIcon(new ArtifactDrawable(anchor.getContext(), ArtifactDrawable.Kind.BRANCH_ALL, null));
                    }
                    */
                }
            }
            /*
            if (entry.hasDocumentation()) {
                item.setHelp(entry.documentationCallable());      FIXME
            }
            if (match == Match.MISMATCH) {
                item.setDiscouraged(true);
            }
            */
        }
        menu.show();
    }

    @Override
    public boolean onContextMenuItemClick(ContextMenu.Item item) {
        String label = item.getTitle().toString();
        String name = normalizeName(label);
        parentItem = item;

        switch (mode) {
            case SELECT_ACTION:
            case TYPE: {
                if (item.hasSubMenu()) {
                    if (label.startsWith("Clear")) {
                        commandAction = ActionFactory.Action.CLEAR;
                    } else if (label.startsWith("Get")) {
                        commandAction = ActionFactory.Action.GET;
                    } else if (label.startsWith("Has")) {
                        commandAction = ActionFactory.Action.HAS;
                    } else if (label.startsWith("Set")) {
                        commandAction = ActionFactory.Action.SET;
                    } else if (label.startsWith("Call")) {
                        commandAction = ActionFactory.Action.INVOKE;
                    } else {
                        throw new RuntimeException("Unrecoginzed type submenu: " + label);
                    }
                    mode = Mode.SELECT_MEMBER;
                } else if (label.startsWith("New ")) {
                    commandAction = ActionFactory.Action.CREATE;
                    returnCommand();
                } else if (label.startsWith("Filter")) {
                    commandAction = ActionFactory.Action.FILTER;
                    returnCommand();
                } else if (label.startsWith("Switch")) {
                    commandAction = ActionFactory.Action.SWITCH;
                    returnCommand();
                } else if (label.startsWith("Compute")) {
                    commandAction = ActionFactory.Action.COMPUTE;
                    returnCommand();
                } else if (label.equals("this")) {
                    commandAction = ActionFactory.Action.THIS;
                    returnCommand();
                } else {
                    throw new RuntimeException("Unrecognized Action: " + label);
                }
                return true;
            }
            case MODULE: {
                if (item.hasSubMenu()) {
                    artifact = ((Module) artifact).artifact(name);
                    mode = Mode.SELECT_ACTION;
                } else if (label.endsWith("/")) {
                    showModule(((Module) artifact).module(label.substring(0, label.length() - 1)));
                } else if (label.endsWith("\u2026")) {
                    showType((Type) ((Module) artifact).artifact(label.substring(0, label.length() - 1)), false);
                } else {
                    artifact = ((Module) artifact).artifact(name);
                    returnCommand();
                }
                return true;
            }
            case SELECT_MEMBER: {
                artifact = ((Classifier) artifact).member(name);
                returnCommand();
                return true;
            }
            default:
                throw new RuntimeException("Unrecognized mode: " + mode);
        }
    }

    public void returnCommand() {
        callback.run(((ActionFactory) artifact).createCommand(commandAction, implicitInstance));
    }

}