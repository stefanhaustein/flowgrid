package org.flowgrid.swt.api;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Controller;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Instance;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.annotation.FgType;
import org.flowgrid.swt.SwtFlowgrid;

import java.util.ArrayList;

public class CanvasControl extends Canvas {

    private Controller controller;
    private ArrayList<Instance> objects = new ArrayList<Instance>();
    //private android.graphics.Paint fillPaint = new android.graphics.Paint();
    //private android.graphics.Paint borderPaint = new android.graphics.Paint();
    private float pixelPerDp;
    private Image background;
    private int widthPx = 1000;
    private int heightPx = 1000;
    private int pointerId = -1;
    private Instance touchedInstance;
    private double lastX;
    private double lastY;
    private int sizePx = 1000;
    //private RectF rectF = new RectF();
    //private Paint bitmapPaint = new Paint();
    private boolean invalidated;
    SwtFlowgrid flowgrid;


    public CanvasControl(Composite parent, SwtFlowgrid platform, Controller controller) {
        super(parent, SWT.DEFAULT);
        this.controller = controller;
        this.flowgrid = platform;
        pixelPerDp = platform.resourceManager.pixelPerDp;


        this.addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent e) {

            }

            @Override
            public void controlResized(ControlEvent e) {
                System.out.println("*********** canvas resized ********");
                Rectangle bounds = getBounds();
                sizeChanged(bounds.width, bounds.height);
            }
        });


        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                pointerId = e.button;
                onTouchActionDown(normalizedX(e.x), normalizedY(e.y));
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (pointerId != -1 && touchedInstance != null) {
                    pointerId = -1;
                    touchedInstance = null;
                }
            }
        });

        this.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if (pointerId != -1 && touchedInstance != null) {
                    //final int pointerIndex = e.button;
                    //if (pointerIndex != -1) {
                        onTouchActionMove(normalizedX(e.x),
                                normalizedY(e.y));
                }
            }
        });
    }

    public synchronized void setOnClickListener(final @FgType("/graphics/OnClick") Instance object) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                Artifact onClick = object.classifier.artifact("onClick");
                if (onClick instanceof CustomOperation) {
                    controller.invoke((CustomOperation) onClick, object, null /* resultCallback */, this);
                }
            }
        });
    }

    public synchronized void add(@FgType("/graphics/sprite/Placeable") Instance object) {
        objects.add(object);
        postInvalidate();

        Artifact onAttach = object.classifier.artifact("onAttach");
        if (onAttach instanceof CustomOperation) {
            controller.invoke((CustomOperation) onAttach, object, null /* resultCallback */, this);
        }
    }

    public synchronized void remove(@FgType("/graphics/sprite/Placeable") Instance object) {
        objects.remove(object);
        postInvalidate();
    }

    public synchronized void removeAll() {
        objects.clear();
    }

    public Graphics createGraphics() {
        return new Graphics(this);
    }

    int pixelSize(double normalizedSize) { return Math.round((float) (normalizedSize * sizePx / 1000)); }
    int pixelX(double normalizedX) { return Math.round((float) (normalizedX * sizePx / 1000) + widthPx / 2); }
    int pixelY(double normalizedY) { return Math.round((float) (-normalizedY * sizePx / 1000) + heightPx / 2); }


    double normalizedX(float screenX) {
        return (screenX - widthPx / 2) * 1000 / sizePx;
    }

    double normalizedY(float screenY) {
        return -(screenY - heightPx / 2) * 1000 / sizePx;
    }


    private synchronized boolean onTouchActionDown(double x, double y) {
        touchedInstance = null;
        double bestDistanceSquared = 9e9;
        for (Instance sprite: objects) {
            double dx = x - sprite.getNumber("x");
            double dy = y - sprite.getNumber("y");
            double distanceSquared = dx * dx + dy * dy;
            if (distanceSquared < bestDistanceSquared) {
                bestDistanceSquared = distanceSquared;
                touchedInstance = sprite;
            }
        }
        if (touchedInstance == null) {
            return false;
        }
        lastX = x;
        lastY = y;
        onTouchActionMove(x, y);
        return true;
    }

    private synchronized void onTouchActionMove(double x, double y) {
        Artifact m = touchedInstance.classifier.artifact("onDrag");
        if (m instanceof CustomOperation) {
            controller.invoke((CustomOperation) m, touchedInstance, null, x - lastX, y - lastY);
        }
        lastX = x;
        lastY = y;
    }

/*
    @Override
    public synchronized boolean onTouchEvent(MotionEvent ev) {
        final int action =  ev.getActionMasked(); //ev.getAction();
        boolean consumed = false;

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                pointerId = ev.getPointerId(0);
                consumed = onTouchActionDown(normalizedX(ev.getX()), normalizedY(ev.getY()));
                break;

            case MotionEvent.ACTION_MOVE:
                if (pointerId != -1 && touchedInstance != null) {
                    final int pointerIndex = ev.findPointerIndex(pointerId);
                    if (pointerIndex != -1) {
                        onTouchActionMove(normalizedX(ev.getX(pointerIndex)),
                                normalizedY(ev.getY(pointerIndex)));
                        consumed = true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (pointerId != -1 && touchedInstance != null) {
                    pointerId = -1;
                    touchedInstance = null;
                    consumed = true;
                }
                break;
        }
        return consumed ? true : super.onTouchEvent(ev);
    }*/

    private static double getWidth(Instance placeable) {
        if (placeable.classifier().hasProperty("radius", PrimitiveType.NUMBER)) {
            return placeable.getNumber("radius") * 2;
        }
        Object imageObject = placeable.get("image");
        if (imageObject instanceof ImageImpl) {
            ImageImpl image = (ImageImpl) imageObject;
            return image.width();
        }
        return placeable.getNumber("width");
    }

    private static double getHeight(Instance placeable) {
        if (placeable.classifier().hasProperty("radius", PrimitiveType.NUMBER)) {
            return placeable.getNumber("radius") * 2;
        }
        Object imageObject = placeable.get("image");
        if (imageObject instanceof ImageImpl) {
            ImageImpl image = (ImageImpl) imageObject;
            return image.height();
        }
        return placeable.getNumber("height");
    }

    private void drawObject(GC gc, Instance instance) {
        double x = instance.getNumber("x");
        double y = instance.getNumber("y");
        Object colorObject = instance.get("color");

        gc.setBackground(flowgrid.resourceManager.getColor(colorObject instanceof org.flowgrid.model.api.Color
                ? ((org.flowgrid.model.api.Color) colorObject).argb() : 0xffffffff));
        Object imageObject = instance.get("image");
        Object textObject = instance.get("text");
        if (imageObject instanceof ImageImpl) {
            ImageImpl image = (ImageImpl) imageObject;
            int sw = pixelSize(image.width());
            int sh = pixelSize(image.height());
            int sx = pixelX(x);
            int sy = pixelY(y);

            //  canvas.drawBitmap(image.bitmap(), dst.left, dst.top, null);
            gc.drawImage(image.bitmap(), 0, 0, image.width(), image.height(), sx - sw / 2, sy - sh / 2, sw, sh);
        } else if (textObject instanceof String) {
            gc.setFont(flowgrid.resourceManager.getFont((int) (100 * sizePx / 1000), 0));
            int start = 0;
            String text = (String) textObject;
            while (start < text.length()) {
                int end = text.indexOf('\n', start);
                if (end == -1) {
                    end = text.length();
                }
                gc.drawString(text.substring(start, end), Math.round(pixelX(x)), Math.round(pixelY(y)));
                start = end + 1;
                y -= 100;
            }
        } else if (instance.classifier().hasProperty("radius", PrimitiveType.NUMBER)) {
            double radius = instance.getNumber("radius");
            int sr = Math.round((float) radius * sizePx / 1000);
            int sx = Math.round(pixelX(x));
            int sy = Math.round(pixelY(y));
            gc.fillOval(sx - sr, sy - sr, sr * 2, sr * 2);
        } else {
            int sx = Math.round(pixelX(x));
            int sy = Math.round(pixelY(y));
            int sw = Math.round(pixelSize(instance.getNumber("width")));
            int sh = Math.round(pixelSize(instance.getNumber("height")));
            gc.fillRectangle(sx - sw / 2, sy - sh / 2, sw, sh);
        }
    }


    @Override
    public void drawBackground(GC gc, int clipX, int clipY, int clipW, int clipH) {
        invalidated = false;
        if (background != null) {
            gc.drawImage(background, 0, 0);
        } else {
            gc.setBackground(flowgrid.resourceManager.black);
            gc.fillRectangle(0, 0, widthPx, heightPx);
        }

        for (Instance instance: objects) {
            try {
                drawObject(gc, instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int min = (int) (48 * pixelPerDp);
        int w = resolveSize(min, widthMeasureSpec);
        int h = resolveSize(min, heightMeasureSpec);
        setMeasuredDimension(w, h);
    }
    */


    public synchronized Image getBitmap() {
        if (background == null) {
            background = new Image(getDisplay(), widthPx, heightPx);
            GC gc = new GC(background);
            gc.setBackground(flowgrid.resourceManager.black);
            gc.fillRectangle(0, 0, widthPx, heightPx);
            gc.dispose();
        }
        return background;
    }


    void checkCollision(Instance sprite, CustomOperation onCollision) {
        double width = getWidth(sprite);
        double height = getHeight(sprite);
        double x = sprite.getNumber("x");
        double y = sprite.getNumber("y");
        double xMin = x - width / 2;
        double xMax = x + width / 2;
        double yMin = y - height / 2;
        double yMax = y + height / 2;

        for (int i = 0; i < objects.size(); i++) {
            Instance candidate = objects.get(i);
            if (candidate != sprite) {
                double width2 = getWidth(candidate);
                double height2 = getHeight(candidate);
                double x2 = candidate.getNumber("x");
                double y2 = candidate.getNumber("y");
                double xMin2 = x2 - width2 / 2;
                double xMax2 = x2 + width2 / 2;
                double yMin2 = y2 - height2 / 2;
                double yMax2 = y2 + height2 / 2;
                if (xMax >= xMin2 && xMin <= xMax2 &&
                        yMax >= yMin2 && yMin <= yMax2) {
                    controller.invoke(onCollision, sprite, null, candidate);
                }
            }
        }
    }

    public synchronized void timerTick(int frames) {
        if (objects.size() > 0) {
            for (int i = objects.size() - 1; i >= 0; i--) {
                Instance sprite = objects.get(i);
                Artifact onCollision = sprite.classifier.artifact("onCollision");
                if (onCollision instanceof CustomOperation) {
                    checkCollision(sprite, (CustomOperation) onCollision);
                }

                Artifact tick = sprite.classifier.artifact("onTick");
                if (tick instanceof CustomOperation) {
                    controller.invoke((CustomOperation) tick, sprite, null, frames/60.0);
                }
            }
            postInvalidate();
        }
    }



    synchronized void sizeChanged(int w, int h) {

        System.out.println("Size changed to: " + w + "x" + h);
        int oldW = widthPx;
        int oldH = heightPx;
        widthPx = w;
        heightPx = h;
        sizePx = Math.min(w, h);
        if (background != null) {
            Image oldBackground = background;
            Rectangle bounds = oldBackground.getBounds();
            int oldBmW = bounds.width;
            int oldBmH = bounds.height;
            background = null;
            GC gc = new GC(getBitmap());

            int newMin2 = sizePx / 2;
            int x0 = w / 2 - newMin2;
            int y0 = h / 2 - newMin2;
            int width = sizePx;
            int height = sizePx;
                    // rectF.set(w / 2 - newMin2, h / 2 - newMin2,
                            // w / 2 + newMin2, h / 2 + newMin2);
            if (oldBmW > oldBmH) {
                float scale = oldBmW / oldBmH;
                x0 = Math.round(w / 2 - newMin2 * scale);
                width = Math.round(width * scale);
            } else if (oldH > oldW){
                float scale = oldBmH / oldBmW;
                y0 = Math.round(h/2 - newMin2 * scale);
                height = Math.round(newMin2 * scale);
            }
            gc.drawImage(oldBackground, 0, 0, oldBmW, oldBmH, x0, y0, width, height);
            gc.dispose();
        }
    }


    public void postInvalidate() {
        if (!invalidated) {
            invalidated = true;
            getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    redraw();
                }
            });
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        if (wHint == SWT.DEFAULT) {
            return hHint == SWT.DEFAULT ? new Point(128, 128) : new Point(hHint, hHint);
        }
        return new Point(wHint, hHint == SWT.DEFAULT ? wHint : hHint);
    }


    public String toString() {
        return "Canvas#" + (hashCode() % 1000);
    }

}
