package org.flowgrid.swt.port;

import java.io.InputStream;
import java.util.HashMap;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.flowgrid.swt.graphics.ImageCache;


public class Sprite {
    static String[] NAMES = {"None", "Fluffy", "Bluffy", "Robby"};

    enum Mood {NEUTRAL, HAPPY, SAD, SUPER_HAPPY};

    private static HashMap<String,Image> bitmapCache = new HashMap<>();


    static Image getImage(GC gc, String name) {
        return ImageCache.getImage(gc.getDevice(), "/sprites/" + name + ".png");
    }

    public static void draw(GC gc, String icon, Mood mood, int x0, int y0, float cellSize) {

        int width = Math.round(cellSize * 3 / 2);
        int height = Math.round(cellSize);

        Image image = getImage(gc, icon);
        Rectangle imageBounds = image.getBounds();
        gc.drawImage(image, 0, 0, imageBounds.width, imageBounds.height, x0, y0, width, height);

        String mouthId;
        String eyesId;
        switch(mood) {
            case SAD:
                eyesId = "eyes_normal";
                mouthId = "mouth_unhappy";
                break;
            case HAPPY:
                eyesId = "eyes_normal";
                mouthId = "mouth_happy";
                break;
            case SUPER_HAPPY:
                eyesId = "eyes_happy";
                mouthId = "mouth_happy";
                break;
            default:
                eyesId = "eyes_normal";
                mouthId = "mouth_neutral";
        }


        int x1 = x0 + Math.round(cellSize / 6);
        int y1 = y0 + Math.round(cellSize / 2);
        int w1 = width - Math.round(cellSize / 3);
        int h1 = height - Math.round(cellSize / 2 + cellSize / 8);

        Image mouthImage = getImage(gc, mouthId);
        Rectangle mouthBounds = mouthImage.getBounds();
        gc.drawImage(mouthImage, 0, 0, mouthBounds.width, mouthBounds.height, x1, y1, w1, h1);

        x1 = x0 + Math.round(cellSize / 2.5f);
        y1 = y0 + Math.round(cellSize / 7);
        w1 = width - Math.round(cellSize / 1.25f);
        h1 = height - Math.round(cellSize / 7 + cellSize / 2);

        Image eyesImage = getImage(gc, eyesId);
        Rectangle eyesBounds = eyesImage.getBounds();
        gc.drawImage(eyesImage, 0, 0, eyesBounds.width, eyesBounds.height, x1, y1, w1, h1);
    }
}