package org.flowgrid.android.sensor;

import android.hardware.SensorManager;
import org.flowgrid.model.Controller;
import org.flowgrid.model.Port;
import org.flowgrid.model.PortFactory;
import org.flowgrid.model.Type;
import org.flowgrid.model.api.PortCommand;

public class SensorPortFactory implements PortFactory {

    private final SensorManager sensorManager;
    private final String name;
    private final int sensorType;
    private final Type dataType;

    public SensorPortFactory(SensorManager sensorManager, String name, int sensorType, Type dataType) {
        this.sensorManager = sensorManager;
        this.name = name;
        this.sensorType = sensorType;
        this.dataType = dataType;
    }

    public String getPortType() {
        return "Sensor/" + name;
    }

    @Override
    public Port create(Controller controller, PortCommand portCommand) {
        return new SensorPort(controller, portCommand, sensorManager, sensorType);
    }

    @Override
    public boolean hasInput() {
        return false;
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public Type getDataType() {
        return dataType;
    }

    @Override
    public Option[] getOptions() {
        return null;
    }

}
