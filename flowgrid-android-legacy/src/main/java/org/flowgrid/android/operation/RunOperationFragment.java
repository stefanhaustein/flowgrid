package org.flowgrid.android.operation;

import android.os.Bundle;

import com.mobidevelop.widget.SplitPaneLayout;

import org.flowgrid.android.api.CanvasView;
import org.flowgrid.android.widget.MetaLayout;
import org.flowgrid.android.widget.ContextMenu;
import org.flowgrid.model.Artifact;

public class RunOperationFragment extends AbstractOperationFragment<SplitPaneLayout> {

  private MetaLayout metaLayout;
  public RunOperationFragment() {
    super(SplitPaneLayout.class);
  }


  @Override
  public boolean onContextMenuItemClick(ContextMenu.Item item) {
    if (super.onContextMenuItemClick(item)) {
      return true;
    }

    final String label = item.getTitle().toString();
    if (label.equals(MENU_ITEM_RESTART)) {
      stop();
      start();
      return true;
    }
    return false;
  }

    @Override
  public void onPlatformAvailable(Bundle savedInstanceState) {
    controlLayout = metaLayout = new MetaLayout(platform, (SplitPaneLayout) rootView);
    super.onPlatformAvailable(savedInstanceState);
    if (operation.asyncInput()) {
      addMenuItem(MENU_ITEM_RESTART);
    }
  }

  @Override
  public void updateLayout() {
    super.updateLayout();
    if (controlLayout.getChildCount() == 1 && controlLayout.getChildAt(0) instanceof CanvasView) {
      setFullscreen(true);
    } else {
      setFullscreen(false);
      Artifact artifact = "main".equals(operation.name()) ? operation().owner() : operation();
      setTitle(artifact.name().substring(0, 1).toUpperCase() + artifact.name().substring(1), null);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    start();
  }
}
