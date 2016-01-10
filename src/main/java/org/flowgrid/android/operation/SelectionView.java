package org.flowgrid.android.operation;

import android.view.View;
import android.widget.FrameLayout;

public class SelectionView extends View {
  enum Mode {
    CELL, FULL_ROW, FULL_COLUM, CUSTOM
  }
  
  Mode mode = Mode.CELL;
  int col;
  int row;
  int width = 1;
  int height = 1;
  EditOperationFragment flowCodr;

  public SelectionView(EditOperationFragment flowCodr) {
    super(flowCodr.getActivity());
    this.flowCodr = flowCodr;
    setBackgroundColor(0x0880099ff);
  }
  
  protected void onMeasure(int m, int n) {
    float border2 = flowCodr.operationView.cellSize / 5;
    if (mode == Mode.FULL_ROW) {
      setMeasuredDimension(flowCodr.operationView.getWidth(), (int) (flowCodr.operationView.cellSize + border2));
    } else if (mode == Mode.FULL_COLUM) {
      setMeasuredDimension((int) (flowCodr.operationView.cellSize + border2), flowCodr.operationView.getHeight());
    } else {
      setMeasuredDimension((int) (flowCodr.operationView.cellSize * width + border2), (int) (flowCodr.operationView.cellSize * height + border2));
    }
  }

  public void setPosition(int row, int col, int height, int width) {
    this.col = col;
    this.row = row;
    this.width = width;
    this.height = height;
    
    float border = flowCodr.operationView.cellSize / 10;
    
    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
    layoutParams.leftMargin = mode == Mode.FULL_ROW ? 0 : (int) ((col * flowCodr.operationView.cellSize) - flowCodr.operationView.originX - border);
    layoutParams.topMargin = mode == Mode.FULL_COLUM ? 0 : (int) ((row * flowCodr.operationView.cellSize) - flowCodr.operationView.originY - border);
    setLayoutParams(layoutParams);
    requestLayout();
  }

  int col() {
    return col;
  }

  int row() {
    return row;
  }

  void setMode(Mode mode) {
    if (mode != this.mode) {
      
    //  setBackgroundColor(mode == Mode.CUSTOM ? 0x088ff0000 : 0x0880099ff);
      
      this.mode = mode;
      setPosition(row, col, width, height);
      requestLayout();
    }
  }
}
