package org.flowgrid.swt.data;


import org.eclipse.swt.widgets.Composite;
import org.flowgrid.model.Instance;
import org.flowgrid.model.Property;
import org.flowgrid.swt.SwtFlowgrid;

public class PropertyComponent extends DataComponent {

    public PropertyComponent(Composite parent, final SwtFlowgrid flowgrid, final Property property, final Instance instance) {
        super(parent, flowgrid, property.type(), property.name(), null /* widget */, property.module, false);
        setValue(instance.get(property.name()));
        setOnValueChangedListener(new OnValueChangedListener() {
            @Override
            public void onValueChanged(Object newValue) {
                instance.set(property.name(), newValue);
            }
        });
    }

}
