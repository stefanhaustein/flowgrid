package org.flowgrid.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;

public class Instance implements StructuredData {
  public final Classifier classifier;
  final Map<String,Object> properties = new TreeMap<String,Object>();
  int id;
  
  public Instance(Classifier classifier) {
    this.classifier = classifier;
    this.id = classifier.instanceCount++;
  }

  public Classifier classifier() {
    return classifier;
  }

  public String toString() {
    return toString(DisplayType.LIST);
  }

  public String toString(DisplayType displayType) {
    if (displayType == DisplayType.GRAPH) {
      return classifier().name() + "#" + id;
    }
    StringBuilder sb = new StringBuilder();
    toString(sb, 160, new HashSet<Object>());
    return sb.toString();
  }

  public synchronized void toString(StringBuilder sb, int limit, Set<Object> seen) {
    sb.append(classifier.name);
    sb.append('#');
    sb.append(id);
    if (limit > 0) {
      Model.toString(properties, sb, limit, seen);
    }
  }
  
  
  public synchronized void set(String name, Object value) {
    properties.put(name, value);
  }
  
  public synchronized Object get(String name) {
    Object value = properties.get(name);
    if (value == null && classifier.hasProperty(name)) {
      Property property = classifier.property(name);
      if (property != null) {
        return property.value();
      }
    }
    return value;
  }

  @Override
  public Type type(String name) {
    return classifier().property(name).type();
  }

  public double getNumber(String name) {
    Object value = get(name);
    return value instanceof Number ? ((Number) value).doubleValue() : 0;
  }

  
  public void toJson(HutnSerializer json) {
    Model model = classifier.module.model();
    for (Map.Entry<String, Object> e: properties.entrySet()) {
      model.valueToJson(json, e.getKey(), e.getValue());
    }
    json.writeString("", classifier.qualifiedName());
  }

  public void fromJson(HutnObject json) {
    Model model = classifier.module.model();
    Iterator<String> it = json.keySet().iterator();
    while (it.hasNext()) {
      String key = it.next();
      if (!key.isEmpty()) {
        properties.put(key, model.valueFromJson(json.get(key)));
      }
    }
  }
}