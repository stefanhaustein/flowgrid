package org.flowgrid.android.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Controller;
import org.flowgrid.model.DoubleList;
import org.flowgrid.model.Port;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.api.PortCommand;

public class SensorPort implements Port, SensorEventListener {
    private final Controller controller;
    private final PortCommand port;
    private final SensorManager sensorManager;

    private long lastSent;
    private double lastV0;
    private double lastV1;
    private double lastV2;
    private boolean scalar;
    private float tolerance;
    private boolean running;

    SensorPort(Controller controller, final PortCommand portCommand, final SensorManager sensorManager, int sensorType) {
        this.controller = controller;
        this.port = portCommand;
        this.sensorManager = sensorManager;
/*
        int sensorType = portCommand.peerJson().getInt("sensor", 0);

        this.scalar = type == Sensor.TYPE_AMBIENT_TEMPERATURE ||
                type == Sensor.TYPE_LIGHT || type == Sensor.TYPE_PRESSURE ||
                type == Sensor.TYPE_PROXIMITY;
        port.setDataType(scalar ? PrimitiveType.NUMBER : ArrayType.NUMBER);
        */

        scalar = !(port.dataType() instanceof ArrayType);

        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        tolerance = sensorType == Sensor.TYPE_LIGHT ? 0.5f : 0.1f;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long t = System.currentTimeMillis();
        if (t - lastSent < 100 || !controller.isRunning()) {
            return;
        }
        lastSent = t;

        if (scalar) {
            if (Math.abs((lastV0 - event.values[0]) / lastV0) > tolerance) {
                lastV0 = event.values[0];
                if (running) {
                    sendData();
                }
            }
        } else {
            if (Math.abs(lastV0 - event.values[0]) > tolerance ||
                    Math.abs(lastV1 - event.values[1]) > tolerance ||
                    Math.abs(lastV2 - event.values[2]) > tolerance) {
                lastV0 = event.values[0];
                lastV1 = event.values[1];
                lastV2 = event.values[2];
                if (running) {
                    sendData();
                }
            }
        }
    }

    @Override
    public void detach() {
        Log.d("FlowGrid", "Deleting Sensor Connection");
        sensorManager.unregisterListener(this);
    }

    @Override
    public void setValue(Object data) {
        // Not needed.
    }

    private void sendData() {
        if (scalar) {
            port.sendData(controller.rootEnvironment, lastV0, 0);
        } else {
            DoubleList v = new DoubleList();
            v.add(lastV0);
            v.add(lastV1);
            v.add(lastV2);
            port.sendData(controller.rootEnvironment, v, 0);
        }
    }

    public void stop() {
        running = false;
    }

    public void start() {
        running = true;
    }

    @Override
    public void timerTick(int count) {

    }

    @Override
    public void ping() {
        sendData();
    }

}
