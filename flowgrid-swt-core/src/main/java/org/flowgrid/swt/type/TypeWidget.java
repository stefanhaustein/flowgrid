package org.flowgrid.swt.type;

import org.flowgrid.model.Type;
import org.flowgrid.swt.widget.Widget;

public interface TypeWidget extends Widget {
    void setType(Type type);
    Type type();
    void setOnTypeChangedListener(OnTypeChangedListener onTypeChangedListener);

    public interface OnTypeChangedListener {
        void onTypeChanged(Type type);
    }
}
