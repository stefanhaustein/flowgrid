package org.flowgrid.swt.type;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;

public class PrimitiveTypeSpinner extends Combo implements TypeWidget {

        TypeWidget.OnTypeChangedListener listener;

        public PrimitiveTypeSpinner(Composite parent) {
            super(parent, SWT.READ_ONLY);
            for (PrimitiveType type : PrimitiveType.ALL) {
                add(type.name());
            };


/*            this.setOnItemSelectedListener(new OnItemSelectedListener() {                FIXME
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    setType(PrimitiveType.ALL[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            */
        }

        @Override
        public PrimitiveType type() {
            return PrimitiveType.ALL[getSelectionIndex()];
        }

        @Override
        public void setType(Type type) {
            if (type == type()) {
                return;
            }
            for (int i = 0; i < PrimitiveType.ALL.length; i++) {
                if (PrimitiveType.ALL[i].equals(type)) {
                    select(i);
                    break;
                }
            }
            if (listener != null) {
                listener.onTypeChanged(type);
            }
        }

        @Override
        public void setOnTypeChangedListener(TypeWidget.OnTypeChangedListener listener) {
            System.out.println("FIXME: PrimitiveTypeSpinner listener support");
            this.listener = listener;
        }

}
