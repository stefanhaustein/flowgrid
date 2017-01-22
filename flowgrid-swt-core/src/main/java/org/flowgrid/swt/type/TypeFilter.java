package org.flowgrid.swt.type;

import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Container;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.Types;

import java.util.ArrayList;
import java.util.Iterator;

public class TypeFilter {

    public enum Category {
        ALL, INSTANTIABLE, INTERFACE
    }

    public static class Builder {
        Container localModule;
        Type assignableTo = Type.ANY;
        Category category = Category.ALL;

        public Builder setLocalModule(Container localModule) {
            this.localModule = localModule;
            return this;
        }

        public Builder setAssignableTo(Type assignableTo) {
            this.assignableTo = assignableTo;
            return this;
        }

        public Builder setCategory(Category category) {
            this.category = category;
            return this;
        }

        public TypeFilter build() {
            return new TypeFilter(localModule, assignableTo, category);
        }

    }

    public final Container localModule;
    public final Type assignableTo;
    public final Category category;

    private TypeFilter(Container localModule, Type assignableTo, Category category) {
        this.localModule = localModule;
        this.assignableTo = assignableTo;
        this.category = category;
    }

    public boolean filter(Artifact artifact) {
        Container parent = artifact.owner();
        if (artifact instanceof Module) {
            Module module = (Module) artifact;
            if (module.name().equals("system") && module.parent().isRoot()) {
                return false;
            }
            for (Artifact child: module) {
                boolean ok = filter(child);
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
        switch (category) {
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

    Iterable<String> listNames(Module module) {
        ArrayList<String> result = new ArrayList<>();
        if (module.isRoot()) {
            if (category == TypeFilter.Category.ALL && assignableTo.isAssignableFrom(Type.ANY)) {
                result.add("Any");
            }
            if (category != TypeFilter.Category.INTERFACE) {
                if (assignableTo.isAssignableFrom(PrimitiveType.BOOLEAN)) {
                    result.add("Boolean");
                }
                if (assignableTo.isAssignableFrom(PrimitiveType.NUMBER)) {
                    result.add("Number");
                }
                if (assignableTo.isAssignableFrom(PrimitiveType.TEXT)) {
                    result.add("Text");
                }
                if (assignableTo == Type.ANY || assignableTo instanceof ArrayType) {
                    result.add("Array of...");
                }
            }
        }
        // Sub-modules
        for (Artifact entry: module) {
            if (filter(entry)) {
                result.add(entry.toString());
            }
        }
        return result;
    }

    Artifact artifactForName(Module module, String name) {
        if (name.endsWith("/")) {
            return module.module(name.substring(0, name.length() - 1));
        }
        if (module.isRoot() && module.module("system").artifact(name) instanceof PrimitiveType) {
            return module.module("system").artifact(name);
        }
        if (module.isRoot() && "Any".equals(name)) {
            return Type.ANY;
        }
        if (module.isRoot() && "Array of...".equals(name)) {
            return null;
        }
        return module.artifact(name);
    }

}
