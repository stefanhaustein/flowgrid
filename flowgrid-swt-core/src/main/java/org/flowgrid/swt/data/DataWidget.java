package org.flowgrid.swt.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Member;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.StructuredData;
import org.flowgrid.model.Type;
import org.flowgrid.model.Types;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.widget.Widget;

public class DataWidget implements Widget {

    public interface OnValueChangedListener {
        void onValueChanged(Object newValue);
    }

    static String toString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    final SwtFlowgrid flowgrid;
    final Member owner;
    final boolean editable;
    final String[] path;
    final StructuredData parent;
    private final Type type;
    private final String name;
    private OnValueChangedListener onValueChangedListener;

    private Text text;
    private Label label;
    private Button button;

    // We keep a copy here because in some cases (e.g. adding literals), the value holder
    // needs to be created explicitly before it's possible to read the value back
    private Object value;

    private Control control;
    private Composite parentComposite;

    public DataWidget(final SwtFlowgrid platform, Member owner, String... path) {
        this(platform, owner, null, "", true, path);
    }

    public DataWidget(final SwtFlowgrid flowgrid, final Member owner, Type forceType, String widgetType,
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
    public Control getControl() {
        return control;
    }

    @Override
    public Control createControl(Composite parentComposite) {
        if (control != null) {
            throw new IllegalStateException("Control created already.");
        }

        if (editable) {
            if (type == PrimitiveType.BOOLEAN) {
                button = new Button(parentComposite, SWT.CHECK);
                button.setText(name);
                button.setSelection(Boolean.TRUE.equals(value));
                button.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        inputChangedTo(button.getSelection(), false);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        widgetSelected(e);
                    }
                });
                control = button;
                return control;
            }
            if (type == PrimitiveType.NUMBER || type == PrimitiveType.TEXT) {
                Composite container = new Composite(parentComposite, SWT.NONE);
                Label label = new Label(container, SWT.NONE);
                label.setText(name);
                text = new Text(container, SWT.NONE);
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
                        } catch (Exception ex) {
                        }
                    }
                });
                GridLayout gridLayout = new GridLayout(1, false);
                gridLayout.marginWidth = 0;
                gridLayout.marginHeight = 0;
                container.setLayout(gridLayout);
                text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
                control = container;
                control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
                return control;
            }
        }

        Composite container = new Composite(parentComposite, SWT.NONE);
        Label label = new Label(container, SWT.NONE);
        label.setText(name);
        this.label = new Label(container, SWT.NONE);
        this.label.setText(toString(value));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        container.setLayout(gridLayout);
        this.label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        control = container;
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
            if (text != null) {
                text.setText(toString(value));
            } else if (button != null) {
                button.setSelection(Boolean.TRUE.equals(value));
            } else if (label != null) {
                label.setText((toString(value)));
            }
        }
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }
}