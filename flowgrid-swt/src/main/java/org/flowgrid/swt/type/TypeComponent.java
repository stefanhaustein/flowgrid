package org.flowgrid.swt.type;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.widget.Component;

public class TypeComponent implements Component {
    Combo combo;
    TypeFilter typeFilter;
    SwtFlowgrid flowgrid;
    TypeMenu typeMenu;
    boolean foreignSelection;
    Type selectedType;
    int selectedIndex;
    private OnTypeChangedListener onTypeChangedListener;

    public TypeComponent(Composite parent, final SwtFlowgrid flowgrid, final TypeFilter typeFilter) {
        this.flowgrid = flowgrid;
        this.typeFilter = typeFilter;
        combo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);

        for (String name: typeFilter.listNames(flowgrid.model().rootModule)) {
            combo.add(name);
        }

        typeMenu = new TypeMenu(flowgrid, combo, typeFilter, new Callback<Type>() {
            @Override
            public void run(Type value) {
                setType(value);
            }
        });


        combo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (combo.getSelectionIndex() == 0 && foreignSelection) {
                    // Ignore reset.
                    return;
                }
                String name = combo.getItem(combo.getSelectionIndex());
                Artifact artifact = typeFilter.artifactForName(flowgrid.model().rootModule, name);
                if (artifact instanceof Module) {
                    combo.select(selectedIndex);
                    typeMenu.show((Module) artifact);
                } else if (artifact == null) {
                    combo.select(selectedIndex);
                    ArrayTypeDialog.show(flowgrid, typeFilter.localModule, typeFilter.assignableTo, typeFilter.category, new Callback<Type>() {
                        @Override
                        public void run(Type value) {
                            setType(value);
                        }
                    });
                } else {
                    setType((Type) artifact);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    public void setType(Type type) {
        if (type == selectedType) {
            return;
        }
        selectedType = type;
        if (foreignSelection) {
            combo.remove(0);
        }
        foreignSelection = !(type instanceof PrimitiveType);
        if (foreignSelection) {
            combo.add(type.name(), 0);
            combo.select(0);
            selectedIndex = 0;
        } else {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItem(i).equals(type.name())) {
                    combo.select(i);
                    selectedIndex = i;
                    break;
                }
            }
        }
        if (onTypeChangedListener != null) {
            onTypeChangedListener.onTypeChanged(selectedType);
        }
    }

    @Override
    public Combo getControl() {
        return combo;
    }

    @Override
    public void dispose() {
        combo.dispose();
    }

    public void setOnTypeChangedListener(OnTypeChangedListener onTypeChangedListener) {
        this.onTypeChangedListener = onTypeChangedListener;
    }

    public Type type() {
        return selectedType;
    }

    public interface OnTypeChangedListener {
        void onTypeChanged(Type type);
    }
}
