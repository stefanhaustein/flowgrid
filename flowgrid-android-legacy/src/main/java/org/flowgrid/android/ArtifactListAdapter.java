package org.flowgrid.android;

import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import org.flowgrid.android.api.ImageImpl;
import org.flowgrid.android.graphics.ArtifactDrawable;
import org.flowgrid.android.graphics.Drawing;
import org.flowgrid.model.Artifact;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.Container;
import org.flowgrid.model.CustomOperation;
import org.flowgrid.model.Module;
import org.flowgrid.model.Operation;
import org.flowgrid.model.Property;
import org.flowgrid.model.ResourceFile;
import org.flowgrid.model.VirtualOperation;
import org.flowgrid.model.io.IOCallback;


public class ArtifactListAdapter extends AbstractListAdapter<Artifact> implements OnItemClickListener {
  protected final MainActivity platform;
  protected Container container;
  
  private ArrayList<Artifact> list = new ArrayList<Artifact>(); 
  
  public ArtifactListAdapter(MainActivity platform, Container container) {
    this.platform = platform;
    this.container = container;
  }
  
  public void refresh() {
    list.clear();
    for (Artifact artifact: container) {
      if (artifact instanceof Module && ((Module) artifact).systemModule()) {
        continue;
      }
      if (artifact instanceof ResourceFile && artifact.name().equals("main.png") &&
          artifact.qualifiedName().startsWith("/missions/") &&
          !platform.settings().developerMode()) {
        continue;
      }
      list.add(artifact);
    }
    notifyDataSetChanged();
  }
  
  @Override
  public int getCount() {
    return list.size();
  }

  @Override
  public Artifact getItem(int position) {
    return list.get(position);
  }
  
  @Override
  public boolean areAllItemsEnabled() {
    return false;
  }
  
  @Override
  public boolean isEnabled(int position) {
    Artifact entry = getItem(position);
    if (entry instanceof CustomOperation) {
      CustomOperation cop = (CustomOperation) entry;
      
      if (cop.isTutorial()) {
        MainActivity platform = (MainActivity) cop.module().model().platform;
        return platform.settings().developerMode() || position == 0 || 
            !(getItem(position -1) instanceof CustomOperation) ||
          ((CustomOperation) getItem(position - 1)).tutorialData.passed();
      }
    }
    return true;
  }

  private void setBitmapAsync(final ImageView iv, final ResourceFile resource) {
    resource.resourceAsync(new IOCallback<Object>() {
      @Override
      public void onSuccess(final Object value) {
        platform.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            iv.setImageBitmap(((ImageImpl) value).bitmap());
          }
        });
      }
      @Override
      public void onError(IOException e) {
      }
    });

  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LinearLayout layout = (convertView instanceof LinearLayout) ? ((LinearLayout) convertView) : new LinearLayout(platform);
    layout.removeAllViews();
    
    TextView tv = (TextView) createViewFromResource(platform, android.R.layout.simple_list_item_1, parent);
    Artifact entry = getItem(position);
    final ImageView iv = new ImageView(platform);
    iv.setScaleType(ScaleType.CENTER_INSIDE);
    int padding = Views.px(platform, 12);
    iv.setPadding(padding, padding, padding, padding);

    if (entry instanceof Module) {
      Container module = (Container) entry;
      String label;
      if (module.owner() == module.model().rootModule && module.name().equals("missions")) {
        label = "\u25C7";
      } else if (module.qualifiedName().startsWith("/examples")) {
          label = "\u2606";
      } else {
        label = null;
      }
      iv.setImageDrawable(new ArtifactDrawable(platform, ArtifactDrawable.Kind.MODULE, label));
      Artifact a =  module.artifact("main.png");
      if (a instanceof ResourceFile) {
        setBitmapAsync(iv, ((ResourceFile) a));
      }
    } else if (entry instanceof Classifier) {
      Classifier classifier = (Classifier) entry;
      iv.setImageDrawable(new ArtifactDrawable(platform, ArtifactDrawable.Kind.CLASSIFIER, classifier.isInterface() ? "I" : "C"));
    } else if (entry instanceof ResourceFile) {
      ResourceFile rf = (ResourceFile) entry;
      if (rf.kind == ResourceFile.Kind.IMAGE) {
        iv.setImageDrawable(new ArtifactDrawable(platform, ArtifactDrawable.Kind.IMAGE, null));
        setBitmapAsync(iv, rf);
      } else {
        iv.setImageDrawable(new ArtifactDrawable(platform, ArtifactDrawable.Kind.SOUND, null));
      }
    } else if (entry instanceof Operation) {
      Operation op = (Operation) entry;
      if (op instanceof CustomOperation) {
        CustomOperation cop = (CustomOperation) op;
        if (cop.isTutorial()) {
          boolean enabled = isEnabled(position);
          tv.setEnabled(enabled);
          ArtifactDrawable ad = new ArtifactDrawable(platform, ArtifactDrawable.Kind.TUTORIAL, cop.tutorialData.passed() ? "\uf889" : null);
          ad.setEnabled(enabled);
          iv.setImageDrawable(ad);
//          iv.setImageResource(cop.tutorialData.passed() ? R.drawable.ic_tutorial_done : enabled ?  R.drawable.ic_tutorial : R.drawable.ic_tutorial_disabled);
        } else if (cop.asyncInput()) {
          iv.setImageDrawable(new ArtifactDrawable(platform, ArtifactDrawable.Kind.CONTINUOUS_OPERATION, "co"));
        } else {
          iv.setImageDrawable(new ArtifactDrawable(platform, ArtifactDrawable.Kind.OPERATION, op.classifier == null ? "op" : (Drawing.SHORT_THIS_PREFIX + "m")));
        }
      } else if (op instanceof VirtualOperation) {
        iv.setImageDrawable(
            new ArtifactDrawable(platform, ArtifactDrawable.Kind.OPERATION, "vm"));
      }
    } else if (entry instanceof Property) {
      iv.setImageDrawable(new ArtifactDrawable(platform, ArtifactDrawable.Kind.PROPERTY,((Property) entry).classifier == null ? "c" : (Drawing.SHORT_THIS_PREFIX + "p")));
    }
    if (entry.isPublic() && !(entry instanceof Module)) {
      tv.setTypeface(Typeface.DEFAULT_BOLD);
    }
    tv.setText(entry.toString());
    layout.addView(iv);
    LinearLayout.LayoutParams imageParams = ((LinearLayout.LayoutParams) iv.getLayoutParams());
    imageParams.gravity = Gravity.CENTER;
    imageParams.width = Views.px(platform, 48);
    imageParams.height = Views.px(platform, 48);
    layout.addView(tv);
    
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv.getLayoutParams();
    params.height = LayoutParams.MATCH_PARENT;
    iv.setLayoutParams(params);
    
    return layout;
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Artifact artifact = getItem(position);
    platform.openArtifact(artifact);
  }
}
