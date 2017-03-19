package org.kobjects.swt.avd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Transform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;

public class AndroidVectorDrawable {
    private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";

    private static String getAttribute(Element element, String name) {
        String value = element.getAttributeNS(ANDROID_NAMESPACE, name);
        System.out.println("Attribute " + name + ": " + value);
        return value;
    }

    private static float getFloatAttribute(Element element, String name, float dflt) {
        String value = getAttribute(element, name);
        if (value == null || value.isEmpty()) {
            return dflt;
        }
        return Float.parseFloat(value);
    }

    private static float getDimensionalAttribute(Element element, String name, float pixlePerDp, float dflt) {
        String value = getAttribute(element, name);
        if (value == null || value.isEmpty()) {
            return dflt;
        }
        PathTokenizer tokenizer = new PathTokenizer(value);
        float f = tokenizer.readNumber();
        String dimension = tokenizer.getRemainder();
        if (dimension.equals("dp")) {
            f *= pixlePerDp;
        }
        return f;
    }

    public static AndroidVectorDrawable read(Device device, InputStream is, float pixelPerDp) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            AndroidVectorDrawable result = new AndroidVectorDrawable(device);
            result.parse(doc.getDocumentElement(), pixelPerDp);
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Device device;
    private String name;
    private float width;
    private float height;
    private float viewportWidth;
    private float viewportHeight;
    private Color tint;               // Currently unsupported
    private String tintMode;          // Currently unsupported
    private boolean autoMirrored;     // Currently unsupported
    private float alpha;
    private ArrayList<Drawable> children;
    private boolean disposed;

    private AndroidVectorDrawable(Device device) {
        this.device = device;
    }

    public void draw(GC gc) {
        float scaleX = viewportWidth / width;
        float scaleY = viewportHeight / height;
        Transform transform = null;
        Transform original = null;
        if (scaleX != 1 || scaleY != 1) {
            original = gc.getTransform();
            float[] buf = new float[6];
            original.getElements(buf);
            transform = new Transform(original.getDevice(), buf);
            transform.scale(scaleX, scaleY);
        }

        int saveAlpha = gc.getAlpha();
        gc.setAlpha(Math.min(Math.max(0, Math.round(alpha * 255)), 255));
        for(Drawable child: children) {
            child.draw(gc);
        }
        gc.setAlpha(saveAlpha);

        if (transform != null) {
            transform.dispose();
            gc.setTransform(original);
        }
    }

    public void dispose() {
        if (disposed) {
            return;
        }
        if (tint != null) {
            tint.dispose();
        }
        for(Drawable child : children) {
            child.dispose();
        }
    }

    private final Color getColorAttribute(Element element, String name) {
        String def = getAttribute(element, name);
        if (def == null || def.isEmpty()) {
            return null;
        }
        if (def.startsWith("#")) {
            int color = (int) Long.parseLong(def.substring(1), 16);
            switch (def.length()) {
                case 4:
                    color |= 0x0f000;
                    // Fall-through
                case 5:
                    return new Color(device,
                            ((color & 0xf00) >> 8) | ((color & 0xf00) >> 4),
                            ((color & 0xf0) >> 4) | ((color & 0xf0)),
                            ((color & 0xf)) | ((color & 0xf) << 4),
                            ((color & 0xf000) >> 12 | ((color & 0xf000) >> 8)));
                case 7:
                    color |= 0x0ff000000;
                case 9:
                    return new Color(device,
                            (color >> 16) & 255,
                            (color >> 8) & 255,
                            color & 0xff,
                            (color >> 24) & 255);

                default:
                    // Fall through to error.
            }
        }
        throw new RuntimeException("Invalid color: " + def);
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public String getName() {
        return name;
    }

    private void parse(Element root, float pixelPerDp) {
        System.out.println("Root element: " + root);

        name = getAttribute(root, "name");

        width = getDimensionalAttribute(root, "width", pixelPerDp, -1);
        height = getDimensionalAttribute(root, "height", pixelPerDp, -1);

        viewportHeight = Math.round(getFloatAttribute(root, "viewportHeight", 0));
        viewportWidth = Math.round(getFloatAttribute(root, "viewportWidth", 0));

        tint = getColorAttribute(root, "tint");

        // Missing: tintMode
        // Missing: autoMirrored

        alpha = getFloatAttribute(root, "alpha", 1);

        children = parseChildren(root);
    }

    private ArrayList<Drawable> parseChildren(Element element) {
        ArrayList<Drawable> result = new ArrayList<>();
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                if ("path".equals(child.getLocalName())) {
                    result.add(parseSvgPath(childElement));
                } else if ("group".equals(child.getLocalName())) {
                    result.add(parseGroup(childElement));
                } else {
                    throw new RuntimeException("Unrecognized element: " + childElement.getLocalName());
                }
            }
        }
        return result;
    }

    private Group parseGroup(Element element) {
        Group group = new Group();
        group.name = getAttribute(element, "name");
        group.rotation = getFloatAttribute(element, "rotation", 0);
        group.pivotX = getFloatAttribute(element, "pivotX", 0);
        group.pivotY = getFloatAttribute(element, "pivotY", 0);
        group.scaleX = getFloatAttribute(element, "scaleX", 1);
        group.scaleY = getFloatAttribute(element, "scaleY", 1);
        group.translateX = getFloatAttribute(element, "translateX", 0);
        group.translateY = getFloatAttribute(element, "translateX", 0);

        group.children = parseChildren(element);

        return group;
    }

     private Path parseSvgPath(Element element) {
         Path path = new Path();
         path.path = parseSvgPath(getAttribute(element, "pathData"));
         path.fillColor = getColorAttribute(element, "fillColor");
         path.strokeColor = getColorAttribute(element, "strokeColor");
         path.strokeWidth = getFloatAttribute(element, "strokeWidth", 1);
         path.strokeAlpha = getFloatAttribute(element, "strokeAlpha", 1);
         path.fillAlpha = getFloatAttribute(element, "fillAlpha", 1);
         path.trimPathStart = getFloatAttribute(element, "trimPathStart", 0);
         path.trimPathEnd = getFloatAttribute(element, "trimPathStart", 0);
         path.trimPathOffset = getFloatAttribute(element, "trimPathStart", 0);
         // Defaults taken from the SVG spec, Android doesn't seem to spec them.
         String cap = getAttribute(element, "strokeLineCap");
         path.strokeLineCap = "miter".equals(cap) ? SWT.CAP_SQUARE : "round".equals(cap) ? SWT.CAP_ROUND : SWT.CAP_FLAT;
         String join = getAttribute(element, "strokeLineJoin");
         path.strokeLineJoin = "bevel".endsWith(join) ? SWT.JOIN_BEVEL : "round".equals(join) ? SWT.JOIN_ROUND : SWT.JOIN_MITER;
         String fillType = getAttribute(element, "fillType");
         path.fillType = "eventOdd".equals(fillType) || "evenodd".equals(fillType) ? SWT.FILL_EVEN_ODD : SWT.FILL_WINDING;
         return path;
     }

     private org.eclipse.swt.graphics.Path parseSvgPath(String pathData) {
        org.eclipse.swt.graphics.Path svgPath = new org.eclipse.swt.graphics.Path(device);
        PathTokenizer tokenizer = new PathTokenizer(pathData);
        char cmd = '0';
        char prevCmd;
        float startX = 0;
        float startY = 0;
        float x = 0;
        float y = 0;
        float x1 = 0;
        float y1 = 0;
        float x2 = 0;
        float y2 = 0;
        while (!tokenizer.eof()) {
            prevCmd = cmd;
            cmd = tokenizer.readCommand();
            System.out.print("cmd: " + cmd);
            switch(cmd) {
                case 'M':
                    startX = x = tokenizer.readNumber();
                    startY = y = tokenizer.readNumber();
                    svgPath.moveTo(x, y);
                    while(tokenizer.hasNumber()) {
                        x = tokenizer.readNumber();
                        y = tokenizer.readNumber();
                        svgPath.lineTo(x, y);
                    }
                    break;
                case 'm':
                    startX = x = tokenizer.readNumber() + x;
                    startY = y = tokenizer.readNumber() + y;
                    svgPath.moveTo(x, y);
                    while(tokenizer.hasNumber()) {
                        x += tokenizer.readNumber();
                        y += tokenizer.readNumber();
                        svgPath.lineTo(x, y);
                    }
                    break;
                case 'L':
                    do {
                        x = tokenizer.readNumber();
                        y = tokenizer.readNumber();
                        svgPath.lineTo(x, y);
                    } while (tokenizer.hasNumber());
                    break;
                case 'l':
                    do {
                        x += tokenizer.readNumber();
                        y += tokenizer.readNumber();
                        svgPath.lineTo(x, y);
                    } while (tokenizer.hasNumber());
                    break;
                case 'H':
                    do {
                        x = tokenizer.readNumber();
                        svgPath.lineTo(x, y);
                    } while (tokenizer.hasNumber());
                    break;
                case 'h':
                    do {
                        x += tokenizer.readNumber();
                        svgPath.lineTo(x, y);
                    } while (tokenizer.hasNumber());
                    break;
                case 'V':
                    do {
                        y = tokenizer.readNumber();
                        svgPath.lineTo(x, y);
                    } while (tokenizer.hasNumber());
                    break;
                case 'v':
                    do {
                        y += tokenizer.readNumber();
                        svgPath.lineTo(x, y);
                    } while (tokenizer.hasNumber());
                    break;
                case 'Z':
                case 'z':
                    x = startX;
                    y = startY;
                    svgPath.close();
                    break;

                case 'C':
                    do {
                        x1 = tokenizer.readNumber();
                        y1 = tokenizer.readNumber();
                        x2 = tokenizer.readNumber();
                        y2 = tokenizer.readNumber();
                        x = tokenizer.readNumber();
                        y = tokenizer.readNumber();
                        svgPath.cubicTo(x1, y1, x2, y2, x, y);
                    } while (tokenizer.hasNumber());
                    break;
                case 'c':
                    do {
                        x1 = x + tokenizer.readNumber();
                        y1 = y + tokenizer.readNumber();
                        x2 = x + tokenizer.readNumber();
                        y2 = y + tokenizer.readNumber();
                        x += tokenizer.readNumber();
                        y += tokenizer.readNumber();
                        svgPath.cubicTo(x1, y1, x2, y2, x, y);
                    } while (tokenizer.hasNumber());
                    break;
                case 's':
                case 'S':
                    do {
                        if (prevCmd == 's' || prevCmd == 'S' || prevCmd == 'c' || prevCmd == 'C') {
                            x1 = x - (x2 - x);
                            y1 = y - (y2 - y);
                        } else {
                            x1 = x;
                            y1 = y;
                        }
                        prevCmd = cmd;
                        if (cmd == 's') {
                            x2 = x + tokenizer.readNumber();
                            y2 = y + tokenizer.readNumber();
                            x += tokenizer.readNumber();
                            y += tokenizer.readNumber();
                        } else {
                            x2 = tokenizer.readNumber();
                            y2 = tokenizer.readNumber();
                            x = tokenizer.readNumber();
                            y = tokenizer.readNumber();
                        }
                        svgPath.cubicTo(x1, y1, x2, y2, x, y);
                    } while (tokenizer.hasNumber());
                    break;
                case 't':
                case 'T':
                    do {
                        if (prevCmd == 't' || prevCmd == 'T' || prevCmd == 'q' || prevCmd == 'Q') {
                            x1 = x - (x1 - x);
                            y1 = y - (y1 - y);
                        } else {
                            x1 = x;
                            y1 = y;
                        }
                        prevCmd = cmd;
                        if (cmd == 't') {
                            x += tokenizer.readNumber();
                            y += tokenizer.readNumber();
                        } else {
                            x = tokenizer.readNumber();
                            y = tokenizer.readNumber();
                        }
                        svgPath.quadTo(x1, y1, x, y);
                    } while (tokenizer.hasNumber());
                    break;

                default:
                    throw new RuntimeException("Unrecognized command: " + cmd);
            }
            System.out.println(";");
        }
        return svgPath;
    }

    private interface Drawable {
        void draw(GC gc);
        String getName();
        void dispose();
    }

    private static class Group implements Drawable {
        private String name;
        private ArrayList<Drawable> children;
        float scaleX;
        float scaleY;
        float rotation;
        float pivotX;
        float pivotY;
        float translateX;
        float translateY;

        public void draw(GC gc) {
            Transform original = null;
            Transform transform = null;
            if (scaleX != 1 || scaleY != 1 || rotation != 0 || translateX != 0 || translateY != 0) {
                Transform orignal = gc.getTransform();
                float[] buf = new float[6];
                orignal.getElements(buf);
                transform = new Transform(gc.getDevice(), buf);
                if (scaleX != 1 || scaleY != 1) {
                    transform.scale(scaleX, scaleY);
                }
                if (rotation != 0) {
                    transform.translate(-pivotX, -pivotY);
                    transform.rotate(rotation);
                    transform.translate(pivotX, pivotY);
                }
                if (translateX != 0 || translateY != 0) {
                    transform.translate(translateX, translateY);
                }
                gc.setTransform(transform);
            }
            for (Drawable child : children) {
                child.draw(gc);
            }
            if (transform != null) {
                transform.dispose();
                gc.setTransform(original);
            }
        }

        public String getName() {
            return name;
        }

        public void dispose() {
            for (Drawable child: children) {
                child.dispose();
            }
        }
    }

    private static class Path implements Drawable {
        String name;
        org.eclipse.swt.graphics.Path path;
        Color fillColor;
        Color strokeColor;
        float strokeWidth;
        float strokeAlpha;
        float fillAlpha;
        float trimPathStart;
        float trimPathEnd;
        float trimPathOffset;
        int strokeLineCap;
        int strokeLineJoin;
        float strokeMiteerLimit;
        int fillType;

        public void draw(GC gc) {
            if (fillColor != null && fillAlpha != 0) {
                gc.setBackground(fillColor);
                gc.fillPath(path);
            }
            if (strokeColor != null && strokeAlpha != 0) {
                gc.setForeground(strokeColor);
                gc.drawPath(path);
            }
        }

        public String getName() {
            return name;
        }

        public void dispose() {
            if (fillColor != null) {
                fillColor.dispose();
            }
            path.dispose();
        }
    }


    private static class PathTokenizer {
        private final String path;
        private final int len;
        private int pos;

        PathTokenizer(String path) {
            this.path = path;
            this.len = path.length();
            this.pos = 0;
        }

        private boolean skip() {
            while (pos < len && (path.charAt(pos) <= ' ' || path.charAt(pos) == ',')) {
                pos++;
            }
            return pos < len;
        }

        boolean eof() {
            return !skip();
        }

        char readCommand() {
            if (!skip()) {
                throw new RuntimeException("Unexpected eof.");
            }
            char cmd = path.charAt(pos++);
            if ((cmd < 'a' || cmd > 'z') && (cmd < 'A' && cmd > 'Z')) {
                throw new RuntimeException("Illegal command char: " + cmd);
            }
            return cmd;
        }

        float readNumber() {
            if (!skip()) {
                throw new RuntimeException("Unexpected eof.");
            }
            float f = 0;
            boolean neg = false;
            while (pos < len && path.charAt(pos) == '-') {
                neg = !neg;
                pos++;
            }
            while (pos < len) {
                char c = path.charAt(pos);
                if (c < '0' || c > '9') {
                    break;
                }
                pos++;
                f = f * 10 + (c - '0');
            }
            if (pos < len && path.charAt(pos) == '.') {
                pos++;
                float factor = 0.1f;
                while (pos < len) {
                    char c = path.charAt(pos);
                    if (c < '0' || c > '9') {
                        break;
                    }
                    pos++;
                    f += (c - '0') * factor;
                    factor = factor * 0.1f;
                }
            }
            System.out.print(" " + (neg ? -f : f));
            return neg ? -f : f;
        }

        public boolean hasNumber() {
            if (!skip()) {
                return false;
            }
            char c = path.charAt(pos);
            return (c >= '0' && c <= '9') || c == '-' || c == '.';

        }

        public String getRemainder() {
            skip();
            String result = path.substring(pos).trim();
            pos = path.length();
            return result;
        }
    }
}
