package org.flowgrid.android.data;

import org.flowgrid.android.PlatformFragment;
import org.flowgrid.model.Instance;
import org.flowgrid.model.Member;
import org.flowgrid.model.Property;
import org.flowgrid.model.StructuredData;
import org.flowgrid.android.widget.ColumnLayout;
import org.flowgrid.android.widget.ContextMenu.Item;

import android.os.Bundle;
import android.view.View;

public class InstanceFragment extends PlatformFragment<ColumnLayout> {
  public InstanceFragment() {
    super(ColumnLayout.class);
  }
  
  @Override
  public boolean onContextMenuItemClick(Item item) {
    return false;
  }

  @Override
  public void onPlatformAvailable(Bundle savedInstanceState) {
    rootView.setColumnCount(1, 2);
    Bundle args = getArguments();
    Member owner = (Member) platform.model().artifact(args.getString("artifact"));
    String[] path = args.getString("data").split("/", -1);
    StructuredData parent = owner.structuredData(path);
    String name = path[path.length - 1];
    final Instance instance = (Instance) parent.get(name);
    StringBuilder sb = new StringBuilder(owner.name());
    for (int i = 0; i < path.length - 1; i++) {
      sb.append('/');
      sb.append(path[i]);
    }
    setTitle(name, sb.toString());
    boolean edit = "edit".equals(args.getString("action"));

    for (final Property property: instance.classifier.properties(null)) {
      String[] childPath = new String[path.length + 1];
      System.arraycopy(path, 0, childPath, 0, path.length);
      childPath[path.length] = property.name();
      
      DataWidget dataWidget = new DataWidget(platform, owner, childPath);
      if (edit) {
        dataWidget.setOnValueChangedListener(new DataWidget.OnValueChangedListener() {
          @Override
          public void onValueChanged(Object newValue) {
            instance.set(property.name(), newValue);
          }
        });
      }
      View view = dataWidget.view();
      rootView.addView(view, 1, 1);
    }
  }
}
