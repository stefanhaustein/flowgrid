package org.flowgrid.android.type;

import org.flowgrid.android.MainActivity;
import org.flowgrid.model.Callback;
import org.flowgrid.android.Views;
import org.flowgrid.model.Container;
import org.flowgrid.model.Platform;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;

import android.content.Context;
import android.view.View;
import android.widget.Button;

public class TypeSpinner extends Button implements TypeWidget {
  private Type type = PrimitiveType.NUMBER;
  private OnTypeChangedListener listener = null;
  
  public TypeSpinner(final MainActivity platform, final Container localModule,
      final Type assignableTo, final TypeFilter filter) {
    super(platform, null, android.R.attr.spinnerStyle);
    setText(type.name());
    Views.applyEditTextStyle(this, true);
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        new TypeMenu(platform, v, localModule, assignableTo, filter, new Callback<Type>() {
          @Override
          public void run(Type type) {
            setType(type);
          }
        }).show();
      }
    });
  }
  

  public void setType(Type type) {
    if (type != this.type) {
      this.type = type;
      setText(type.name());
      if (listener != null) {
        listener.onTypeChanged(type);
      }
    }
  }
  
  public Type type() {
    return type;
  }
  
 

  public void setOnTypeChangedListener(OnTypeChangedListener onTypeChangedListener) {
    this.listener = onTypeChangedListener;
  }


  @Override
  public View view() {
    return this;
  }
}
