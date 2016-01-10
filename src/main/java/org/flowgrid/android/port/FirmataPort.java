package org.flowgrid.android.port;

import org.flowgrid.model.Port;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.api.PortCommand;
import org.shokai.firmata.ArduinoFirmata;

import android.util.Log;

public class FirmataPort implements Port {
  private static final String TAG = "FirmataPort";
  
  public enum Mode {
    ANALOG, DIGITAL, SERVO
  }
  
  private PortManager manager;
  private PortCommand portCommand;
  private int pin;
  private Mode mode;
  private Object value;
  private boolean running;
  
  public FirmataPort(PortManager manager, PortCommand portCommand) {
    this.manager = manager;
    this.portCommand = portCommand;
    pin = portCommand.peerJson().getInt("pin", 0);
    mode = Mode.DIGITAL;
    String modeStr = portCommand.peerJson().getString("mode", Mode.DIGITAL.toString());
    try {
      mode = Mode.valueOf(modeStr);
    } catch(IllegalArgumentException e) {
      Log.e(TAG, "Unrecognized Firmata mode " + modeStr, e);
    }
    portCommand.setDataType(mode == Mode.DIGITAL ? PrimitiveType.BOOLEAN : PrimitiveType.NUMBER);
  }

  @Override
  public void detach() {
    stop();
  }

  @Override
  public void setValue(Object data) {
    this.value = data;
    if (portCommand.output) {
      ArduinoFirmata firmata = manager.platform().arduinoFirmata();
      switch(mode) {
      case DIGITAL: {
        boolean b = Boolean.TRUE.equals(value);
        firmata.digitalWrite(pin, b);
        break;
      }
      case ANALOG: {
        Number n = (Number) value;
        firmata.analogWrite(pin, n.intValue()); 
        break;
      }
      case SERVO: {
        Number n = (Number) value;
        firmata.servoWrite(pin, n.intValue()); 
        break;
      }
      default:
        Log.d(TAG, "Invalid output mode " + mode);
      }
    }
  }
  
  void sendValue(Object value) {
    if (value.equals(this.value)) {
      return;
    }
    this.value = value;
    portCommand.sendData(manager.controller().rootEnvironment, value, 0);
  }

  public synchronized void timerTick(int count) {
    if (running && value != null && portCommand.input) {
      ArduinoFirmata firmata = manager.platform().arduinoFirmata();
      switch(mode) {
      case DIGITAL:
        sendValue(firmata.digitalRead(pin));
        break;
      case ANALOG:
        sendValue(firmata.analogRead(pin));
        break;
      default: 
        Log.d(TAG, "Invalig input mode " + mode);
      }
    }
  }
  
  @Override
  public void start() {
    if (running) {
      Log.e(TAG, "start() called, but aleady running.", new Exception());
      return;
    }
  }

  @Override
  public void stop() {
    running = false;
  }

  @Override
  public synchronized void ping() {
    start();
    timerTick(0);
    stop();
  }

}
