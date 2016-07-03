package org.flowgrid.android.classifier;

import org.flowgrid.android.AbstractListAdapter;
import org.flowgrid.model.VirtualOperation;
import org.flowgrid.model.VirtualOperation.Parameter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class VirtualOperationListAdapter extends AbstractListAdapter<Parameter> {
  
  private final Context context;
  private final VirtualOperation operation;

  public VirtualOperationListAdapter(Context context, VirtualOperation operation) {
    this.context = context;
    this.operation = operation;
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    TextView tv = (TextView) ((convertView instanceof TextView) ? convertView : 
      createViewFromResource(context, android.R.layout.simple_list_item_1, parent));
    VirtualOperation.Parameter entry = getItem(position);
    boolean out = position >= operation.inputParameterCount();
    int index = position - (out ? operation.inputParameterCount() : 0);
    tv.setText((out ? "Out " : "In ") + (index + 1) + ". " + entry.name + ": " + entry.type.toString());
    return tv;
}

  @Override
  public int getCount() {
    return operation.inputParameterCount() + operation.outputParameterCount();
  }

  @Override
  public Parameter getItem(int position) {
    return position >= operation.inputParameterCount() ? operation.outputParameter(position - operation.inputParameterCount()) : 
      operation.inputParameter(position);
  }
  
  public void notifyDataSetChanged() {
    super.notifyDataSetChanged();
  }
  
}