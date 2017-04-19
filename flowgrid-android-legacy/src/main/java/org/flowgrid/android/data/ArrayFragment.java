package org.flowgrid.android.data;

import java.util.List;

import org.flowgrid.R;
import org.flowgrid.android.AbstractListAdapter;
import org.flowgrid.model.Callback;
import org.flowgrid.android.PlatformFragment;
import org.flowgrid.android.Views;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Member;
import org.flowgrid.model.Model;
import org.flowgrid.model.StructuredData;
import org.flowgrid.model.TypeAndValue;
import org.flowgrid.android.widget.ContextMenu.Item;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ArrayFragment extends PlatformFragment<ListView> {
  private Member owner;
  private List<Object> list;
  private Adapter adapter;
  private boolean edit;
  private String[] path;
  private ArrayType arrayType;

  public ArrayFragment() {
    super(ListView.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onPlatformAvailable(Bundle savedInstanceState) {
    Bundle args = getArguments();
    owner = (Member) platform.model().artifact(args.getString("artifact"));
    path = args.getString("data").split("/");
    StructuredData parent = owner.structuredData(path);
    String name = path[path.length - 1];
    list = (List<Object>) parent.get(name);
    arrayType = (ArrayType) parent.type(name);
    edit = "edit".equals(args.getString("action"));
    StringBuilder sb = new StringBuilder(owner.name());
    for (int i = 0; i < path.length - 1; i++) {
      sb.append('/');
      sb.append(path[i]);
    }
    setTitle(name, sb.toString());
    adapter = new Adapter();
    rootView.setAdapter(adapter);
    rootView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        edit(position, view);
      }
    });
    if (edit) {
      addMenuItem("Add item");
    }
  }

  private void edit(final int index, View view) {
    String[] childPath = new String[path.length + 1];
    System.arraycopy(path, 0, childPath, 0, path.length);
    childPath[path.length] = "" + index;
    platform.editStructuredDataValue(owner, childPath, view, new Callback<TypeAndValue>() {
      @Override
      public void run(TypeAndValue variant) {
        if (index == list.size()) {
          list.add(variant.value);
        } else {
          list.set(index, variant.value);
        }
        adapter.notifyDataSetChanged();
      }
    });
  }

  @Override
  public boolean onContextMenuItemClick(Item item) {
    String title = item.getTitle();
    if (title.equals("Add item")) {
      // Handing in null as the context view is consistent with what ContextMenu does.
      edit(adapter.getCount(), null);
      return true;
    }
    return true;
  }
  
  class Adapter extends AbstractListAdapter<Object> {
    @Override
    public int getCount() {
      return list.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      LinearLayout layout = new LinearLayout(platform);
      TextView positionTextView = new TextView(platform); //(TextView) createViewFromResource(platform, android.R.layout.simple_list_item_1, parent);
      positionTextView.setText(String.valueOf(position) + ") ");
      Views.applyEditTextStyle(positionTextView, false);
      layout.addView(positionTextView);
      
      TextView contentTextView = new TextView(platform); //  createViewFromResource(platform, android.R.layout.simple_list_item_1, parent);
      final Object o = getItem(position);
      contentTextView.setText(Model.toString(o));
      layout.addView(contentTextView);
      Views.applyEditTextStyle(contentTextView, edit);
      ((LinearLayout.LayoutParams) contentTextView.getLayoutParams()).weight = 1;
      
      if (edit) {
        final ImageView upImage = new ImageView(platform);
        final ImageView downImage = new ImageView(platform);
        final ImageView removeImage = new ImageView(platform);
        OnClickListener listener = new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (v == removeImage) {
              list.remove(position);
            } else {
              int p2 = position + (v == downImage ? 1 : -1);
              list.set(position, getItem(p2));
              list.set(p2, o);
            }
            notifyDataSetChanged();
          }
        };
        upImage.setImageResource(R.drawable.ic_keyboard_arrow_up_white_24dp);
        if (position == 0) {
          upImage.setAlpha(0.5f);
        } else {
          upImage.setOnClickListener(listener);
        }
        layout.addView(upImage);

        downImage.setImageResource(R.drawable.ic_keyboard_arrow_down_white_24dp);
        if (position == list.size() - 1) {
          downImage.setAlpha(0.5f);
        } else {
          downImage.setOnClickListener(listener);
        }
        layout.addView(downImage);

        removeImage.setImageResource(R.drawable.ic_clear_white_24dp);
        layout.addView(removeImage);
        removeImage.setOnClickListener(listener);
      }
      
      
      return layout;
    }

    @Override
    public Object getItem(int position) {
      return list.get(position);
    }
    
  }
}
