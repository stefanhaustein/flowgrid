package org.flowgrid.model;

import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class DisabledMap {
  boolean disabled;
  Map<String, DisabledMap> children;

  public DisabledMap() {
  }

  public DisabledMap(HutnObject json) {
    if (json != null) {
      Iterator<String> it = json.keySet().iterator();
      if (it.hasNext()) {
        children = new TreeMap<>();
        while (it.hasNext()) {
          String key = it.next();
          Object entry = json.get(key);
          if (entry instanceof HutnObject) {
            children.put(key, new DisabledMap((HutnObject) entry));
          } else {
            DisabledMap child = new DisabledMap();
            child.disabled = true;
            children.put(key, child);
          }
        }
      }
    }
  }

  public boolean isEnabled(String title) {
    return !isDisabled(title);
  }

  public boolean isDisabled(String title) {
    if (children == null) {
      return false;
    }
    DisabledMap entry = children.get(title);
    return entry != null && entry.disabled;
  }

  public void enable(String title) {
    // Don't create an entry if not needed
    if (isDisabled(title)) {
      getChild(title).disabled = false;
    }
  }

  public void disable(String title) {
    getChild(title).disabled = true;
  }

  public DisabledMap getChild(String title) {
    if (children == null) {
      children = new TreeMap<String, DisabledMap>();
    }
    DisabledMap result = children.get(title);
    if (result == null) {
      result = new DisabledMap();
      children.put(title, result);
    }
    return result;
  }

  public void toJson(HutnSerializer result) {
    if (children != null) {
      for (String key : children.keySet()) {
        DisabledMap child = children.get(key);
        if (child.disabled) {
          result.writeBoolean(key, true);
        } else {
          result.startObject(key);
          child.toJson(result);
          result.endObject();
        }
      }
    }
  }
}
