package org.flowgrid.android.widget;


import android.view.View;

import org.flowgrid.android.Widget;

public interface ColumnLayoutInterface extends Widget {
  void addView(View view);
  void addView(View view, int index);
  void addView(int index, View view, int colCount, int rowCount);
  void addView(View view, int colCount, int rowCount);

  int getChildCount();
  View getChildAt(int index);
  void removeView(View view);
}
