package org.flowgrid.swt;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Callback;
import org.flowgrid.model.DisplayType;
import org.flowgrid.swt.widget.ContextMenu;

public abstract class ArtifactEditor implements ContextMenu.ItemClickListener {

    public abstract Artifact getArtifact();

    public abstract SwtFlowgrid flowgrid();

    public abstract void fillMenu(Menu menu);

    public abstract String getMenuTitle();

    @Override
    public boolean onContextMenuItemClick(ContextMenu.Item item) {
        String title = item.getTitle();
        final SwtFlowgrid flowgrid = flowgrid();
        final Artifact artifact = getArtifact();
        if (Strings.MENU_ITEM_DELETE.equals(title)) {
            Dialogs.confirm(flowgrid.shell, "Confirmation", "Delete '" + artifact.qualifiedName() +
                "' and all contained content? This cannot be undone.", new Runnable() {
                @Override
                public void run() {
                    System.out.println("FIXME: Delete");
                    artifact.rename(null, null, null);
                    flowgrid.openArtifact(null);
                }
            });
            return true;
        }
        if (Strings.MENU_ITEM_DOCUMENTATION.equals(title)
                || Strings.MENU_ITEM_ADD_DOCUMENTATION.equals(title)) {
            editDocumentation(null);
            return true;
        }
        if (Strings.MENU_ITEM_PUBLIC.equals(title)) {
            artifact.setPublic(!artifact.isPublic());
            item.setChecked(artifact.isPublic());
            artifact.save();
            return true;
        }
        if (Strings.MENU_ITEM_RENAME_MOVE.equals(title) ||
                Strings.MENU_ITEM_RENAME.equals(title)) {
            MoveDialog.show(this, artifact);
            return true;
        }
        return false;
    }

    protected void editDocumentation(final Runnable callback) {
        final SwtFlowgrid flowgrid = flowgrid();
        final Artifact artifact = getArtifact();
        Dialogs.promptMultiline(flowgrid.shell, artifact == artifact.model().rootModule ? "Flowgrid" : artifact.toString(DisplayType.TITLE), null, artifact.documentation(), new Callback<String>() {
            @Override
            public void run(String value) {
                if (artifact.setDocumentation(value)) {
                    artifact.save();
                    if (callback != null) {
                        callback.run();
                    }
                }
            }
        });
    }
}
