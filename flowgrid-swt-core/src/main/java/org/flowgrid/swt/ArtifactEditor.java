package org.flowgrid.swt;

import org.eclipse.swt.widgets.Menu;
import org.flowgrid.model.Artifact;

public interface ArtifactEditor {

    void addArtifactMenu(Menu menuBar);

    Artifact getArtifact();
}
