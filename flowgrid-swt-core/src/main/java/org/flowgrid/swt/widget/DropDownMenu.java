package org.flowgrid.swt.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

public class DropDownMenu implements Component {

    final Combo combo;
    Menu menu;
    String selectedText;

    public DropDownMenu(Composite parent) {
        combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
    }

    public void setSelectedText(String text) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItem(i).equals(text)) {
                combo.select(i);
                return;
            }
        }
        combo.add(text, 0);
        combo.select(0);
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
        combo.removeAll();
        for (int i = 0; i < menu.getItemCount(); i++) {
            combo.add(menu.getItem(i).getText());
        }
    }

    public Combo getControl() {
        return combo;
    }

    public void dispose() {
        combo.dispose();
    }
}
