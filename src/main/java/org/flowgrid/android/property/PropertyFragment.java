package org.flowgrid.android.property;

import org.flowgrid.android.ArtifactFragment;
import org.flowgrid.android.Views;
import org.flowgrid.android.data.DataWidget;
import org.flowgrid.android.type.TypeFilter;
import org.flowgrid.android.type.TypeSpinner;
import org.flowgrid.model.Property;
import org.flowgrid.model.Type;
import org.flowgrid.android.widget.ColumnLayout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class PropertyFragment extends ArtifactFragment<ColumnLayout> {
  Property property;
  DataWidget valueWidget;
  int dataViewIndex;

  public PropertyFragment() {
    super(ColumnLayout.class);
  }

  private void setType(Type type) {
    if (valueWidget != null) {
      if (type == valueWidget.type()) {
        return;
      }
      rootView.removeView(valueWidget.view());
    }
    
    if (type != property.type()) {
      property.setType(type);
      property.save();
    }

    valueWidget = new DataWidget(platform, property, property.classifier != null ? "Initial value" : "Constant value");
    rootView.addView(valueWidget.view(), dataViewIndex);
    ColumnLayout.LayoutParams params = (ColumnLayout.LayoutParams) valueWidget.view().getLayoutParams();
    params.colSpan = 0;
    params.rowSpan = 1;
    valueWidget.setOnValueChangedListener(new DataWidget.OnValueChangedListener() {
      @Override
      public void onValueChanged(Object newValue) {
        property.setValue(newValue);
        property.save();
      }
    });
  }
  
  public void onPause() {
    super.onPause();
    property.save();
  }


  @Override
  public void onPlatformAvailable(Bundle savedInstanceState) {
    property = (Property) platform.model().artifact(getArguments().getString("artifact"));
    setArtifact(property);
    addMenuItem(MENU_ITEM_PUBLIC);
    if (property.classifier == null) {
      addMenuItem(MENU_ITEM_RENAME_MOVE);
    } else {
      addMenuItem(MENU_ITEM_RENAME);
    }
    addMenuItem(MENU_ITEM_DELETE);

    rootView.setColumnCount(1, 1);

    TypeSpinner typeSpinner = new TypeSpinner(
        platform, property.module, Type.ANY, property.classifier == null ? TypeFilter.INSTANTIABLE : TypeFilter.ALL);
    typeSpinner.setType(property.type());
    typeSpinner.setOnTypeChangedListener(new TypeSpinner.OnTypeChangedListener() {
      @Override
      public void onTypeChanged(Type type) {
        setType(type);
      }
    });
    rootView.addView(Views.addLabel("Type", typeSpinner), 0, 1);
    dataViewIndex = rootView.getChildCount();
    
    // Make sure the value widget is added when this is called again..
    valueWidget = null;
    setType(property.type());
    
    final EditText documentationEditText = new EditText(platform);
    documentationEditText.setText(property.documentation());
    documentationEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
    documentationEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        property.setDocumentation(documentationEditText.getText().toString());
        property.save();
      }
    });
    View documentationLabelContainer = Views.addLabel("Documentation", documentationEditText);
    rootView.addView(documentationLabelContainer, 0, 1);
  }
}
