package org.flowgrid.swt;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.DisplayType;
import org.flowgrid.swt.widget.MenuSelectionHandler;

public abstract class ArtifactEditor implements MenuSelectionHandler {


    public abstract void addArtifactMenu(Menu menuBar);

    public abstract Artifact getArtifact();

    public abstract SwtFlowgrid flowgrid();

    public void menuItemSelected(MenuItem item) {

        String title = item.getText();
        SwtFlowgrid flowgrid = flowgrid();
        final Artifact artifact = getArtifact();
        /*if (Strings.MENU_ITEM_DELETE.equals(title)) {
            Dialogs.confirm(flowgrid, "Confirmation", "Delete '" + artifact.qualifiedName() +
                "' and all contained content? This cannot be undone.", new Runnable() {
                @Override
                public void run() {
                    artifact.rename(null, null, null);
                    platform.finishFragment(ArtifactFragment.this);
                }
            });
        } else */if (Strings.MENU_ITEM_DOCUMENTATION.equals(title)
                || Strings.MENU_ITEM_ADD_DOCUMENTATION.equals(title)) {
            HelpDialog.show(flowgrid.shell, artifact == artifact.model().rootModule ? "Flowgrid" : artifact.toString(DisplayType.TITLE), artifact.documentation(), new Callback<String>() {
                @Override
                public void run(String value) {
                    if (artifact.setDocumentation(value)) {
                        artifact.save();
                    }
                }
            });
        } else if (Strings.MENU_ITEM_PUBLIC.equals(title)) {
            artifact.setPublic(!artifact.isPublic());
            item.setSelection(artifact.isPublic());
            artifact.save();
        } else if (Strings.MENU_ITEM_RENAME_MOVE.equals(title) ||
                Strings.MENU_ITEM_RENAME.equals(title)) {
            MoveDialog.show(this, artifact);
        }

    }
}
