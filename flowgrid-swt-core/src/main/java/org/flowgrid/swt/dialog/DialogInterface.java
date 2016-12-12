package org.flowgrid.swt.dialog;

/**
 * Created by haustein on 11.09.16.
 */
public interface DialogInterface {

    int BITTON_NEGATIVE = -2;
    int BUTTON_NEUTRAL = -3;
    int BUTTON_POSITIVE = -1;

    interface OnCancelListener {
        void onCancel(DialogInterface dialog);
    }

    interface OnClickListener {
        void onClick(DialogInterface dialog, int which);
    }
}
