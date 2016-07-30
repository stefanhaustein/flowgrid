package org.flowgrid.android;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import org.flowgrid.model.Artifact;

public class IdentifierValidator implements TextWatcher {
  private final EditText editText;

  public IdentifierValidator(EditText editText) {
    this.editText = editText;
  }


  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {

  }

  @Override
  public void afterTextChanged(Editable s) {
    String text = editText.getText().toString();
    if (!Artifact.isIdentifier(text)) {
      editText.setError(Artifact.IDENTIFIER_CONSTRAINT_MESSAGE);
    }
  }
}
