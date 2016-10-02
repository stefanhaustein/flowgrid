package org.flowgrid.android.swt;

import android.os.Bundle;
import android.view.WindowManager;
import org.eclipse.swt.widgets.SwtActivity;
import org.flowgrid.swt.SwtFlowgrid;

import java.io.File;

public class MainActivity extends SwtActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        new SwtFlowgrid(getDisplay(),
                getExternalFilesDir(null)).start();
    }
}
