package org.flowgrid.model;

import org.flowgrid.model.api.PortCommand;


public interface PortFactory {

    String getPortType();

    Port create(Controller controller, PortCommand portCommand);

    boolean hasInput();

    boolean hasOutput();

    Type getDataType();

    Option[] getOptions();


    class Option {
        public final String name;
        public final Type type;
        public final Object[] values;

        public Option(String name, Type type, Object... values) {
            this.name = name;
            this.type = type;
            this.values = values;
        }
    }

}
