package org.flowgrid.android.type;

import org.flowgrid.android.Widget;
import org.flowgrid.model.Type;

public interface TypeWidget extends Widget {
  void setType(Type type);
  Type type();
  void setOnTypeChangedListener(OnTypeChangedListener onTypeChangedListener);
  
  public interface OnTypeChangedListener {
    void onTypeChanged(Type type);
  }
}
