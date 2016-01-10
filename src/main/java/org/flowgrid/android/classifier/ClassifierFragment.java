package org.flowgrid.android.classifier;

import java.util.List;

import org.flowgrid.android.ArtifactFragment;
import org.flowgrid.android.ArtifactListAdapter;
import org.flowgrid.model.Callback;
import org.flowgrid.android.Dialogs;
import org.flowgrid.android.type.TypeFilter;
import org.flowgrid.android.type.TypeMenu;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.DisplayType;
import org.flowgrid.model.Type;
import org.flowgrid.model.VirtualOperation;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Operation;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Property;
import org.flowgrid.android.widget.ContextMenu.Item;

import android.os.Bundle;
import android.widget.ListView;

public class ClassifierFragment extends ArtifactFragment<ListView> {
  private Classifier classifier;
  private ArtifactListAdapter adapter;

  public ClassifierFragment() {
    super(ListView.class);
  }

  @Override
  public void onPlatformAvailable(Bundle savedInstanceState) {
    classifier = platform.model().classifier(getArguments().getString("artifact"));
    classifier.ensureLoaded();
    adapter = new ArtifactListAdapter(platform, classifier);
    rootView.setAdapter(adapter);
    rootView.setOnItemClickListener(adapter);

    setArtifact(classifier);
    addMenuItem(classifier.hasDocumentation() ? MENU_ITEM_ADD_DOCUMENTATION : MENU_ITEM_DOCUMENTATION);
    addMenuItem(MENU_ITEM_PUBLIC);
    addMenuItem("Add Property...");
    addMenuItem("Add Operation...");
    if (!classifier.isInterface()) {
      addMenuItem("Implement Interface...");
    }
    addMenuItem(MENU_ITEM_RENAME_MOVE);
    addMenuItem(MENU_ITEM_DELETE);
  }
 
  @Override
  public boolean onContextMenuItemClick(Item item) {
    final String title = item.getTitle().toString();
    if ("Add Property...".equals(title)) {
      Dialogs.promptIdentifier(platform, "Add property", "Name", "", new Callback<String>() {
        @Override
        public void run(String propertyName) {
          Property property = new Property(classifier, propertyName, PrimitiveType.NUMBER, 0);
          classifier.addProperty(property);
          classifier.save();
          adapter.refresh();
          platform.openProperty(property);
        }
      });
      return true;
    }
    if ("Add Operation...".equals(title)) {
      Dialogs.promptIdentifier(getActivity(), title, "Name", "", new Callback<String>() {
        @Override
        public void run(String value) {
          Operation operation = classifier.isInterface()
              ? new VirtualOperation(value, classifier) :
              new CustomOperation(classifier, value, true);
          classifier.addOperation(operation);
          classifier.save();
          adapter.refresh();
          platform.openOperation(operation);
        }
      });
      return true;
    }
    if ("Implement Interface...".equals(title)) {
      new TypeMenu(platform, rootView, classifier.module(), Type.ANY, TypeFilter.INTERFACE, new Callback<Type>() {
        @Override
        public void run(Type value) {
          Classifier implement = (Classifier) value;
          List<String> problems = classifier.implementInterface(implement);
          if (problems.size() > 0) {
            StringBuilder sb = new StringBuilder("The following members have incompatible signatures:");

            for (String problem: problems) {
              sb.append("\n\n" + problem + "\n");
              sb.append("  Expected: ").append(implement.artifact(problem).toString(DisplayType.DETAILED)).append("\n");
              sb.append("  Found: ").append(classifier.artifact(problem).toString(DisplayType.DETAILED)).append("\n");
            }


            Dialogs.info(platform, "Cannot implement " + ((Classifier) value).name(), 
                 sb.toString());
          } else {
            classifier.save();
            adapter.refresh();
          }
        }
      }).show();
      return true;
    }
    return super.onContextMenuItemClick(item);
  }

  @Override
  public void refresh() {
    super.refresh();
    adapter.refresh();
  }
}
