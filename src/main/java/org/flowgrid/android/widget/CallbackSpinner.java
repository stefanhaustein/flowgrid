package org.flowgrid.android.widget;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import org.flowgrid.android.Views;
import org.flowgrid.model.Callback;

public abstract class CallbackSpinner<T> extends Button {
  private T value;
  private Callback<T> valueChangeListener;

  public CallbackSpinner(Context context) {
    super(context, null, android.R.attr.spinnerStyle);
    Views.applyEditTextStyle(this, true);
        setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            showUi(new Callback<T>() {
              @Override
              public void run(T value) {
                setValue(value);
              }
            });
          }
        });
  }

  protected abstract void showUi(Callback<T> callback);

  public T getValue() {
    return value;
  }

  public void setValueChangeListener(Callback<T> callback) {
    this.valueChangeListener = callback;
  }


  public void setValue(T value) {
    if (value != this.value){
      this.value = value;
      setText(value.toString());
      if (valueChangeListener != null) {
        valueChangeListener.run(value);
      }
    }
  }
}
