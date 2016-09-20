package org.flowgrid.swt.api;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
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


    public CanvasControl(Composite parent, SwtFlowgrid platform, Controller controller) {
        super(parent, SWT.DEFAULT);
        this.controller = controller;
//        pixelPerDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        pixelPerDp = platform.dpToPx(1);

        /*
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);

        fillPaint.setStyle(Style.FILL);
        fillPaint.setColor(Color.LTGRAY);
        fillPaint.setFilterBitmap(true);
        fillPaint.setAntiAlias(true);
        fillPaint.setDither(true);

        borderPaint.setStyle(Style.STROKE);
        borderPaint.setColor(Color.DKGRAY);
        borderPaint.setAntiAlias(true); */

        this.addControlListener(new ControlListener() {
            @Override
            public void controlMoved(ControlEvent e) {

            }

            @Override
            public void controlResized(ControlEvent e) {
                Rectangle bounds = getBounds();
                sizeChanged(bounds.width, bounds.height);
            }
        });

    }

    /*
    public synchronized void setOnClickListener(final @FgType("/graphics/OnClick") Instance object) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Artifact onClick = object.classifier.artifact("onClick");
                if (onClick instanceof CustomOperation) {
                    controller.invoke((CustomOperation) onClick, object, null /* resultCallback /, this);
                }
            }
        });
    }
    */

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

    float pixelSize(double normalizedSize) { return (float) (normalizedSize * sizePx / 1000); }
    float pixelX(double normalizedX) { return (float) (normalizedX * sizePx / 1000) + widthPx / 2; }
    float pixelY(double normalizedY) { return (float) (-normalizedY * sizePx / 1000) + heightPx / 2; }


    double normalizedX(float screenX) {
        return (screenX - widthPx / 2) * 1000 / sizePx;
    }

    double normalizedY(float screenY) {
        return -(screenY - heightPx / 2) * 1000 / sizePx;
    }

    /*
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
        /*
        fillPaint.setColor(colorObject instanceof org.flowgrid.model.api.Color
                ? ((org.flowgrid.model.api.Color) colorObject).argb() : 0xffffffff);
        Object imageObject = instance.get("image");
        Object textObject = instance.get("text");
        if (imageObject instanceof ImageImpl) {
            ImageImpl image = (ImageImpl) imageObject;
            Rect dst = new Rect();
            dst.left = (int) pixelX(x - image.width() / 2);
            dst.right = (int) pixelX(x + image.width() / 2);
            dst.top = (int) pixelY(y + image.height() / 2);
            dst.bottom = (int) pixelY(y - image.height() / 2);
            //  canvas.drawBitmap(image.bitmap(), dst.left, dst.top, null);
            canvas.drawBitmap(image.bitmap(), null, dst, bitmapPaint);
        } else if (textObject instanceof String) {
            fillPaint.setTextSize(100 * sizePx / 1000);
            int start = 0;
            String text = (String) textObject;
            while (start < text.length()) {
                int end = text.indexOf('\n', start);
                if (end == -1) {
                    end = text.length();
                }
                canvas.drawText((String) textObject, start, end, pixelX(x), pixelY(y), fillPaint);
                start = end + 1;
                y -= 100;
            }
        } else if (instance.classifier().hasProperty("radius", PrimitiveType.NUMBER)) {
            double radius = instance.getNumber("radius");
            canvas.drawCircle(pixelX(x), pixelY(y), (int) (radius * sizePx / 1000), fillPaint);
        } else {
            double width = instance.getNumber("width");
            double height = instance.getNumber("height");
            canvas.drawRect(pixelX(x - width / 2), pixelY(y + height / 2),
                    pixelX(x + width / 2), pixelY(y - height / 2), fillPaint);
        }
        */
    }


    @Override
    public void drawBackground(GC gc, int clipX, int clipY, int clipW, int clipH) {
        invalidated = false;
        if (background != null) {
            gc.drawImage(background, 0, 0);
        } else {
            gc.fillRectangle(1, 1, widthPx, heightPx);
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
            System.out.println("FIXME: CanvasControl.postInvalidate();");
        }
    }



    synchronized void sizeChanged(int w, int h) {
        int oldW = widthPx;
        int oldH = heightPx;
        widthPx = w;
        heightPx = h;
        sizePx = Math.min(w, h);
        if (background != null) {
            Rectangle oldBmSize = background.getBounds();
            float oldBmW = oldBmSize.width;
            float oldBmH = oldBmSize.height;
            Image newBackground = new Image(getDisplay(), w, h);
          /*  int newMin2 = sizePx / 2;
            rectF.set(w / 2 - newMin2, h / 2 - newMin2,
                    w / 2 + newMin2, h / 2 + newMin2);
            if (oldBmW > oldBmH) {
                float scale = oldBmW / oldBmH;
                rectF.left = w / 2 - newMin2 * scale;
                rectF.right = w / 2 + newMin2 * scale;
            } else if (oldH > oldW){
                float scale = oldBmH / oldBmW;
                rectF.top = h/2 - newMin2 * scale;
                rectF.bottom = h/2 + newMin2 * scale;
            }
            Canvas canvas = new Canvas(newBackground);
            canvas.drawBitmap(background, null, rectF, bitmapPaint); */
            background = newBackground;
        }
    }


    public void postInvalidate() {
        if (!invalidated) {
            invalidated = true;
            System.out.println("FIXME: CanvasControl.postInvalidate()");  // FIXME
            redraw();
        }
    }

    public String toString() {
        return "Canvas#" + (hashCode() % 1000);
    }

}
