package org.flowgrid.android;

import java.io.IOException;

import org.flowgrid.android.api.ImageImpl;
import org.flowgrid.android.graphics.ArtifactDrawable;
import org.flowgrid.android.widget.MetaLayout;
import org.flowgrid.model.ResourceFile;
import org.flowgrid.model.ResourceFile.Kind;
import org.flowgrid.model.Sound;
import org.flowgrid.android.widget.ContextMenu;
import org.flowgrid.android.view.RunChart;
import org.kobjects.filesystem.api.IOCallback;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.mobidevelop.widget.SplitPaneLayout;

public class ResourceFileFragment extends ArtifactFragment<SplitPaneLayout> {
  private ResourceFile resourceFile;
  private TextView sizeTextView;
  private TextView rateTextView;
  private Sound sound;
  private ImageView imageView;
  private RunChart runChart;
  private MetaLayout metaLayout;

  public ResourceFileFragment() {
    super(SplitPaneLayout.class);
  }

  @Override
  public boolean onContextMenuItemClick(ContextMenu.Item item) {
    String title = item.getTitle().toString();
    if (title.equals("Play")) {
      if (sound != null) {
        sound.play();
      }
      return true;
    }
    return super.onContextMenuItemClick(item);
  }
   
  @Override
  public void onPlatformAvailable(Bundle savedInstanceState) {
    resourceFile = platform.model().resourceFile(getArguments().getString("artifact"));
    setArtifact(resourceFile);
    metaLayout = new MetaLayout(platform, rootView);

    if (resourceFile.kind == Kind.AUDIO) {
      addMenuItem(MENU_ITEM_PLAY);
    }
    addMenuItem(MENU_ITEM_RENAME_MOVE);
    addMenuItem(MENU_ITEM_DELETE);

    View contentView;
    if (resourceFile.kind == Kind.IMAGE) {
      contentView = imageView = new ImageView(platform);
      imageView.setScaleType(ScaleType.CENTER_INSIDE);
      imageView.setImageDrawable(new ArtifactDrawable(platform, ArtifactDrawable.Kind.IMAGE, null));
    } else {
      contentView = runChart = new RunChart(platform);
    }
    metaLayout.addView(contentView, 0, 0);

    sizeTextView = new TextView(platform);
    Views.applyEditTextStyle(sizeTextView, false);
    
    final IOCallback<Void> callback = platform.defaultIoCallback("Opening resource '" + resourceFile.qualifiedName() + "'");
    if (resourceFile.kind == ResourceFile.Kind.IMAGE) {
      metaLayout.addView(Views.addLabel("Image size (w x h):", sizeTextView), 1, 1);
      resourceFile.resourceAsync(new IOCallback<Object>() {
        @Override
        public void onSuccess(Object res) {
          final Bitmap bitmap = ((ImageImpl) res).bitmap();
          platform.runOnUiThread(new Runnable() {
            public void run() {
              imageView.setImageBitmap(bitmap);
              sizeTextView.setText(bitmap.getWidth() + " x " + bitmap.getHeight() + " pixel");
            }
          });
        }

        @Override
        public void onError(IOException e) {
          callback.onError(e);
        }
      });
    } else {
      metaLayout.addView(Views.addLabel("Sample length:", sizeTextView), 1, 1);
      rateTextView = new TextView(platform);
      Views.applyEditTextStyle(rateTextView, false);
      metaLayout.addView(Views.addLabel("Sampling rate:", rateTextView), 1, 1);
      resourceFile.resourceAsync(new IOCallback<Object>() {
        @Override
        public void onSuccess(Object res) {
          sound = (Sound) res;
          platform.runOnUiThread(new Runnable() {
            public void run() {
              sizeTextView.setText(sound.length() + " s");
              rateTextView.setText(sound.samplingRate() + " Hz");
              sound.getData(runChart.getDatBuffer(0));
              runChart.invalidate();
            }
          });
        }
        @Override
        public void onError(IOException e) {
          callback.onError(e);
        }
      });
    }
  }
}
