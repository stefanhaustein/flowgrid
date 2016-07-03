package org.flowgrid.android.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.mobidevelop.widget.SplitPaneLayout;

import org.flowgrid.android.MainActivity;
import org.flowgrid.android.UiTimerTask;
import org.flowgrid.android.Widget;
import org.flowgrid.android.widget.ColumnLayout;
import org.flowgrid.android.widget.ColumnLayoutInterface;

public class MetaLayout implements Widget, ColumnLayoutInterface {
  static final String TAG = "MetaLayout";
  SplitPaneLayout rootLayout;
  ScrollView scrollView;
  ColumnLayout mainLayout;
  LinearLayout fixedPane;
  int fixedIndex = -1;
  private MainActivity platform;

  public static void adjustSplitPaneToColumnLayoutDeferred(
      MainActivity platform, final ColumnLayout columnLayout, final SplitPaneLayout splitPane) {
    new UiTimerTask(platform) {
      @Override
      public void runOnUiThread() {
        if (splitPane.getOrientation() == SplitPaneLayout.ORIENTATION_HORIZONTAL) {
          splitPane.setSplitterPositionPercent(columnLayout.rowCount() == 0 ? 0 : 0.25f);
        } else {
          int pos = columnLayout.cellHeight() * Math.min(2, columnLayout.rowCount()) + splitPane.getSplitterSize() / 2;
          splitPane.setSplitterPosition(pos);
        }
        splitPane.requestLayout();
      }
    }.schedule(100);
  }


  public static void matchParent(View view) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (params != null) {
      params.height = ViewGroup.LayoutParams.MATCH_PARENT;
      params.width = ViewGroup.LayoutParams.MATCH_PARENT;
      view.setLayoutParams(params);
    }
  }

  public MetaLayout(MainActivity platform) {
    this(platform, new SplitPaneLayout(platform));
  }

  public MetaLayout(final MainActivity platform, final SplitPaneLayout root) {
    this.rootLayout = root;
    this.platform = platform;
    root.setmAutoOrientation(true);
    root.setSplitterMovable(false);
    root.setSplitterSize(0);
    scrollView = new ScrollView(platform);
    rootLayout.addView(scrollView);
    mainLayout = new ColumnLayout(platform);
    scrollView.addView(mainLayout);
    fixedPane = new LinearLayout(platform);
    rootLayout.addView(fixedPane);
    matchParent(fixedPane);
    rootLayout.setSplitterPositionPercent(1f);

    View.OnLayoutChangeListener layoutChangeListener = new View.OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (fixedIndex != -1) {
          adjustSplitPaneToColumnLayoutDeferred(platform, mainLayout, rootLayout);
        }
      }
    };

    rootLayout.addOnLayoutChangeListener(layoutChangeListener);
    mainLayout.addOnLayoutChangeListener(layoutChangeListener);
  }

  @Override
  public void addView(View view) {
    addView(getChildCount(), view, 1, 1);
  }

  @Override
  public void addView(View view, int index) {
    addView(index, view, 1, 1);
  }

  @Override
  public void addView(int index, View child, int colSpan, int rowSpan) {
    if (rowSpan <= 0 && fixedIndex == -1) {
      fixedPane.addView(child);
      matchParent(child);
      rootLayout.setSplitterPositionPercent(0.25f);
      fixedIndex = index;
    } else {
      mainLayout.addView(child, colSpan, rowSpan);
    }
  }

  public void addView(View child, int colSpan, int rowSpan) {
    addView(getChildCount(), child, colSpan, rowSpan);
  }

  @Override
  public int getChildCount() {
    return mainLayout.getChildCount() + fixedIndex == -1 ? 0 : 1;
  }

  @Override
  public View getChildAt(int index) {
    if (index == fixedIndex) {
      return fixedPane.getChildAt(0);
    }
    return mainLayout.getChildAt(index - (index < fixedIndex ? 0 : 1));
  }

  @Override
  public void removeView(View view) {
    if (fixedIndex != -1 && view == fixedPane.getChildAt(0)) {
      fixedPane.removeView(view);
      fixedIndex = -1;
      rootLayout.setSplitterPositionPercent(1);
    } else {
      for (int i = 0; i < mainLayout.getChildCount(); i++) {
        if (mainLayout.getChildAt(i) == view) {
          if (i < fixedIndex) {
            fixedIndex--;
          }
          mainLayout.removeViewAt(i);
          break;
        }
      }

    }
  }


  @Override
  public View view() {
    return rootLayout;
  }


}
