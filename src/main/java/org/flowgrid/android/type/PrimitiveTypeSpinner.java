package org.flowgrid.android.type;

import org.flowgrid.android.Views;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class PrimitiveTypeSpinner extends Spinner implements TypeWidget {

  OnTypeChangedListener listener;
  
  public PrimitiveTypeSpinner(Context context) {
    super(context);
    Views.setSpinnerOptions(this, PrimitiveType.ALL);
    this.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view,
          int position, long id) {
        setType(PrimitiveType.ALL[position]);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  public PrimitiveType type() {
    return (PrimitiveType) getSelectedItem();
  }

  @Override
  public void setType(Type type) {
    if (type == type()) {
      return;
    }
    for (int i = 0; i < PrimitiveType.ALL.length; i++) {
      if (PrimitiveType.ALL[i].equals(type)) {
        setSelection(i);
        break;
      }
    }
    if (listener != null) {
      listener.onTypeChanged(type);
    }
  }

  @Override
  public void setOnTypeChangedListener(OnTypeChangedListener listener) {
    this.listener = listener;
  }

  @Override
  public View view() {
    return this;
  }
}
