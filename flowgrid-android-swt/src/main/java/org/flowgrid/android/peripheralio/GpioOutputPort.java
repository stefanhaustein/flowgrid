package org.flowgrid.android.peripheralio;

import org.flowgrid.model.Port;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class GpioOutputPort implements Port {
    private final PeripheralManagerService peripheralManagerService;
    private final String name;
    Gpio gpio;

    GpioOutputPort(PeripheralManagerService peripheralManagerService, String name) {
        this.peripheralManagerService = peripheralManagerService;
        this.name = name;
    }

    @Override
    public void detach() {
        try {
            if (gpio != null) {
                gpio.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setValue(Object data) {
        try {

            System.out.println("SetValue: " + data);

            if (gpio == null) {
                gpio = peripheralManagerService.openGpio(name);
                gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
                gpio.setActiveType(Gpio.ACTIVE_LOW);
            }
            gpio.setValue((Boolean) data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
    }

    @Override
    public void timerTick(int count) {

    }

    @Override
    public void ping() {

    }
}
