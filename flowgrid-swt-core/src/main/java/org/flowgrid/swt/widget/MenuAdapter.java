package org.flowgrid.swt.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class MenuAdapter implements SelectionListener {

    private final MenuSelectionHandler handler;
    private final Menu menu;

    public MenuAdapter(Menu parent, String name, MenuSelectionHandler handler) {
        MenuItem menuItem = new MenuItem(parent, SWT.CASCADE);
        menuItem.setText(name);
        menu = new Menu(menuItem);
        this.handler = handler;
    }


    public MenuItem addItem(String label) {
        return addItem(label, SWT.PUSH);
    }

    public MenuItem addItem(String label, int style) {
        MenuItem item = new MenuItem(menu, style);
        item.setText(label);
        item.addSelectionListener(this);
        return item;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        handler.menuItemSelected((MenuItem) e.widget);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        handler.menuItemSelected((MenuItem) e.widget);
    }
}
