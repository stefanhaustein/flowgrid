package org.flowgrid.android.swt;

import android.os.Bundle;
import android.util.TypedValue;
import org.eclipse.swt.widgets.SwtActivity;
import org.flowgrid.android.sensor.SensorSetup;
import org.flowgrid.android.peripheralio.PeripheralIoSetup;
import org.flowgrid.swt.SwtFlowgrid;

public class MainActivity extends SwtActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        float pixelPerDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());



        new SwtFlowgrid(getDisplay(), getExternalFilesDir(null), true, pixelPerDp).start(
                new SensorSetup(this),
                new PeripheralIoSetup());
    }
}
