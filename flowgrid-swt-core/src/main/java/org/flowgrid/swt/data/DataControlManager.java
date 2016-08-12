package org.flowgrid.swt.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Member;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.StructuredData;
import org.flowgrid.model.Type;
import org.flowgrid.model.Types;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.widget.ControlManager;

public class DataControlManager implements ControlManager {

    public interface OnValueChangedListener {
        void onValueChanged(Object newValue);
    }

    final SwtFlowgrid flowgrid;
    final Member owner;
    final boolean editable;
    final String[] path;
    final StructuredData parent;
    private final Type type;
    private final String name;
    private OnValueChangedListener onValueChangedListener;

    // We keep a copy here because in some cases (e.g. adding literals), the value holder
    // needs to be created explicitly before it's possible to read the value back
    private Object value;

    private Control control;

    public DataControlManager(final SwtFlowgrid platform, Member owner, String... path) {
        this(platform, owner, null, "", true, path);
    }

    public DataControlManager(final SwtFlowgrid flowgrid, final Member owner, Type forceType, String widgetType,
                              final boolean editable, final String... path) {
        this.flowgrid = flowgrid;

        parent = owner.structuredData(path);
        name = path[path.length - 1];

        //Data data = owner.data(path);
        type = forceType != null ? forceType : parent.type(name);
        this.editable = editable && Types.hasInstantiableImplementation(type);
        this.owner = owner;
        this.path = path;
    }


    public Object value() {
        return value;
    }

    public Type type() {
        return type;
    }

    @Override
    public Control createControl(Composite parent) {
        if (control != null) {
            throw new IllegalStateException(
                    "control created already. Call disposeControl() before creating a new one.");
        }
        final Text text = new Text(parent, SWT.NONE);
        text.addModifyListenr(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent eve) {
                try {
                    String textValue = text.getText();
                    final Object newValue = type == PrimitiveType.NUMBER ?
                            Double.parseDouble(textValue) : textValue;
                    if (!newValue.equals(value)) {
                        inputChangedTo(newValue, true);
                    }
                } catch(Exception ex) {
                }
            }
        });
        control = text;
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return control;
    }

    @Override
    public void disposeControl() {
        control.dispose();
        control = null;
    }

    /**
     * Called from UI widgets after changes.
     */
    protected void inputChangedTo(Object newValue, boolean delayNotification) {
        value = newValue;
        parent.set(name, newValue);
/*
        if (delayNotification) {
            if (sendTask != null) {
                sendTask.cancel();
            }

            sendTask = new UiTimerTask(platform) {
                @Override
                public void runOnUiThread() {
                    sendTask = null;
                    owner.saveData();
                    if (onValueChangedListener != null) {
                        onValueChangedListener.onValueChanged(value);
                    }
                }
            };
            if (timer == null) {
                timer = new Timer();
            }
            timer.schedule(sendTask, 1000);
        } else */ {
            owner.saveData();
            if (onValueChangedListener != null) {
                onValueChangedListener.onValueChanged(newValue);
            }
        }
    }


    public void setValue(Object newValue) {
        if (!newValue.equals(value)) {
            value = newValue;
            if (control != null) {
                ((Text) control).setText(String.valueOf(value));
            }
        }
    }
}