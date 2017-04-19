package org.flowgrid.android;

import org.flowgrid.R;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.android.widget.ContextMenu;
import org.flowgrid.android.widget.ContextMenu.Item;
import org.flowgrid.android.widget.ContextMenu.ItemClickListener;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.DisplayType;
import org.flowgrid.model.Module;

import android.support.v4.widget.DrawerLayout;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

public abstract class ArtifactFragment<V extends View> extends PlatformFragment<V> implements ItemClickListener{
  
  protected Artifact artifact;
  
  protected ArtifactFragment(Class<V> rootViewClass) {
    super(rootViewClass);
  }

  public void setArtifact(Artifact artifact) {
    this.artifact = artifact;
    if (artifact.owner() == null) {
      setTitle("FlowGrid", null);
      setTitleIcon(R.drawable.ic_menu_white_24dp);
    } else {
      String path = artifact.qualifiedName();
      int cut = path.lastIndexOf('/');
      if (cut > 0) {
        path = path.substring(0, cut + 1);
      } else {
        path = null;
      }
      String title = artifact.toString(DisplayType.TITLE);
      SpannableString spannable = new SpannableString(title);
      int nameStart = title.indexOf('\'') + 1;
      if (artifact instanceof CustomOperation && ((CustomOperation) artifact).asyncInput()) {
        spannable.setSpan(new UnderlineSpan(), nameStart, title.length() - 1, 0);
      }
      if (artifact.isPublic() && !(artifact instanceof Module)) {
        spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), nameStart, title.length() - 1, 0);
      }
      setTitle(spannable, path);
    }
  }

  @Override
  public ContextMenu.Item addMenuItem(String label) {
    ContextMenu.Item item = super.addMenuItem(label);
    if (label.equals(MENU_ITEM_PUBLIC)) {
      item.setCheckable(true).setChecked(artifact.isPublic());
    }
    return item;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (super.onOptionsItemSelected(item)) {
      return true;
    }
    if (item.getItemId() == android.R.id.home) {
      if (artifact.owner() != null) {
        platform.openArtifact(artifact.owner());
        return true;
      }
      DrawerLayout drawer = (DrawerLayout) platform.findViewById(R.id.drawer_layout);
      if (drawer.isDrawerOpen(Gravity.START)) {
        drawer.closeDrawers();
      } else {
        drawer.openDrawer(Gravity.START);
      }
      return true;
    }
    return false;
  }
  
  public boolean onContextMenuItemClick(Item item) {
    String title = item.getTitle().toString();
    if (MENU_ITEM_DELETE.equals(title)) {
      Dialogs.confirm(platform, "Confirmation", "Delete '" + artifact.qualifiedName() +
          "' and all contained content? This cannot be undone.", new Runnable() {
        @Override
        public void run() {
          artifact.rename(null, null, null);
          platform.finishFragment(ArtifactFragment.this);
        }
      });
      return true;
    }
    if (MENU_ITEM_DOCUMENTATION.equals(title) || MENU_ITEM_ADD_DOCUMENTATION.equals(title)) {
      HelpDialog.show(platform, artifact == artifact.model().rootModule ? "Flowgrid" : artifact.toString(DisplayType.TITLE), artifact.documentation(), new Callback<String>() {
        @Override
        public void run(String value) {
          if (artifact.setDocumentation(value)) {
            artifact.save();
          }
        }
      });
      return true;
    }
    if (MENU_ITEM_PUBLIC.equals(title)) {
      artifact.setPublic(!artifact.isPublic());
      item.setChecked(artifact.isPublic());
      artifact.save();
      return true;
    }
    if (MENU_ITEM_RENAME_MOVE.equals(title) ||
        MENU_ITEM_RENAME.equals(title)) {
      MoveDialog.show(this, artifact);
      return true;
    }
    return false;
  }
}
