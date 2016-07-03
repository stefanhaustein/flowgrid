package org.flowgrid.android.classifier;

import org.flowgrid.android.ArtifactFragment;
import org.flowgrid.model.Callback;
import org.flowgrid.model.VirtualOperation;
import org.flowgrid.android.widget.ContextMenu.Item;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class VirtualOperationFragment extends ArtifactFragment<ListView> {
  VirtualOperation operation;
  VirtualOperationListAdapter adapter;
  
  public VirtualOperationFragment() {
    super(ListView.class);
  }
  
  

  @Override
  public void onPlatformAvailable(Bundle savedInstanceState) {
    operation = (VirtualOperation) platform.model().artifact(
        getArguments().getString("artifact"), VirtualOperation.class);
    setArtifact(operation);
    addMenuItem(operation.hasDocumentation() ? MENU_ITEM_DOCUMENTATION : MENU_ITEM_ADD_DOCUMENTATION);
    addMenuItem("Add input");
    addMenuItem("Add output");
    addMenuItem(MENU_ITEM_RENAME_MOVE);
    addMenuItem(MENU_ITEM_DELETE);

    adapter = new VirtualOperationListAdapter(platform, operation);
    rootView.setAdapter(adapter);
    rootView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean output = position >= operation.inputCount();
        if (output) {
          position -= operation.inputCount();
        }
        ParameterDialog.show(platform, operation, output, position, new Callback<Void>() {
          @Override
          public void run(Void value) {
            adapter.notifyDataSetChanged();
            operation.classifier.save();
          }
        });
      }
    });
  }


  @Override
  public boolean onContextMenuItemClick(Item item) {
    String title = item.getTitle().toString();
    if (title.startsWith("Add ")) {
      ParameterDialog.show(platform, operation, title.equals("Add output"), -1, new Callback<Void>() {
        @Override
        public void run(Void value) {
          adapter.notifyDataSetChanged();
          operation.classifier.save();
        }
      });
      return true;
    }
    return super.onContextMenuItemClick(item);
  }
}
