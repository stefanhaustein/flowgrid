package org.flowgrid.android.widget;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.flowgrid.R;
import org.flowgrid.android.Views;
import org.flowgrid.model.Callback;
import org.flowgrid.model.DisabledMap;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;


public class ContextMenu  {
  private static final int ITEM_LAYOUT_ID = android.R.layout.simple_spinner_dropdown_item;
  private Context context;
  private View view;
  ArrayList<Item> items = new ArrayList<Item>();
  ItemClickListener listener;
  static TextView exampleTextView;
  private boolean anyCheckableOrIcon;
  private boolean editDisabledMode;
  private DisabledMap disabledMap;
  private Callback<Void> editDisabledCallback;
  private HelpProvider helpProvider;

  public void setDisabledMap(DisabledMap disabledMap, boolean editDisabledMode, Callback<Void> editDisabledCallback) {
    this.disabledMap = disabledMap;
    this.editDisabledMode = editDisabledMode;
    this.editDisabledCallback = editDisabledCallback;
  }

  public interface ItemClickListener {
    boolean onContextMenuItemClick(Item item);
  }


  public interface HelpProvider {
    String getHelp(String label);
  }


  public class Item {
    private String title;
    private boolean enabled = true;
    private ContextMenu subMenu;
    private Callable<String> help;
    private boolean checkable;
    private boolean checked;
    private int iconId = -1;
    private boolean discouraged;
    private Drawable iconDrawable;

    private Item(String label) {
      this.title = label;
      if (helpProvider != null) {
        final String helpText = helpProvider.getHelp(label);
        if (helpText != null) {
          help = new Callable<String>() {
            @Override
            public String call() {
              return helpText;
            }
          };
        }
      }
    }

    public void setEnabled(boolean b) {
      this.enabled = b;
    }

    public String getTitle() {
      return title;
    }

    public Item setHelp(final String help) {
      this.help = new Callable<String>() {
        @Override
        public String call() {
          return help;
        }
      };
      return this;
    }
    
    public Item setHelp(Callable<String> help) {
      this.help = help;
      return this;
    }

    public Item setCheckable(boolean b) {
      this.checkable = b;
      return this;
    }

    public Item setChecked(boolean checked) {
      this.checked = checked;
      return this;
    }

    public void setDiscouraged(boolean discouraged) {
      this.discouraged = discouraged;
    }

    public boolean isDiscouraged() {
      return discouraged;
    }

    public boolean isChecked() {
      return checked;
    }

    public boolean hasSubMenu() {
      return subMenu != null;
    }

    public void setIcon(Drawable drawable) {
      this.iconDrawable = drawable;
    }

    public void setIcon(int id) {
      this.iconId = id;
    }


    public ContextMenu getSubMenu() {
      return subMenu;
    }

    public void propagateDisabledState(ContextMenu target) {
      if (disabledMap != null) {
        target.setDisabledMap(disabledMap.getChild(title), editDisabledMode, editDisabledCallback);
      }
    }

    public int getIcon() {
      return iconId;
    }
  }

  protected static View createViewFromResource(Context context, int resourceId, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    return inflater.inflate(resourceId, parent, false);
  }
  
  public ContextMenu(Context context, View view) {
    this.context = context;
    this.view = view;
    
    if (exampleTextView == null) {
      ViewGroup tmp = new LinearLayout(context);
      exampleTextView = (TextView) 
          createViewFromResource(context, ITEM_LAYOUT_ID, tmp);
    }
  }

  public Item add(String label) {
    return add(new Item(label));
  }

  public Item add(Item item) {
    items.add(item);
    return item;
  }

  public Item addSubMenu(String title) {
    ContextMenu subMenu = new ContextMenu(context, view);
    subMenu.setHelpProvider(helpProvider);
    Item item = add(title);
    item.subMenu = subMenu;
    return item;
  }

  public Iterable<Item> items() {
    return items;
  }

  public Item find(String s) {
    for (Item item: items) {
      if (item.getTitle().equals(s)) {
        return item;
      }
    }
    return null;
  }

  public void show() {
    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
    final ListView list = new ListView(context);
    anyCheckableOrIcon = false;
    for (Item item: items) {
      if (item.checkable || item.iconId != -1 || editDisabledMode || item.iconDrawable != null) {
        anyCheckableOrIcon = true;
        break;
      }
    }
    
    
    final ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(context, ITEM_LAYOUT_ID, items) {
      @Override
      public boolean isEnabled(int position) {
        Item item = getItem(position);
        return item.enabled &&
            (editDisabledMode || disabledMap == null || disabledMap.isEnabled(item.getTitle()));
      }
      
      @Override
      public boolean areAllItemsEnabled() {
        return false;
      }
      
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        final LinearLayout layout = (convertView instanceof LinearLayout) ? ((LinearLayout) convertView) : new LinearLayout(context);
        layout.removeAllViews();

        final Item item = getItem(position);
        final String title = item.getTitle();

        final boolean enabled = isEnabled(position);

        if (item.checkable || editDisabledMode) {
          final CheckBox checkBox = new CheckBox(context);
          checkBox.setFocusable(false);
          checkBox.setFocusableInTouchMode(false);
          checkBox.setEnabled(enabled || editDisabledMode);
          checkBox.setClickable(false);
          layout.addView(checkBox);
          checkBox.getLayoutParams().width = Views.px(context, 40);

          if (editDisabledMode) {
            checkBox.setChecked(disabledMap.isEnabled(title));
            checkBox.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                if (disabledMap.isDisabled(title)) {
                  disabledMap.enable(title);
                } else {
                  disabledMap.disable(title);
                }
                editDisabledCallback.run(null);
              }

            });
          } else {
            checkBox.setChecked(item.checked);
          }
      //    checkBox.setPadding(exampleTextView.getPaddingLeft(), checkBox.getPaddingTop(), checkBox.getPaddingRight(), checkBox.getPaddingBottom());
          ((LinearLayout.LayoutParams) checkBox.getLayoutParams()).leftMargin = Views.px(context, 8);
        } else if (item.iconId != -1 || item.iconDrawable != null) {
          ImageView imageView = new ImageView(context);
          imageView.setScaleType(ScaleType.CENTER_INSIDE);
          if (item.iconDrawable != null) {
            imageView.setImageDrawable(item.iconDrawable);
          } else {
            imageView.setImageResource(item.iconId);
          }
          imageView.setFocusable(false);
          imageView.setFocusableInTouchMode(false);
          layout.addView(imageView);
          imageView.getLayoutParams().height = LayoutParams.MATCH_PARENT;

          LinearLayout.LayoutParams imageParams = ((LinearLayout.LayoutParams) imageView.getLayoutParams());
          imageParams.gravity = Gravity.CENTER;
          imageParams.width = Views.px(context, 48);
          imageParams.height = Views.px(context, 48);


        } else if (anyCheckableOrIcon) {
          Space space = new Space(context);
          layout.addView(space);
          space.getLayoutParams().width = Views.px(context, 48);
        }

        TextView textView = (TextView) 
            createViewFromResource(context, ITEM_LAYOUT_ID, parent);
        
        textView.setEnabled(enabled);
        textView.setText(item.title);
        if (item.discouraged) {
          textView.setTextColor(Color.GRAY);
        }
        layout.addView(textView);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
        params.weight = 1;
        
        if (item.help != null && enabled) {
          ImageView  helpButton = new ImageView(context);
          helpButton.setImageResource(R.drawable.ic_info_outline_white_24dp);
          
     //     TextView helpButton = new TextView(context);
          helpButton.setFocusable(false);
          helpButton.setFocusableInTouchMode(false);
       //   helpButton.setTextSize(Dimension.UNIT_PX, tv.getTextSize());
        //  helpButton.setText("  [ ? ]  ");
//          helpButton.setHeight(tv.getHeight());
          layout.addView(helpButton);
          helpButton.getLayoutParams().height = LayoutParams.MATCH_PARENT;

          helpButton.setEnabled(enabled);
          helpButton.setAlpha(0.5f);
          
//        layout.addView(tv);
          helpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              AlertDialog.Builder secondary = new AlertDialog.Builder(context);
              secondary.setTitle(item.title);
              try {
                secondary.setMessage(item.help.call());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
              secondary.setNeutralButton("Ok", null);
              secondary.show();
            }
          });
        }
        return layout;
      }
    };
    
    list.setAdapter(adapter);
    alert.setView(list);
    final AlertDialog dialog = alert.create();
    
    list.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        dialog.dismiss();
        Item item = adapter.getItem(position);
        listener.onContextMenuItemClick(item);
        if (item.subMenu != null) {
          item.propagateDisabledState(item.subMenu);
          item.subMenu.setOnMenuItemClickListener(listener);
          item.subMenu.show();
        }
      }
    });
    
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    Point screenSize = new Point();
    display.getSize(screenSize);
    dialog.show();
    
    WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
    //dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    wmlp.horizontalMargin = 0;
    wmlp.verticalMargin = 0;
    wmlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;

    if (screenSize.x > screenSize.y) {
      wmlp.width = screenSize.x / 3;
    }
    
    if (view == null) {
      wmlp.gravity = Gravity.TOP | Gravity.START;
      wmlp.x = screenSize.x;
      wmlp.y = 100;
    } else {
      int[] location = new int[2];
      view.getLocationInWindow(location);
    
      int viewX = location[0] + view.getWidth() / 2;
      int viewY = location[1] + view.getHeight() / 2;
    
      if (screenSize.x > screenSize.y) {
        if (viewX < screenSize.x / 2) {
          wmlp.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
          wmlp.x = viewX;   //x position
        } else {
          wmlp.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
          wmlp.x = screenSize.x - viewX;   //x position
        }
        wmlp.y = viewY - screenSize.y / 2;
      } else {
        if (viewY < screenSize.y / 2) {
          wmlp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
          wmlp.y = viewY;
        } else {
          wmlp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
          wmlp.y = screenSize.y - viewY;   //x position
        }
        wmlp.x = viewX - screenSize.x / 2;
      }
  //  wmlp.y = 100;   //y position
    }
    dialog.getWindow().setAttributes(wmlp);
  }

  public void setOnMenuItemClickListener(ItemClickListener listener) {
    this.listener = listener;
  }

  public void setHelpProvider(HelpProvider helpProvider) {
    this.helpProvider = helpProvider;
  }

  public Item getItem(int i) {
    return items.get(i);
  }

  public int size() {
    return items.size();
  }

}
