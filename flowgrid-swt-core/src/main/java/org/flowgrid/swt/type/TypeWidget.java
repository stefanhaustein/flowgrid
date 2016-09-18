package org.flowgrid.swt.type;

import org.flowgrid.model.Type;

public interface TypeWidget {
    void setType(Type type);
    Type type();
    void setOnTypeChangedListener(OnTypeChangedListener onTypeChangedListener);

    interface OnTypeChangedListener {
        void onTypeChanged(Type type);
    }
}
