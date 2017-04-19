package org.flowgrid.android.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Model;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;

import static android.content.Context.SENSOR_SERVICE;

public class SensorSetup extends Callback<Model> {
    private final SensorManager sensorManager;

    public SensorSetup(Context context) {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
    }

    void addSensor(Model model, String name, int sensorType, Type dataType) {
        SensorPortFactory factory = new SensorPortFactory(sensorManager, name, sensorType, dataType);
        model.addPortFactory(factory);
    }

    @Override
    public void run(Model model) {
           final ArrayType vec3 = new ArrayType(PrimitiveType.NUMBER, 3);
        addSensor(model, "Accelerometer", Sensor.TYPE_ACCELEROMETER, vec3);
        addSensor(model, "AmbientTemperature", Sensor.TYPE_AMBIENT_TEMPERATURE, PrimitiveType.NUMBER);
        addSensor(model, "Light", Sensor.TYPE_ACCELEROMETER, PrimitiveType.NUMBER);
        addSensor(model, "Pressure", Sensor.TYPE_ACCELEROMETER, PrimitiveType.NUMBER);
        addSensor(model, "Proximity", Sensor.TYPE_ACCELEROMETER, PrimitiveType.NUMBER);
    }
}
