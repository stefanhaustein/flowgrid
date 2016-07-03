package org.flowgrid.model;

import java.util.List;

/**
 * An artifact that may exist in the context of a class.
 */
public abstract class Member extends Artifact {
  public final Classifier classifier;
  public final Module module;
  
  
  
  protected Member(Container owner, String name) {
    super(owner, name);
    if (owner instanceof Classifier) {
      this.classifier = (Classifier) owner;
      this.module = classifier.module;
    } else {
      this.classifier = null;
      this.module = (Module) owner;
    }
  }

  public StructuredData structuredData() {
    throw new UnsupportedOperationException();
  }

  public StructuredData structuredData(String[] path) {
    StructuredData data = structuredData();
    for (int i = 0; i < path.length - 1; i++) {
      String name = path[i];
      final Object value = data.get(name);
      data = value instanceof List ? new StructuredData.ListWrapper((List) value, data.type(name)) :
          (StructuredData) value;
    }
    return data;
  }

  public abstract boolean matches(Artifact other, boolean checkVisibility);

  public void saveData() {
    save();
  }
  
  public boolean visibilityMatches(Member specific) {
    if (specific.isPublic()) {
      return true;
    }
    return specific.classifier.module == classifier.module;
  }
}
