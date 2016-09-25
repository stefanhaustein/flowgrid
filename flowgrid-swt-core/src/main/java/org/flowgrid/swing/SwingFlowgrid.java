package org.flowgrid.swing;

import org.eclipse.swt.widgets.SwingDisplay;
import org.flowgrid.swt.SwtFlowgrid;

import javax.swing.*;

public class SwingFlowgrid {

    public static void main(String[] args) {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }

        SwingDisplay display = new SwingDisplay();
        new SwtFlowgrid(display);
    }

}