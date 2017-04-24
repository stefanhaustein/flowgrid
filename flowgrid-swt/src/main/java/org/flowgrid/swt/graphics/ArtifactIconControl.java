package org.flowgrid.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.flowgrid.model.Artifact;
import org.flowgrid.swt.ResourceManager;

public class ArtifactIconControl extends Canvas {
    private ArtifactIcon icon;
    private final int iconSize;

    public ArtifactIconControl(Composite parent, ResourceManager resourceManager, Artifact artifact) {
        super(parent, SWT.NONE);
        icon = ArtifactIcon.create(resourceManager, artifact);
        iconSize = resourceManager.dpToPx(24);
    }

    @Override
    public void drawBackground(GC gc, int clipX, int clipY, int clipW, int clipH) {
        Point size = getSize();
        icon.draw(gc, (size.x - iconSize) / 2, (size.y - iconSize) / 2, iconSize);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return new Point(iconSize, iconSize);
    }
}
