package org.flowgrid.swt.operation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.flowgrid.model.ArrayType;
import org.flowgrid.model.Cell;
import org.flowgrid.model.Command;
import org.flowgrid.model.DisplayType;
import org.flowgrid.model.Edge;
import org.flowgrid.model.Model;
import org.flowgrid.model.Operation;
import org.flowgrid.model.Shape;
import org.flowgrid.model.Type;
import org.flowgrid.model.Types;
import org.flowgrid.model.api.LocalCallCommand;
import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.api.PropertyCommand;
import org.flowgrid.swt.Colors;
import org.flowgrid.swt.graphics.Drawing;

public class ScaledGraphElements {
    private final Model model;
    private float cellSize;
    private float originX;
    private float originY;
    private final Display display;
    private final Colors colors;
    private final Color highlightColor;
    private Font operatorFont;
    private Font valueFont;

    int arrayConnectorWidth;
    int connectorWidth;
    int operatorOutlineWidth;
    int valueBorderWidth;

    ScaledGraphElements(Display display, Colors colors, Model model) {
        this.display = display;
        this.colors = colors;
        this.highlightColor = new Color(display, 255, 255, 255, 127);
        this.model = model;
    }

    void setState(float originX, float originY, float cellSize) {
        this.originX = originX;
        this.originY = originY;

        //        operatorFont.dispose();  // FIXME
        // valueFont.dispose();

        if (cellSize != this.cellSize || operatorFont == null) {
            operatorFont = colors.getFont(Math.round(cellSize / 2), 0);
            valueFont = colors.getFont(Math.round(cellSize / 3), 0);

            arrayConnectorWidth = Math.max(1, Math.round(2 * cellSize / 16));
            connectorWidth = Math.max(1, Math.round(cellSize / 16));
            operatorOutlineWidth = Math.max(1, Math.round(cellSize / 16));
            valueBorderWidth = Math.max(1, Math.round(cellSize / 32));

            this.cellSize = cellSize;
        }
    }

    int screenX(int col) {
        return Math.round(col * cellSize - originX);
    }

    int screenY(int row) {
        return Math.round(row * cellSize - originY);
    }


    void drawCell(GC gc, Cell cell, boolean ready) {
        if (cell.command() == null) {
            drawConnections(cell, gc);
        } else {
            int x0 = screenX(cell.col());
            int y0 = screenY(cell.row());

            drawOperator(cell, gc, x0, y0, ready);

            if (cell.command().shape() == Shape.BAR) {
                float radius = cellSize / 6;
                y0 += Math.round(cellSize / 2 - radius);
            }


            for (int i = 0; i < cell.inputCount(); i++) {
                if (cell.isBuffered(i)) {
                    drawBuffer(gc, Math.round(x0 + (i + 0.5f) * cellSize), y0);
                }
            }

        }
    }


    public void drawConnector(GC gc, int x0, int y0, int x1, int y1, Type type) {
        Color color = colors.typeColor(type, false);
        if (type instanceof ArrayType) {
            gc.setLineWidth(arrayConnectorWidth);
            gc.setForeground(color);
            gc.drawLine(x0, y0, x1, y1);
            gc.setForeground(colors.background);
        } else {
            gc.setForeground(color);
        }
        gc.setLineWidth(connectorWidth);
        gc.drawLine(x0, y0, x1, y1);
    }

    void drawConnector(GC gc, int row, int col, Edge fromEdge, Edge toEdge, Type type) {
        int x0 = Math.round(col * cellSize - originX);
        int y0 = Math.round(row * cellSize - originY);
        int toX = x0 + xOffset(toEdge);
        int toY = y0 + yOffset(toEdge);
        drawConnector(gc, x0 + xOffset(fromEdge), y0 + yOffset(fromEdge), toX, toY, type);
        /*

        int toRow = row + toEdge.row;
        int toCol = col + toEdge.col;

        if (!operation.hasInputConnector(toRow, toCol, toEdge.opposite())) {
            drawOpenConnection(canvas, toX, toY, type);
        }
        */
    }

    void drawConnections(Cell cell, GC gc) {
        for (int i = 0; i < 4; i++) {
            Edge from = Edge.forIndex(i);
            Edge to = cell.connection(i);
            if (to != null) {
                drawConnector(gc, cell.row(), cell.col(), from, to, cell.inputType(i));
            }
        }
    }

    public void drawData(GC gc, int x, int y, Object value, boolean constant, int mod) {
        String text = Model.toString(value, DisplayType.GRAPH);

        int h = Math.round(cellSize / 2);
        int border = Math.round(cellSize / 16);
        gc.setFont(valueFont);
        Point textSize = gc.stringExtent(text);
        int width = textSize.x + h / 2; // TextHelper.measureText(valueTextPaint, text) + h / 2;
//      rectF.left = x - width / 2 - border;
//      rectF.top = y - h / 2 - border;
//      rectF.right = x + width / 2 + border;
//      rectF.bottom = y + h / 2 + border;
        // canvas.drawRoundRect(rectF, border, border, constant ? constantValueBoxPaint : valueBoxPaint);

        int x0 = x - width / 2;
        int y0 = y - h / 2;

        int tX = x - textSize.x / 2;
        int tY = Math.round(y0 + (h - textSize.y) / 2); ; // + (h - valueTextPaint.descent() - valueTextPaint.ascent()) / 2;

        int x1 = x0 + width;
        int y1 = y0 + h;

        gc.setLineWidth(valueBorderWidth);

        if (constant) {
            gc.setBackground(mod == 0 ? colors.background : colors.grays[Colors.Brightness.REGULAR.ordinal()]);
            gc.setForeground(colors.foreground);
            int[] points = new int[] {
                x0, y0,
                x1 + border, y0,
                x1, y1,
                x0 - border, y1
            };
            gc.fillPolygon(points);
            gc.drawPolygon(points);

    /*    } else if (value instanceof Boolean || (value instanceof String && Emoji.isEmoji(text))) {  FIXME
            if (value instanceof Boolean) {
                text = value.equals(Boolean.TRUE) ? "\uf889" : "\uf888";
            }
            operatorTextPaint.setTextSize(cellSize / 2);
            operatorTextPaint.setTextAlign(Align.CENTER);
            if (mod > 0) {
                Drawing.drawHalo(canvas, x, y, cellSize * 2 / 3, Color.WHITE);
            }

            paint = operatorTextPaint; */
        } else {
//            valueTextPaint.setColor(colors.white);
            gc.setForeground(colors.background);
            Colors.Brightness brightness = colors.dark
                    ? (mod > 0 ? Colors.Brightness.BRIGHTER : Colors.Brightness.DARKEST)
                    : (mod > 0 ? Colors.Brightness.DARKEST : Colors.Brightness.REGULAR);
            Color rgb = colors.typeColor(model.type(value), brightness);
//            if (mod > 0) {
                gc.setBackground(rgb);
  /*          } else {
                mod = -mod + 1;
                valueBoxPaint.setARGB(255/mod, ((rgb >> 16) & 255) ,
                        ((rgb >> 8) & 255), (rgb & 255));
            } */
            gc.fillRoundRectangle(x0, y0, x1 - x0, y1 - y0, 2 * border, 2 * border);
            gc.drawRoundRectangle(x0, y0, x1 - x0, y1 - y0, 2 * border, 2 * border);

            gc.setForeground(colors.white);
        }

        gc.drawString(text, tX, tY, true);
        // TextHelper.drawText(platform, canvas, text, x, tY, paint);
    }




    void drawOperator(Cell cell, GC gc, int x0, int y0, boolean ready) {
        Command cmd = cell.command();
        int width = cell.width();

        if (cmd.shape() == Shape.BRANCH) {
            //   float border = cellSize / 10f;
            Type type = cell.inputType(0);

            if (ready) {
                highlightCell(gc, x0, y0, highlightColor);
            }

            if (cmd.outputType(0, null) != null) {
                drawConnector(gc, cell.row(), cell.col(), Edge.TOP, Edge.LEFT, type);
            }
            if (cmd.outputType(1, null) != null) {
                drawConnector(gc, cell.row(), cell.col(), Edge.TOP, Edge.BOTTOM, type);
            }
            if (cmd.outputType(2, null) != null) {
                drawConnector(gc, cell.row(), cell.col(), Edge.TOP, Edge.RIGHT, type);
            }
            return;
        }

        Type[] inputSignature = new Type[cmd.inputCount()];

        // Input connectors
        int yM = Math.round(y0 + cellSize / 2);
        if (cell.inputCount() == 0) {
            int x = Math.round(x0 + cellSize / 2);
            drawConnector(gc, x, y0, x, yM, null);
            Type type = cell.inputType(0);
            if (type != null) {
                while (Types.isArray(type)) {
                    type = ((ArrayType) type).elementType;
                }
                drawConnector(gc, Math.round(x - cellSize / 10), y0, Math.round(x + cellSize / 10), y0, cell.inputType(0));
            }
        } else {
            for (int i = 0; i < cell.inputCount(); i++) {
                int x = Math.round(x0 + i * cellSize + cellSize / 2);

                Type actualType = cell.inputType(i);
                if (actualType != null && cmd.inputType(i).isAssignableFrom(actualType)) {
                    inputSignature[i] = actualType;
                } else {
                    inputSignature[i] = cmd.inputType(i);
                }
                drawConnector(gc, x, y0, x, yM, cell.inputType(i));
            }
        }

        // Output connectors
        for (int i = 0; i < cmd.outputCount(); i++) {
            Type oti = cmd.outputType(i, inputSignature);
            if (oti != null) {
                drawConnector(gc, cell.row(), cell.col() + i, null, Edge.BOTTOM, oti);
            }
        }

        boolean regular = true;
        if (cmd instanceof PortCommand) {
            PortCommand portCommand = (PortCommand) cmd;
            /*
            if (portCommand.port() instanceof TestPort) {
                ((TestPort) portCommand.port()).draw(platform, canvas, x0, y0, cellSize, ready);
                regular = false;
            }
            */
        }
        if (regular) {
            drawOperator(gc, cmd, x0, y0, width, ready);
        }

        for (int i = 0; i < inputSignature.length; i++) {
            if (cell.inputType(i) != null && cell.inputType(i) != inputSignature[i]) {
                int x = Math.round(x0 + i * cellSize + cellSize / 2);
                drawErrorMarker(gc, x, y0);
            }
        }
    }

    public void drawOperator(GC gc, Command cmd, int x0, int y0, int width, boolean ready) {
        boolean passSelf = false;
        String text = cmd.name();
        if (cmd instanceof Operation) {
            Operation op = (Operation) cmd;
            passSelf = op.classifier != null;
        } else if (cmd instanceof PropertyCommand) {
            PropertyCommand pc = (PropertyCommand) cmd;
            if (pc.implicitInstance()) {
                text = Drawing.THIS_PREFIX + text;
            } else {
                passSelf = pc.property().classifier != null;
            }
        } else if (cmd instanceof LocalCallCommand) {
            text = Drawing.THIS_PREFIX + text;
        }

        if ("and".equals(text)) {
            text = "&";//\u2227";
        } else if ("or".equals(text)) {
            text = "\u2228";
        } else if("xor".equals(text)) {
            text = "\u2295";
        } else if ("not".equals(text)) {
            text = "\u00ac";
        }
        drawShape(gc, cmd.shape(), text, x0, y0, width, ready, passSelf);
    }

    public void drawShape(GC gc, Shape shape, String text, int x0, int y0, int width, boolean highlight, boolean passThrough) {
        int x1 = Math.round(x0 + width * cellSize);
        int y1 = Math.round(y0 + cellSize);
        int xM = (x0 + x1) / 2;
        int yM = (y0 + y1) / 2;
        int border = Math.round(cellSize / 10);

        //Paint boxPaint = highlight ? readyBoxPaint : operatorBoxPaint; FIXME

        gc.setForeground(colors.foreground);
        gc.setBackground(highlight ? colors.highlight : colors.background);
        gc.setLineWidth(operatorOutlineWidth);

        int tX = Math.round(x0 + cellSize * width / 2);

        switch(shape) {
            case SQUARE:
            case CIRCLE: {
                //   tX = width > 1 ? x0 + cellSize * 3 / 4 : x0 + cellSize / 2;

                int top = y0 + border;
                int bottom = y1 - border;
                int left;
                int right;
                if (width == 2) {
                    left = Math.round(x0 + cellSize / 2 -  border);
                    right = Math.round(x1 - cellSize / 2 + border);
                } else {
                    left = x0 + border;
                    right = x1 - border;
                }
                int w = right - left;
                int h = bottom - top;
                if (shape == Shape.CIRCLE) {
                    gc.fillRoundRectangle(left, top, w, h, (right - left) / 2, Math.round(cellSize / 2 - border));
                    gc.drawRoundRectangle(left, top, w, h, (right - left) / 2, Math.round(cellSize / 2 - border));
                } else {
                    gc.fillRectangle(left, top, w, h);
                    gc.drawRectangle(left, top, w, h);
                }
                break;
            }
            case FOLDER: {
                int left = x0 + border;
                int top = y0 + border;
                int bottom = y0 + 5 * border;
                int right = xM;
                int w = right - left;
                int h = bottom - top;
                gc.fillRoundRectangle(left, top, w, h, border, border);
                gc.drawRoundRectangle(left, top, w, h, border, border);

                left = x0 + border;
                top = Math.round(y0 + 2.5f * border);
                bottom = y1 - border;
                right = x1 - border;
                w = right - left;
                h = bottom - top;
                gc.fillRoundRectangle(left, top, w, h, border, border);
                gc.drawRoundRectangle(left, top, w, h, border, border);
                break;
            }

            case HEXAGON: {
                int[] points = new int[] {
                        x0 + 2*border, y0 + border,
                        x1 - 2*border, y0 + border,
                        x1, yM,
                        x1 - 2*border, y1 - border,
                        x0 + 2*border, y1 - border,
                        x0, yM
                };
                gc.fillPolygon(points);
                gc.drawPolygon(points);
                break;
            }
            case INPUT_OUTPUT: {
                int [] points = {
                        x0, y0 + border,
                        x1 - border, y0 + border,
                        x1, yM,
                        x1 - border, y1 - border,
                        x0, y1 - border,
                        x0 + border, yM
                };
                gc.fillPolygon(points);
                gc.drawPolygon(points);
                break;
            }
            case INPUT: {
                int[] points = new int[] {
                        x0, y0 + border,
                        x1 - border, y0 + border,
                        x1 - border, y1 - border,
                        x0, y1 - border,
                        x0 + border, yM
                };
                gc.fillPolygon(points);
                gc.drawPolygon(points);
                break;
            }
            case PACKAGE: {
                int[] points = new int[]{
                        x0 + border, y0 + 2 * border,
                        xM, y0,
                        x1 - border, y0 + 2 * border,
                        x1 - border, y1 - 2 * border,
                        xM, y1,
                        x0 + border, y1 - 2 * border
                };
                gc.fillPolygon(points);
                gc.drawPolygon(points);
                gc.drawLine(x0 + border, y0 + 2 * border, xM, y0 + 4 * border);
                gc.drawLine(x1 - border, y0 + 2 * border, xM, y0 + 4 * border);
                gc.drawLine(xM, y1, xM, y0 + 4 * border);
                break;
            }
            case PARALLELOGRAM: {
                int[] points = new int[] {
                        x0 + border, y0 + border,
                        x1, y0 + border,
                        x1 - border, y1 - border,
                        x0, y1 - border
                };
                gc.fillPolygon(points);

                if (passThrough) {
                    gc.setForeground(colors.greens[Colors.Brightness.DARKEST.ordinal()]);
                    gc.drawLine(Math.round(x0 + cellSize / 2), y0 + border,
                            Math.round(x0 + cellSize / 2), y1 - border);
                    gc.setForeground(colors.foreground);
                }
                gc.drawPolygon(points);
                break;
            }
            case RHOMBUS: {
                int[] points = new int[] {
                        xM, y0,
                        x1, yM,
                        xM, y1,
                        x0, yM
                };
                gc.fillPolygon(points);
                gc.drawPolygon(points);
                break;
            }
            case STAR: {
                int[] points = new int[] {
                        x0, y0,
                        xM, y0 + border,
                        x1, y0,
                        x1 - border, yM,
                        x1, y1,
                        xM, y1 - border,
                        x0, y1,
                        x0 + border, yM
                };
                gc.fillPolygon(points);
                gc.drawPolygon(points);
                break;
            }
            case SOUND: {
                int[] points = new int[] {
                        x1 - border, y0 + border,
                        x1 - border, y1 - border,
                        xM, Math.round(yM + 1.5f * border),
                        xM, Math.round(yM - 1.5f * border)
                };
                gc.fillPolygon(points);
                gc.drawPolygon(points);

                int left = x0 + border;
                int right = Math.round(xM - 1.5f * border);
                int top = Math.round(yM - 1.5f * border);
                int bottom = Math.round(yM + 1.5f * border);
                int w = right - left;
                int h = bottom - top;
                gc.fillRectangle(left, top, w, h);
                gc.drawRectangle(left, top, w, h);
                return;
            }
            /*
            case STORAGE: {
                path.rewind();
                path.moveTo(x0 + 2 * border, y0 + border);
                path.lineTo(x1, y0 + border);
                path.cubicTo(x1 - 2 * border, y0 + border, x1 - 2 * border, y1 - border, x1, y1 - border);
                path.lineTo(x0 + 2 * border, y1 - border);
                path.cubicTo(x0, y1 - border, x0, y0 + border, x0 + 2 * border, y0 + border);
                path.close();
                canvas.drawPath(path, boxPaint);
                canvas.drawPath(path, operatorOutlinePaint);
                break;
            } */
            case OUTPUT: {
                int[] points = new int[] {
                        x0 + border, y0 + border,
                        x1 - border, y0 + border,
                        x1, yM,
                        x1 - border, y1 - border,
                        x0 + border, y1 - border
                };
                gc.fillPolygon(points);
                gc.drawPolygon(points);
                break;
            }
            case TRAPEZOID_DOWN: {
                int[] points = new int[] {
                        x0, y0 + border,
                        x1, y0 + border,
                        x1 - border, y1 - border,
                        x0 + border, y1 - border
                };
                gc.fillPolygon(points);
                gc.drawPolygon(points);
                break;
            }
            case TRAPEZOID_UP: {
                int[] points = new int[] {
                        x0 + border, y0 + border,
                        x1 - border, y0 + border,
                        x1, y1 - border,
                        x0, y1 - border
                };
                gc.fillPolygon(points);
                gc.drawPolygon(points);
                break;
            }

            case BAR:
                gc.drawLine(x0 + border, yM - border, x1 - border, yM - border);
                gc.drawLine(x0 + border, yM + border, x1 - border, yM + border);
                // No text, crosses
                return;
            default:
                int left = x0 + border;
                int top = y0 + border;
                int bottom = y1 - border;
                int right = x1 - border;
                int w = right - left;
                int h = bottom - top;
                if (shape == Shape.OVAL) {
                    int r = h / 2;
                    gc.fillRoundRectangle(left, top, w, h, r, r);
                    gc.drawRoundRectangle(left, top, w, h, r, r);
                } else if (shape == Shape.ROUNDED_RECTANGLE || shape == Shape.ASYNC) {
                    gc.fillRoundRectangle(left, top, w, h, border, border);
                    gc.drawRoundRectangle(left, top, w, h, border, border);
                } else {
                    gc.fillRectangle(left, top, w, h);
                    gc.drawRectangle(left, top, w, h);
                }

                if (passThrough) {
                    gc.setForeground(colors.greens[Colors.Brightness.DARKEST.ordinal()]);
                    gc.drawLine(Math.round(x0 + cellSize / 2), y0 + border,
                            Math.round(x0 + cellSize / 2), y1 - border);
                    gc.setForeground(colors.foreground);
                }
        }

        if (text == null || text.isEmpty()) {
            return;
        }

        int len = text.length();

        gc.setFont(operatorFont);

        // operatorTextPaint.setTextSize(cellSize / 2);
        // operatorTextPaint.setTextAlign(Align.CENTER);

        float available = cellSize * width - cellSize / 3;

        // operatorTextPaint.setUnderlineText(shape == Shape.ASYNC);

        Point textExtent = gc.stringExtent(text);

        if (textExtent.x > available) {
            gc.setFont(colors.getFont(Math.round(cellSize/3), 0));
            while ((textExtent = gc.stringExtent(text.substring(0, len))).x > available) {
                len--;
            }
            if (len < text.length()) {
                int start2 = len;
                int sp = text.indexOf(' ');
                if (sp != -1) {
                    start2 = sp + 1;
                } else {
                    for (int i = len - 1; i > 0; i--) {
                        if (Character.isLowerCase(text.charAt(i)) && Character.isUpperCase(text.charAt(i + 1))) {
                            start2 = i + 1;
                            break;
                        }
                    }
                }
                if (start2 < len) {
                    len = start2;
                }
                int len2 = text.length();
                Point textExtent2;
                while ((textExtent2 = gc.stringExtent(text.substring(start2, len2))).x > available) {
                    len2--;
                }

                int tY = y0; //  - cellSize / 6 + (cellSize - operatorTextPaint.descent() - operatorTextPaint.ascent()) / 2;
                gc.drawString(text.substring(0, len), tX - textExtent.x / 2, tY);
                tY += cellSize / 3;
                gc.drawString(text.substring(start2, len2), tX - textExtent2.x / 2, tY);
                return;
            }
        }

        //gc.setBackground(gc.getForeground());
        int tY = Math.round(y0 + (cellSize - textExtent.y) / 2);
        gc.drawString(text, tX - textExtent.x / 2, tY);
    }


    public void drawOpenConnection(GC gc, int toX, int toY, Type type) {
        gc.setBackground(colors.typeColor(type, false));

        int r = Math.round(cellSize / 16);
        gc.fillOval(toX - r, toY - r, 2 * r, 2 * r);
    }

    public void drawErrorMarker(GC gc, int x, int y0) {
        int radius = Math.round(cellSize / 4);
        gc.setForeground(colors.reds[2]);
        gc.setLineWidth(Math.max(1, Math.round(cellSize / 8)));
        gc.drawLine(x - radius, y0 - radius, x + radius, y0 + radius);
        gc.drawLine(x - radius, y0 + radius, x + radius, y0 - radius);
    }


    public void drawBuffer(GC gc, int x, int y) {
        gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
        int radius = Math.round(cellSize / 6);
        gc.fillRectangle(x - radius, y - radius, 2 * radius, 2 *  radius);
        gc.drawRectangle(x - radius, y - radius, 2 * radius, 2 *  radius);
    }

    public void highlightCell(GC gc, int x0, int y0, Color color) {
        gc.setBackground(color);
        gc.fillRectangle(x0, y0, Math.round(cellSize), Math.round(cellSize));
    }


    public int xOffset(Edge edge) {
        if (edge == null) {
            return Math.round(cellSize / 2);
        }
        switch(edge) {
            case TOP:
            case BOTTOM:
                return Math.round(cellSize / 2);
            case RIGHT:
                return Math.round(cellSize);
            default:
                return 0;
        }
    }

    public int yOffset(Edge edge) {
        if (edge == null) {
            return Math.round(cellSize / 2);
        }
        switch (edge) {
            case LEFT:
            case RIGHT:
                return Math.round(cellSize / 2);
            case BOTTOM:
                return Math.round(cellSize);
            default:
                return 0;
        }
    }


}
