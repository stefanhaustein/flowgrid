package org.flowgrid.android;

import org.flowgrid.R;
import org.flowgrid.android.widget.ContextMenu;
import org.flowgrid.android.widget.ContextMenu.ItemClickListener;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class PlatformFragment<V extends View> extends Fragment implements ItemClickListener {
  protected static final String MENU_ITEM_CANCEL = "Cancel";
  protected static final String MENU_ITEM_ADD_DOCUMENTATION = "Add documentation";
  protected static final String MENU_ITEM_COPY = "Copy";
  protected static final String MENU_ITEM_CUT = "Cut";
  protected static final String MENU_ITEM_DELETE = "Delete";
  protected static final String MENU_ITEM_DOCUMENTATION = "Documentation";
  protected static final String MENU_ITEM_PLAY = "Play";
  protected static final String MENU_ITEM_PUBLIC = "Public";
  protected static final String MENU_ITEM_RENAME = "Rename";
  protected static final String MENU_ITEM_RENAME_MOVE = "Rename / move";
  protected static final String MENU_ITEM_RESTART = "Restart";
  protected static final String MENU_ITEM_RESTORE = "Restore";
  protected static final String MENU_ITEM_STOP = "Stop";
  private static final HashMap<String,Integer> ACTION_ITEMS = new HashMap<>();

  protected final Class<V> rootViewClass;
  protected MainActivity platform;
  protected V rootView;
  private boolean initialized;
  protected ContextMenu menu;
  private Spannable title = new SpannableString("No Title");
  private String subTitle = null;
  private int titleIcon = R.drawable.ic_arrow_back_white_24dp;
  protected ContextMenu actions;
  private boolean fullscreen;

  static {
    //ACTION_ITEMS.put(MENU_ITEM_ADD_DOCUMENTATION, R.drawable.ic_info_outline_white_24dp);
    ACTION_ITEMS.put(MENU_ITEM_CANCEL, R.drawable.ic_clear_white_24dp);
    ACTION_ITEMS.put(MENU_ITEM_COPY, R.drawable.ic_content_copy_white_24dp);
    ACTION_ITEMS.put(MENU_ITEM_CUT, R.drawable.ic_content_cut_white_24dp);
    ACTION_ITEMS.put(MENU_ITEM_DOCUMENTATION, R.drawable.ic_info_outline_white_24dp);
    ACTION_ITEMS.put(MENU_ITEM_PLAY, R.drawable.ic_play_arrow_white_24dp);
    ACTION_ITEMS.put(MENU_ITEM_STOP, R.drawable.ic_stop_white_24dp);
    ACTION_ITEMS.put(MENU_ITEM_RESTART, R.drawable.ic_refresh_white_24dp);
  }


  protected PlatformFragment(Class<V> rootViewClass) {
    this.rootViewClass = rootViewClass;
  }

  protected void clearMenu() {
    actions = new ContextMenu(platform, null);
    menu = new ContextMenu(platform, null);
    menu.setOnMenuItemClickListener(this);
    getActivity().invalidateOptionsMenu();
  }


  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (initialized) {
      return;
    }
    initialized = true;
    clearMenu();
    onPlatformAvailable(savedInstanceState);
  }
  
  
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.platform = (MainActivity) activity;
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
    //optionsMenu.clear();
    for (ContextMenu.Item action: actions.items()) {
      String title = action.getTitle();
      MenuItem item = optionsMenu.add(title);
      item.setIcon(action.getIcon());
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }
    if (menu != null && menu.size() > 0) {
      MenuItem overflowItem = optionsMenu.add("Overflow");
      overflowItem.setIcon(R.drawable.ic_more_vert_white_24dp);
      overflowItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      overflowItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          showMenu();
          return true;
        }
      });
    }
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (rootView == null) {
      try {
        rootView = rootViewClass.getConstructor(Context.class).newInstance(platform);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return rootView;
  }

  public abstract void onPlatformAvailable(Bundle savedInstanceState);
  
  public ContextMenu.Item addMenuItem(String title) {
    ContextMenu.Item result;
    if (ACTION_ITEMS.containsKey(title)) {
      result = actions.add(title);
      result.setIcon(ACTION_ITEMS.get(title));
    } else {
      result = menu.add(title);
    }
    return result;
  }

  public void setTitle(String title, String subTitle) {
    setTitle(new SpannableString(title), subTitle);
  }

  public void setTitle(Spannable title, String subTitle) {
    this.title = title;
    this.subTitle = subTitle;
    updateTitle();
  }
  
  public void setTitleIcon(int titleIcon) {
    this.titleIcon = titleIcon;
    updateTitle();
  }

  protected void showMenu() {
    if (menu.size() > 0) {
      menu.show();
    }
  }

  public void refresh() {
  }

  @Override
  public void onResume() {
    super.onResume();
    updateTitle();
    refresh();
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home && getArguments() != null) {
      String caller = getArguments().getString("caller");
      if (caller != null && !caller.isEmpty()) {
        platform.showFragmentForTag(caller);
        return true;
      }
    }
    CharSequence title = item.getTitle();
    return title == null ? super.onOptionsItemSelected(item)
        : onContextMenuItemClick(actions.find(title.toString()));
  }

  public MainActivity platform() {
    return platform;
  }

  protected void setFullscreen(boolean fullscreen) {
    this.fullscreen = fullscreen;
    updateTitle();
  }

  private void updateTitle() {
    if (fullscreen) {
      platform.hideActionBar();
    } else {
      platform.showActionBar(title, subTitle, titleIcon);
    }
  }
}
