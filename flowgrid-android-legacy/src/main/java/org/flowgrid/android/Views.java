package org.flowgrid.android;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class Views {
  static EditText editText;
  static Float pixelPerDp;
  
  public static float pxF(Context context, float dp) {
    if (pixelPerDp == null) {
      pixelPerDp = Float.valueOf(TypedValue.applyDimension(
          TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics()));
    }
    return dp * pixelPerDp.floatValue();
  }
  
  public static int px(Context context, float dp) {
    return Math.round(pxF(context, dp));
  }
  
  
  public static ViewGroup addLabel(String label, View view) {
    LinearLayout wrapper = new LinearLayout(view.getContext());
    wrapper.setOrientation(LinearLayout.VERTICAL);
    TextView labelView = new TextView(view.getContext());
    labelView.setText(label);
    wrapper.addView(labelView);
    wrapper.addView(view);
    return wrapper;
  }
  
  
  public static void applyEditTextStyle(TextView textView, boolean active) {
    if (editText == null) {
      editText = new EditText(textView.getContext());
    }
    if (active) {
      textView.setTextColor(editText.getTextColors().getDefaultColor());
    }
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, editText.getTextSize());
    textView.setPadding(editText.getPaddingLeft(), editText.getPaddingTop(), editText.getPaddingRight(), editText.getPaddingBottom());
   // textView.setLines(1);
    textView.setEllipsize(TruncateAt.END);
  /*  if (editText.getMinHeight() != -1) {
      textView.setMinHeight(editText.getMinHeight());
    }*/
  }

  public static void setSpinnerOptions(Spinner spinner, Object[] options) {
    ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(spinner.getContext(),
        android.R.layout.simple_spinner_item, options);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  public static Spinner createSpinner(Context context, Object[] options) {
    Spinner spinner = new Spinner(context);
    setSpinnerOptions(spinner, options);
    return spinner;
  }

  public static Spinner createSpinner(Context context, Object[] options, Object selected) {
    Spinner spinner = createSpinner(context, options);
    for (int i = 0; i < options.length; i++) {
      if (options[i].equals(selected)) {
        spinner.setSelection(i);
        break;
      }
    }
    return spinner;
  }

  public static TabHost createTabHost(Context context) {
    // Create the TabWidget (the tabs)
    TabWidget tabWidget = new TabWidget(context);
    tabWidget.setId(android.R.id.tabs);

    // Create the FrameLayout (the content area)
    FrameLayout tabContent = new FrameLayout(context);
    tabContent.setId(android.R.id.tabcontent);
    LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    
    int px = px(context, 4);
    frameLayoutParams.setMargins(px, 4 * px, px, 2 * px);
    tabContent.setLayoutParams(frameLayoutParams);

    // Create the container for the above widgets
    LinearLayout tabHostLayout = new LinearLayout(context);
    tabHostLayout.setOrientation(LinearLayout.VERTICAL);
    tabHostLayout.addView(tabWidget);
    tabHostLayout.addView(tabContent);
    
    // Make sure the total size accounts for the largest tab.
    tabContent.setMeasureAllChildren(true);

    // Create the TabHost and add the container to it.
    TabHost tabHost = new TabHost(context, null);
    tabHost.addView(tabHostLayout);
    tabHost.setup();

    return tabHost;
  }
  
  public static void addTab(TabHost tabHost, String label, final View view) {
    TabHost.TabSpec tab = tabHost.newTabSpec(label);
    tab.setContent(new TabHost.TabContentFactory() {
      @Override
      public View createTabContent(String tag) {
        return view;
      }
    });
    tab.setIndicator(label);
    tabHost.addTab(tab);
    // Make sure the tab content gets added to the frame, so the size doesn't
    // change later when switching.
    tabHost.setCurrentTabByTag(label);
    tabHost.setCurrentTab(0);
  }

}
