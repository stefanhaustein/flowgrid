package org.flowgrid.swt.data;


import org.flowgrid.model.Instance;
import org.flowgrid.model.Property;

public class PropertyWidget extends DataWidget {

    public PropertyWidget(final Property property, final Instance instance) {
        super(property.type());
        setLabel(property.name());
        setValue(instance.get(property.name()));

        setOnValueChangedListener(new OnValueChangedListener() {
            @Override
            public void onValueChanged(Object newValue) {
                instance.set(property.name(), newValue);
            }
        });
    }

}
