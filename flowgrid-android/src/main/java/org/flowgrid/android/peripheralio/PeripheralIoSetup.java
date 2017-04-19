package org.flowgrid.android.peripheralio;

import org.flowgrid.model.Callback;
import org.flowgrid.model.Model;

import com.google.android.things.pio.PeripheralManagerService;

public class PeripheralIoSetup extends Callback<Model> {
    PeripheralManagerService peripheralManagerService;

    public PeripheralIoSetup() {
        try {
            peripheralManagerService = new PeripheralManagerService();
        } catch (NoClassDefFoundError e) {
        }
    }

    @Override
    public void run(Model model) {
        if (peripheralManagerService == null) {
            return;
        }
        model.addPortFactory(new GpioOutputPortFactory(peripheralManagerService));
    }
}
