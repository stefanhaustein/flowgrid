package org.flowgrid.swt.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class MenuAdapter implements SelectionListener {

    private final MenuSelectionHandler handler;

    public MenuAdapter(MenuSelectionHandler handler) {
        this.handler = handler;
    }

    public void addItem(Menu menu, String label) {
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(label);
        item.addSelectionListener(this);
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
