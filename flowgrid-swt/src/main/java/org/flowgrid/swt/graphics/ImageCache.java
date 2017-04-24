package org.flowgrid.swt.graphics;


import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.flowgrid.swt.port.Sprite;

import java.io.InputStream;
import java.util.HashMap;

public class ImageCache {
    static HashMap<String, Image> cache = new HashMap<>();

    public static Image getImage(Device device, String name) {
        Image bitmap = cache.get(name);
        if (bitmap == null) {
            InputStream is = Sprite.class.getResourceAsStream(name);
            if (is == null) {
                throw new RuntimeException("Resource not found: " + name);
            }
            bitmap = new Image(device, is);
            cache.put(name, bitmap);
        }
        return bitmap;
    }
}
