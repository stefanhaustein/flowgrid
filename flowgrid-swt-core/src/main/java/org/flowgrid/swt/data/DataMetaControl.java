package org.flowgrid.swt.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.flowgrid.model.Module;
import org.flowgrid.model.Objects;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.swt.SwtFlowgrid;
import org.flowgrid.swt.type.TypeFilter;
import org.flowgrid.swt.type.TypeSpinner;
import org.flowgrid.swt.type.TypeWidget;
import org.flowgrid.swt.widget.MetaControl;

public class DataMetaControl implements MetaControl {

    public interface OnValueChangedListener {
        void onValueChanged(Object newValue);
    }

    static String toString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    boolean editable = true;
    private Type type;
    private String name;
    private OnValueChangedListener onValueChangedListener;

    private Text text;
    private Label label;
    private Button button;
    private Scale scale;

    // We keep a copy here because in some cases (e.g. adding literals), the value holder
    // needs to be created explicitly before it's possible to read the value back
    private Object value;
    private Control control;
    private String widget;
    private SwtFlowgrid flowgrid;
    private DataMetaControl inner;
    private Module localModule;

    public DataMetaControl(SwtFlowgrid flowgrid, Type type) {
        this.flowgrid = flowgrid;
        this.type = type;
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

    private Composite maybeAddLabel(Composite parentComposite) {
        if (name == null || name.isEmpty()) {
            return parentComposite;
        }
        Composite container = new Composite(parentComposite, SWT.NONE);
        Label label = new Label(container, SWT.NONE);
        label.setText(name);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        container.setLayout(gridLayout);
        control = container;
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return container;
    }

    private Control setControl(Control control) {
        if (this.control == null) {
            this.control = control;
        }
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
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
                return setControl(button);
            }
            if (type == PrimitiveType.NUMBER && "slider".equals(widget)) {
                setControl(scale = new Scale(maybeAddLabel(parentComposite), SWT.NONE));
                scale.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Double newValue = (double) scale.getSelection();
                        if (!newValue.equals(value)){
                            inputChangedTo((double) scale.getSelection(), false);
                        }
                    }
                });

                return control;
            }

            if (type == PrimitiveType.NUMBER || type == PrimitiveType.TEXT) {
                System.out.println("*** MetaControl for " + name + ": " + widget);
                setControl(text = new Text(maybeAddLabel(parentComposite), SWT.NONE));
                text.addModifyListener(new ModifyListener() {
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
                return control;
            }

            if (type == Type.ANY) {
                final Composite container = new Composite(parentComposite, SWT.NONE);
                GridLayout gridLayout = new GridLayout(1, false);
                gridLayout.marginWidth = 0;
                gridLayout.marginHeight = 0;
                container.setLayout(gridLayout);
                if (name != null && !name.isEmpty()) {
                    Label label = new Label(container, SWT.NONE);
                    label.setText(name);
                }
                control = container;
                container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

                TypeSpinner typeSpinner = new TypeSpinner(container, flowgrid, localModule, Type.ANY, TypeFilter.INSTANTIABLE);
                inner = new DataMetaControl(flowgrid, typeSpinner.type());
                inner.createControl(container);
                inner.setOnValueChangedListener(new OnValueChangedListener() {
                    @Override
                    public void onValueChanged(Object newValue) {
                        inputChangedTo(newValue, false);
                    }
                });
                typeSpinner.setOnTypeChangedListener(new TypeWidget.OnTypeChangedListener() {
                    @Override
                    public void onTypeChanged(Type type) {
                        inner.disposeControl();
                        inner.type = type;
                        inner.createControl(container);
                    }
                });

                return container;
            }
        }

        setControl(label = new Label(maybeAddLabel(parentComposite), SWT.NONE));
        return control;
    }

    @Override
    public void disposeControl() {
        if (control != null) {
            control.dispose();
            control = null;
        }
    }

    /**
     * Called from UI widgets after changes.
     */
    protected void inputChangedTo(Object newValue, boolean delayNotification) {
        value = newValue;
        // parent.set(name, newValue);
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
            // owner.saveData();
            if (onValueChangedListener != null) {
                onValueChangedListener.onValueChanged(newValue);
            }
        }
    }

    public DataMetaControl setLabel(String label) {
        this.name = label;
        return this;
    }

    public DataMetaControl setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    public void setValue(Object newValue) {
        if (!Objects.equals(newValue, value)) {
            value = newValue;
            if (text != null) {
                text.setText(toString(value));
            } else if (button != null) {
                button.setSelection(Boolean.TRUE.equals(value));
            } else if (label != null) {
                label.setText((toString(value)));
            } else if (scale != null) {
                scale.setSelection(Math.round(((Number) value).floatValue()));
            }
        }
    }

    public void setLocalModule(Module localModule) {
        this.localModule = localModule;
    }

    public void setWidget(String widget) {
        this.widget = widget;
    }


}