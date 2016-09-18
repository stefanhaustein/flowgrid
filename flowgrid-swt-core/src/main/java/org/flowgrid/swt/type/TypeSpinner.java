package org.flowgrid.swt.type;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Container;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.swt.SwtFlowgrid;


public class TypeSpinner extends Button implements TypeWidget {
        private Type type = PrimitiveType.NUMBER;
        private TypeWidget.OnTypeChangedListener listener = null;

        public TypeSpinner(Composite parent, final SwtFlowgrid platform, final Container localModule,
                           final Type assignableTo, final TypeFilter filter) {
            super(parent, SWT.PUSH);
            setText(type.name());
//            Views.applyEditTextStyle(this, true);
            addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    new TypeMenu(platform, TypeSpinner.this, localModule, assignableTo, filter, new Callback<Type>() {
                        @Override
                        public void run(Type type) {
                            setType(type);
                        }
                    }).show();
                }
            });
        }


        public void setType(Type type) {
            if (type != this.type) {
                this.type = type;
                setText(type.name());
                if (listener != null) {
                    listener.onTypeChanged(type);
                }
            }
        }

        public Type type() {
            return type;
        }


        public void setOnTypeChangedListener(TypeWidget.OnTypeChangedListener onTypeChangedListener) {
            this.listener = onTypeChangedListener;
        }


}
