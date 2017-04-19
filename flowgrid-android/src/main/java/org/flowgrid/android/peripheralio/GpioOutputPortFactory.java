package org.flowgrid.android.peripheralio;


import com.google.android.things.pio.PeripheralManagerService;
import org.flowgrid.model.Controller;
import org.flowgrid.model.Port;
import org.flowgrid.model.PortFactory;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.api.PortCommand;

import java.util.List;

public class GpioOutputPortFactory implements PortFactory {

    final PeripheralManagerService peripheralManagerService;

    GpioOutputPortFactory(PeripheralManagerService peripheralManagerService) {
        this.peripheralManagerService = peripheralManagerService;
    }

    @Override
    public String getPortType() {
        return "Peripheral IO/GPIO Output";
    }

    @Override
    public Port create(Controller controller, PortCommand portCommand) {
        return new GpioOutputPort(peripheralManagerService, portCommand.peerJson().getString("Pin"));
    }

    @Override
    public boolean hasInput() {
        return true;
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public Type getDataType() {
        return PrimitiveType.BOOLEAN;
    }

    @Override
    public Option[] getOptions() {
        List<String> gpioPins = peripheralManagerService.getGpioList();

        Option pins = new Option("Pin", PrimitiveType.TEXT, gpioPins.toArray(new String[gpioPins.size()]));

        return new Option[] {pins};
    }
}
