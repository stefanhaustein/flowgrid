package org.flowgrid.android.data;

import java.util.Timer;

import org.flowgrid.R;
import org.flowgrid.model.Callback;
import org.flowgrid.android.MainActivity;
import org.flowgrid.android.UiTimerTask;
import org.flowgrid.android.Views;
import org.flowgrid.android.Widget;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Member;
import org.flowgrid.model.Model;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.StructuredData;
import org.flowgrid.model.Type;
import org.flowgrid.model.TypeAndValue;
import org.flowgrid.model.Types;

import android.graphics.Point;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public final class DataWidget implements Widget {
  public interface OnValueChangedListener {
    void onValueChanged(Object newValue);
  }

  // Ctor
  final MainActivity platform;
  final Member owner;
  final boolean editable;
  final String[] path;
  final StructuredData parent;
  private final Type type;
  private final String name;

  // We keep a copy here because in some cases (e.g. adding literals), the value holder
  // needs to be created explicitly before it's possible to read the value back
  private Object value;

  // Built up
  final View view;
  private ProgressBar progressBar;
  private ImageView clearButton;
  private OnValueChangedListener onValueChangedListener;
  private TextView textView;

  private Timer timer;
  private UiTimerTask sendTask;



  public DataWidget(final MainActivity platform, Member owner, String... path) {
    this(platform, owner, null, "", true, path);
  }

  public DataWidget(final MainActivity platform, final Member owner, Type forceType, String widgetType, final boolean editable, final String... path) {
    this.platform = platform;

    parent = owner.structuredData(path);
    name = path[path.length - 1];

    //Data data = owner.data(path);
    type = forceType != null ? forceType : parent.type(name);
    this.editable = editable && Types.hasInstantiableImplementation(type);
    this.owner = owner;
    this.path = path;

    if (type == PrimitiveType.BOOLEAN && editable) {
      CheckBox checkBox = new CheckBox(platform);
      checkBox.setText(name);
//      Switch switchButton = new Switch(platform);
 //     view = switchButton;
  //    switchButton.setText(name);
      checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          inputChangedTo(isChecked, false);
        }
      });
      view = checkBox;
    } else if (Types.isAbstract(type) || type instanceof Classifier || type instanceof ArrayType) {
      View wrap = textView = new TextView(platform);
      if (this.editable) {
        textView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            platform.editStructuredDataValue(owner, path, view, new Callback<TypeAndValue>() {
              @Override
              public void run(TypeAndValue variant) {
                inputChangedTo(variant.value, false);
                updateView();
              }
            });
          }
        });

        clearButton = new ImageView(platform);
        clearButton.setImageResource(R.drawable.ic_clear_white_24dp);
        clearButton.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            inputChangedTo(null, false);
            updateView();
          }
        });
        LinearLayout layout = new LinearLayout(platform);
        layout.addView(textView);
        layout.addView(clearButton);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
        params.weight = 1;
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        params = ((LinearLayout.LayoutParams) clearButton.getLayoutParams());
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        wrap = layout;
      }
      view = Views.addLabel(name, wrap);
      Views.applyEditTextStyle(textView, editable);
    } else if (widgetType.equals("slider")) {
      SeekBar seekBar = new SeekBar(platform);
      if (editable) {
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
          }
          
          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
          }
          
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            inputChangedTo((double) progress, false);
          }
        });
      }
      progressBar = seekBar;
      view = Views.addLabel(name, seekBar);
    } else if (widgetType.equals("percent")) {
      progressBar = new ProgressBar(platform, null, android.R.attr.progressBarStyleHorizontal);
      view = Views.addLabel(name, progressBar);
    } else if (editable) {
      EditText editText = new EditText(platform);
      textView = editText;
      view = Views.addLabel(name, editText);
      if (type == PrimitiveType.NUMBER) {
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
      } else {
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
      }
      Point p = new Point();
      platform.getWindowManager().getDefaultDisplay().getSize(p);
      editText.setMinimumWidth(p.x / 4);
      editText.addTextChangedListener(new TextWatcher() {
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
          String text = s.toString();
          try {
            final Object newValue = type == PrimitiveType.NUMBER ?
                Double.parseDouble(text) : text;
            if (!newValue.equals(value)) {
              inputChangedTo(newValue, true);
            }
          } catch(Exception e) {
          }
        }
      });
    } else {
      textView = new TextView(platform);
      Views.applyEditTextStyle(textView, false);
      view = Views.addLabel(name, textView);
    }
    value = parent.get(name);
    updateView();
  }

  /**
   * Called from UI widgets after changes.
   */
  protected void inputChangedTo(Object newValue, boolean delayNotification) {
    value = newValue;
    parent.set(name, newValue);

    if (delayNotification) {
      if (sendTask != null) {
        sendTask.cancel();
      }

      sendTask = new UiTimerTask(platform) {
        @Override
        public void runOnUiThread() {
          sendTask = null;
          owner.saveData();
          if (onValueChangedListener != null) {
            onValueChangedListener.onValueChanged(value);
          }
        }
      };
      if (timer == null) {
        timer = new Timer();
      }
      timer.schedule(sendTask, 1000);
    } else {
      owner.saveData();
      if (onValueChangedListener != null) {
        onValueChangedListener.onValueChanged(newValue);
      }
    }
  }

  public void setOnValueChangedListener(
      OnValueChangedListener onValueChangedListener) {
    this.onValueChangedListener = onValueChangedListener;
  }


  /**
   * Set the given value and update the ui.
   * The UI is updated even if the values are identical.
   */
  public void setValue(final Object newValue) {
    if (newValue != value) {
      value = newValue;
      parent.set(name, newValue);
    }
    updateView();
  }

  /** 
   * Adjust the UI to an external value change.
   * Safe to call from non-UI threads.
   */
  public void updateView() {
    platform.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (view instanceof Checkable) {
          ((Checkable) view).setChecked(Boolean.TRUE.equals(value));
        } else if (textView != null) {
          if (clearButton != null) {
            if (value == null) {
              clearButton.setVisibility(View.INVISIBLE);
              textView.setText("[Set value]");
            } else {
              clearButton.setVisibility(View.VISIBLE);
              textView.setText(Model.toString(value));
            }
          } else {
            textView.setText(value == null ? "" : Model.toString(value));
          }
          ;
        } else if (progressBar != null && value instanceof Number) {
          progressBar.setProgress(((Number) value).intValue());
        }
      }
    });
  }

  public Object value() { return value; }
  public View view() { return view; }
  public Type type() { return type; }
}
