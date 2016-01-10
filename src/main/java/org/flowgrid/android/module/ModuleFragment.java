package org.flowgrid.android.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.flowgrid.android.ArtifactFragment;
import org.flowgrid.android.ArtifactListAdapter;
import org.flowgrid.android.ResetDialog;
import org.flowgrid.model.Callback;
import org.flowgrid.android.Dialogs;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Module;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Property;
import org.flowgrid.model.ResourceFile;
import org.flowgrid.android.widget.ContextMenu;
import org.flowgrid.model.hutn.HutnObject;
import org.kobjects.filesystem.api.Filesystems;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ListView;

public class ModuleFragment extends ArtifactFragment<ListView> {
  private static final int ADD_IMAGE = 1;
  private static final int ADD_SOUND = 2;
  private static String[] EMPTY_STRING_ARRAY = new String[0];

  private ArtifactListAdapter adapter;
  private Module module;

  public ModuleFragment() {
    super(ListView.class);
  }

  @Override
  public void onPlatformAvailable(Bundle savedInstanceState) {
    module = getArguments() == null ? platform.model().rootModule : 
      (Module) platform.model().artifact(getArguments().getString("artifact", ""));
    adapter = new ArtifactListAdapter(platform, module);
    rootView.setAdapter(adapter);
    rootView.setOnItemClickListener(adapter);

    setArtifact(module);
    addMenuItem(module.hasDocumentation() ? MENU_ITEM_DOCUMENTATION : MENU_ITEM_ADD_DOCUMENTATION);
    boolean tutorialMode = module.isTutorial() && !platform.settings().developerMode();
    if (!module.isRoot() || platform.settings().developerMode()) {
      HutnObject storageConnections = platform.settings().storageConnections();
      Module parent = module.parent();
      while(parent != null) {
        if (storageConnections.containsKey(parent.qualifiedName())) {
          break;
        }
        parent = parent.parent();
      }
      if (parent == null) {
        addMenuItem("Storage connection");
      }
    }

    if (module.isRoot()) {
      addMenuItem("New module");
    } else if (!tutorialMode) {
      ContextMenu newMenu = menu.addSubMenu("Add...").getSubMenu();
      newMenu.add("New module");
      newMenu.add("New operation");
      newMenu.add("New class");
      newMenu.add("New interface");
      newMenu.add("New constant");
      newMenu.add("Image");
      newMenu.add("Sound");
      String path = module.qualifiedName() + "/";
      if (!path.equals("/exampes/")) {
        addMenuItem(MENU_ITEM_RENAME_MOVE);
        addMenuItem(MENU_ITEM_DELETE);
      }
      if (path.startsWith("/examples/")) {
        addMenuItem(MENU_ITEM_RESTORE);
      }
    }
  }

  public String getFileName(Uri uri) {
    String result = null;
    if (uri.getScheme().equals("content")) {
      Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
      try {
        if (cursor != null && cursor.moveToFirst()) {
          result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
      } finally {
        cursor.close();
      }
    }
    if (result == null) {
      result = uri.getPath();
      int cut = result.lastIndexOf('/');
      if (cut != -1) {
        result = result.substring(cut + 1);
      }
    }
    return result;
  }
  
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (resultCode == Activity.RESULT_OK && (requestCode == ADD_IMAGE || requestCode == ADD_SOUND)) {
      copyFromUri(intent.getData());
    }
  }   

  private void copyFromUri(final Uri uri) {
    final String fileName = getFileName(uri);
    platform.log("Copying " + uri.toString() + " to " + module.qualifiedName() + "/" +fileName);
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          OutputStream os = platform.storageFileSystem().save(module.qualifiedName() + "/" + fileName);
          InputStream is = getActivity().getContentResolver().openInputStream(uri);
          Filesystems.copyStream(is, os);
          Filesystems.close(is);
          Filesystems.close(os);
          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              module.addArtifact(new ResourceFile(module, fileName));
              module.save();
            }
          });
        } catch (IOException e) {
          platform.defaultIoCallback("Adding resource").onError(e);
        }
      }
    }).start();
  }

  @Override
  public boolean onContextMenuItemClick(ContextMenu.Item item) {
    final String title = item.getTitle().toString();
    if (title.equals("Storage connection")) {
      StorageDialog.show(platform, module);
      return true;
    }
    if (title.equals(MENU_ITEM_RESTORE)) {
      ResetDialog.show(platform, module.qualifiedName() + "/");
      return true;
    }
    if (title.startsWith("New ")) {
      Dialogs.promptIdentifier(platform, title, "Name", "", new Callback<String>() {
        @Override
        public void run(String newArtifactName) {
          Artifact newArtifact = null;
          if (newArtifactName == null || newArtifactName.length() == 0) {
            Dialogs.info(getActivity(), "Error", "A name is required.");
          } else if (module.artifact(newArtifactName) != null) {
            Dialogs.info(getActivity(), "Error",
                "An artifact named '" + newArtifactName + "' exists already in this package.");
          } else if ("New class".equals(title) || "New interface".equals(title)) {
            newArtifact = new Classifier(module, newArtifactName, 
                "New class".equals(title) ? Classifier.Kind.CLASS : Classifier.Kind.INTERFACE);
          } else if ("New operation".equals(title)) {
            newArtifact = new CustomOperation(module, newArtifactName, true);
          } else if ("New module".equals(title)) {
            newArtifact = new Module(platform.model(), module, newArtifactName, false);
          } else if ("New constant".equals(title)) {
            newArtifact = new Property(module, newArtifactName, PrimitiveType.NUMBER, null);
          }
          
          if (newArtifact != null) {
            module.addArtifact(newArtifact);
            platform.openArtifact(newArtifact);
            newArtifact.save();
          }
        }});
      return true;
    } 
    if (title.equals("Image")) {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent,
                "Select Image"), ADD_IMAGE);
      return true;
    }
    if (title.equals("Sound")) {
      Intent intent = new Intent();
      intent.setType("audio/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent,
                "Select Sound"), ADD_SOUND);
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
