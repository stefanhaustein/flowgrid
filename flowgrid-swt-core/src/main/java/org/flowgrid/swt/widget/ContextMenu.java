package org.flowgrid.swt.widget;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.flowgrid.model.Callback;
import org.flowgrid.model.DisabledMap;

import java.util.concurrent.Callable;

public class ContextMenu {
    Menu swtMenu;
    ItemClickListener itemClickListener;
    ContextMenu.Item parentItem;

    private boolean editDisabledMode;
    private DisabledMap disabledMap;
    private Callback<Void> editDisabledCallback;

    public ContextMenu(Control control) {
        swtMenu = new Menu(control);
        // control.setMenu(swtMenu);  FIXME
    }

    ContextMenu(Item item) {
        swtMenu = new Menu(item.swtItem);
        parentItem = item;
    }

    public void dispose() {
        swtMenu.dispose();
    }

    public Item add(String label) {
        return new Item(this, label, false);
    }

    public Item addSubMenu(String label) {
        return new Item(this, label, true);
    }

    public void setOnMenuItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setDisabledMap(DisabledMap disabledMap, boolean editDisabledMode, Callback<Void> editDisabledCallback) {
        this.disabledMap = disabledMap;
        this.editDisabledMode = editDisabledMode;
        this.editDisabledCallback = editDisabledCallback;
    }

    public void show() {
        swtMenu.setVisible(true);
    }

    public static class Item implements SelectionListener {
        ContextMenu parentMenu;
        MenuItem swtItem;
        ContextMenu subMenu;

        Item(final ContextMenu parent, final String title, boolean sub) {
            this.parentMenu = parent;
            if (sub) {
                swtItem = new MenuItem(parent.swtMenu, SWT.CASCADE);
                subMenu = new ContextMenu(this);
                propagateDisabledState(subMenu);
            } else {
                swtItem = new MenuItem(parent.swtMenu, SWT.PUSH);
                swtItem.addSelectionListener(Item.this);
            }
            if (parentMenu.disabledMap != null) {
                swtItem.setEnabled(parentMenu.disabledMap.isEnabled(title));
            }
            swtItem.setText(title);
            if (parentMenu.editDisabledMode) {
                final MenuItem disabler = new MenuItem(parent.swtMenu, SWT.CHECK);
                disabler.setText("[^ enabled]");
                disabler.setSelection(parentMenu.disabledMap.isEnabled(title));
                disabler.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (disabler.getSelection()) {
                            parentMenu.disabledMap.disable(title);
                        } else {
                            parentMenu.disabledMap.enable(title);
                        }
                        parentMenu.editDisabledCallback.run(null);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        widgetSelected(e);
                    }
                });

            }
        }

        public ContextMenu getSubMenu() {
            return subMenu;
        }
        public Item getParentItem() {
            return parentMenu == null ? null : parentMenu.parentItem;
        }
        @Override
        public void widgetSelected(SelectionEvent e) {
            ContextMenu.Item current = this;
            do {
                ContextMenu parentMenu = current.parentMenu;
                if (parentMenu == null) {
                    return;
                }
                if (parentMenu.itemClickListener != null) {
                    parentMenu.itemClickListener.onContextMenuItemClick(this);
                    return;
                }
                current = parentMenu.parentItem;
            } while (current != null);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }

        public String getTitle() {
            return swtItem.getText();
        }

        public void propagateDisabledState(ContextMenu target) {
            if (parentMenu.disabledMap != null) {
                target.setDisabledMap(parentMenu.disabledMap.getChild(getTitle()),
                        parentMenu.editDisabledMode, parentMenu.editDisabledCallback);
            }
        }

        public boolean hasSubMenu() {
            return subMenu != null;
        }

        public boolean isChecked() {
            return swtItem.getSelection();
        }

        public void setEnabled(boolean b) {
            swtItem.setEnabled(b);
        }

        public void setHelp(Callable<String> stringCallable) {
            System.out.println("FIXMLE: ContextMenuItem.setHelp()");  // FIXME
        }
    }

    public interface ItemClickListener {
        boolean onContextMenuItemClick(Item item);
    }

}
