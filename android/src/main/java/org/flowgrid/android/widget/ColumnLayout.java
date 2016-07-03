package org.flowgrid.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class ColumnLayout extends ViewGroup implements ColumnLayoutInterface {
  int columnsPortrait = 1;
  int columnsLandscape = 2;
  int columns;
  int rowCountCache;
  int cellHeight;
  int cellWidthCache;
  int[] colIndexCache;
  int[] rowIndexCache;
  int[] rowStartCache;
  float pixelPerDp;
  Point screenSize = new Point();

  public ColumnLayout(Context context) {
    super(context);
    pixelPerDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
//    cellHeight = (int) (64 * pixelPerDp);
    setWillNotDraw(false);
  }

  public void addView(View child, int colSpan, int rowSpan) {
    addView(getChildCount(), child, colSpan, rowSpan);
  }

  public void addView(int index, View child, int colSpan, int rowSpan) {
    this.addView(child, index);
    LayoutParams params = (LayoutParams) child.getLayoutParams();
    params.colSpan = colSpan;
    params.rowSpan = rowSpan;
  }
  
  public int cellHeight() {
    return cellHeight;
  }

  public int cellWidth() {
    return cellWidthCache;
  }

  public int rowCount() {
    return rowCountCache;
  }
  
  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new ColumnLayout.LayoutParams(getContext(), attrs);
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
      return new ColumnLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  }

  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
      return new ColumnLayout.LayoutParams(p);
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
      return p instanceof ColumnLayout.LayoutParams;
  }
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int count = getChildCount();

    WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    windowManager.getDefaultDisplay().getSize(screenSize);

    int availableWidth = MeasureSpec.getSize(widthMeasureSpec);
    if (availableWidth == 0) {
      availableWidth = screenSize.x;
    }
    columns = availableWidth > screenSize.y ? columnsLandscape : columnsPortrait;

    if (cellHeight == 0) {
      TextView dummyTextView = new TextView(getContext());
      EditText dummyEditText = new EditText(getContext());
      dummyTextView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
      dummyEditText.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
      cellHeight = dummyTextView.getMeasuredHeight() + dummyEditText.getMeasuredHeight();
    }

    // Step 1: Do the tetris.
    rowCountCache = 0;
    colIndexCache = new int[count];
    rowIndexCache = new int[count];
    int[] filledTo = new int[columns];
    int col = 0;
    int row = 0;
    for (int i = 0; i < count; i++) {
      View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      ColumnLayout.LayoutParams params = (ColumnLayout.LayoutParams) child.getLayoutParams();
      int colSpan = params.colSpan();

      while (true) {
        if (col + colSpan > columns) {
          col = 0; 
          row++;
        }
        boolean ok = true;
        for (int c = col; c < col + colSpan; c++) {
          if (filledTo[c] > row) {
            ok = false;
            break;
          }
        }
        if (ok) {
          break;
        }
        col++;
      }
      colIndexCache[i] = col;
      rowIndexCache[i] = row;
      int rowSpan = params.rowSpan();
      for (int c = col; c < col + colSpan; c++) {
        filledTo[c] = row + rowSpan;
      }
      rowCountCache = Math.max(rowCountCache, row + rowSpan);
      col++;
    }

    rowStartCache = new int[rowCountCache + 1];
    for (int i = 0; i < rowCountCache + 1; i++) {
      rowStartCache[i] = cellHeight * i;
    }
    
    cellWidthCache = 0;
    int childState = 0;

    int requestedFullWidth = MeasureSpec.getSize(widthMeasureSpec);
    boolean fullWidthIsLimited = (widthMeasureSpec & (MeasureSpec.AT_MOST | MeasureSpec.EXACTLY)) != 0;

    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      ColumnLayout.LayoutParams params = (ColumnLayout.LayoutParams) child.getLayoutParams();
      int colSpan = params.colSpan();
      
      int childWidthSpec;
      if (fullWidthIsLimited) {
        childWidthSpec = requestedFullWidth * colSpan / columns | MeasureSpec.getMode(widthMeasureSpec); 
      } else {
        childWidthSpec = widthMeasureSpec;
      }
      
      int rowSpan = params.rowSpan();
      int childHeightSpec;
      childHeightSpec = (cellHeight * rowSpan) | MeasureSpec.EXACTLY;
      child.measure(childWidthSpec, childHeightSpec);
      cellWidthCache = Math.max(cellWidthCache, child.getMeasuredWidth() / colSpan);

      //cellHeight = Math.max(cellHeight, childMeasuredHeight / (fullHeightIsLimited ? params.rowSpan : 1) + localLabelHeight);
      childState = combineMeasuredStates(childState, child.getMeasuredState());
    }

    int totalHeight = Math.max(cellHeight * rowCountCache, getSuggestedMinimumHeight());
    int totalWidth = Math.max(cellWidthCache * columns, getSuggestedMinimumWidth());

    // Report our final dimensions.
    setMeasuredDimension(resolveSizeAndState(totalWidth, widthMeasureSpec, childState),
        resolveSizeAndState(totalHeight, heightMeasureSpec,
            childState << MEASURED_HEIGHT_STATE_SHIFT));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    if (rowCountCache == 0) {
      return;
    }
    int count = getChildCount();
   // cellWidthCache = (right - left) / columns;
   // cellHeightCache = (bottom - top) / rowCountCache;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      ColumnLayout.LayoutParams params = (ColumnLayout.LayoutParams) child.getLayoutParams();
      int x0 = /*left + */colIndexCache[i] * cellWidthCache;
      int x1 = x0 + cellWidthCache * params.colSpan();
      int y0 = /*top + top + */ rowStartCache[rowIndexCache[i]];
      int y1 = y0 + child.getMeasuredHeight();
      child.layout(x0, y0, x1, y1);
    }
  }
  
  public void onDraw(Canvas canvas) {
    for (int i = 0; i < getChildCount(); i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
    }

  }

  public void setColumnCount(int columnsPortrait, int columnsLandscape) {
    if (this.columnsPortrait != columnsPortrait || this.columnsLandscape != columnsLandscape) {
      this.columnsPortrait = columnsPortrait;
      this.columnsLandscape = columnsLandscape;
      requestLayout();
    }
  }

  @Override
  public boolean shouldDelayChildPressedState() {
    return false;
  }

  @Override
  public View view() {
    return this;
  }


  public class LayoutParams extends ViewGroup.LayoutParams {
    public int colSpan = 1;
    public int rowSpan = 1;

    public LayoutParams(ViewGroup.LayoutParams p) {
      super(p);
    }
    
    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
	}  
    
    public LayoutParams(int w, int h) {
      super(w, h);
    }
    
    private int colSpan() {
      return colSpan < 1 ? columns : Math.min(colSpan, columns);
    }

    private int rowSpan() {
      return rowSpan < 1 ? 2 : rowSpan;
    }
  }

}
