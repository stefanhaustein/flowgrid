package org.flowgrid.model;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.flowgrid.model.ActionFactory.Action;
import org.flowgrid.model.annotation.FgType;
import org.flowgrid.model.api.ApiSetup;
import org.flowgrid.model.hutn.HutnArray;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnObjectBuilder;
import org.flowgrid.model.hutn.HutnSerializer;
import org.flowgrid.model.hutn.HutnWriter;
import org.flowgrid.model.hutn.Hutn;
import org.flowgrid.model.io.Files;

public class Model {
  private static final String[] BUILTIN_COMPATIBILITY_PATHS = {
    "", "/control/switch/", "/comparison/", "/control/branch/"
  };

  private static final DecimalFormat shortDecimalFormat = new DecimalFormat("0.##");
  private static final DecimalFormat mediumDecimalFormat = new DecimalFormat("###0.#");
  private static final DecimalFormat shortExponentialFormat = new DecimalFormat("0.#E0");


  public enum JavaTypeSupportLevel {
    FULL,           // Maps to a FG type directly
    CONVERT,        // Needs conversion
    AVOID,          // May need conversion and should be avoided
    UNSUPPORTED     // Not supported.
  }
  final HashMap<Class<?>,TypeMapping> classMap = new HashMap<Class<?>,TypeMapping>();
  final private HashMap<String,String> aliasMap = new HashMap<String,String>();
  public final Platform platform;
  public final Module rootModule;

  private String resolveAlias(String name) {
    String resolved = aliasMap.get(name);
    if (resolved != null) {
      return resolved;
    }
    if (name.startsWith("/boolean/")) {
      name = "/logic/" + name.substring("/boolean/".length());
    } else if (name.startsWith("/control/boolean/")) {
      name = "/logic/" + name.substring("/control/boolean/".length());
    } else if (name.startsWith("/string/")) {
      name = "/text/" + name.substring("/string/".length());
    } else if (name.startsWith("/number/")) {
      name = "/math/" + name.substring("/number/".length());
    } else if (name.startsWith("/branch/") || name.startsWith("/switch/")) {
      name = "/control" + name;
    } else if (name.startsWith("/media/")) {
      name = "/graphics/" + name.substring("/media/".length());
    }
    return name;
  }


  public Model(Platform storage) {
    this.platform = storage;
    rootModule = new Module(this, null, "", false);

    Module systemModule = rootModule.systemModule("system");
    systemModule.addArtifact(Type.ANY);
    systemModule.addArtifact(PrimitiveType.BOOLEAN);
    systemModule.addArtifact(PrimitiveType.NUMBER);
    systemModule.addArtifact(PrimitiveType.TEXT);
    //timeModuel.add(PrimitiveType.DATE)

    // This is only necessary for the TYPEs and aliases, as the classes are registered via add
    // above.
    mapJavaClass(Boolean.TYPE, Model.JavaTypeSupportLevel.FULL, PrimitiveType.BOOLEAN);
    mapJavaClass(Double.TYPE, Model.JavaTypeSupportLevel.FULL, PrimitiveType.NUMBER);

    mapJavaClass(Integer.TYPE, Model.JavaTypeSupportLevel.CONVERT, PrimitiveType.NUMBER);
    mapJavaClass(Integer.class, Model.JavaTypeSupportLevel.CONVERT, PrimitiveType.NUMBER);

    mapJavaClass(List.class, Model.JavaTypeSupportLevel.FULL, ArrayType.ANY);

    new ApiSetup().run(this);
    platform.platformApiSetup().run(this);

    aliases("/system/Boolean", "/boolean/Boolean", "/logic/Boolean", "/system/boolean");
    aliases("/system/Number", "/system/number", "/math/number", "/math/Number");
    aliases("/system/Text", "/system/string", "/text/String", "/string/String");
    aliases("/sound/Sound", "/media/Sound");
    aliases("/graphics/Image", "/media/Image");
    aliases("/system/Any", "/system/any");
    aliases("/control/if", "/control/?");
  }

  public void aliases(String original, String... aliases) {
    for (String alias: aliases) {
      aliasMap.put(alias, original);
    }
  }

  public void mapJavaClass(Class<?> javaClass, JavaTypeSupportLevel supportLevel, Type type) {
    classMap.put(javaClass, new TypeMapping(supportLevel, type));
  }

  public Artifact artifact(final String qualifiedName) {
    final String resolved = resolveAlias(qualifiedName);
    final String[] parts = resolved.split("/");
    Artifact current = rootModule;
    for (String name: parts) {
      if (!name.isEmpty()) {
        if (current instanceof Container) {
          current = ((Container) current).artifact(name);
        } else {
          return null;
        }
      }
    }
    return current;
  }

  public Artifact artifact(String qualifiedName, Class<?> type) {
    Artifact artifact = artifact(qualifiedName);
    if (artifact == null) {
      throw new IllegalArgumentException(type.getSimpleName() + " '" + qualifiedName + "' not found.");
    }
    if (!type.isAssignableFrom(artifact.getClass())) {
      throw new IllegalArgumentException("'" + qualifiedName + "' is an " + 
          artifact.getClass().getSimpleName() + "; expected: " + type.getSimpleName());
      
    }
    return artifact;
  }

  public Command command(String name) {
    assert name.startsWith("/");

    for(String path: BUILTIN_COMPATIBILITY_PATHS) {
      String qualifiedName = path + name;
      Artifact artifact = artifact(qualifiedName);
      if (artifact instanceof Command) {
        return (Command) artifact;
      } else if (artifact instanceof ActionFactory) {
        return ((ActionFactory) artifact).createCommand(Action.INVOKE, false);
      }
    }
    throw new RuntimeException("Command not found: '" + name + "'");
  }
  
  public Classifier classifier(String qualifiedName) {
    return (Classifier) artifact(qualifiedName, Classifier.class);
  }

  public Module createSystemModule(String qualifiedName) {
    Module current = rootModule;
    String[] parts = qualifiedName.split("/");
    for(String name: parts) {
      if (!name.isEmpty()) {
        current = current.systemModule(name);
      }
    }
    return current;
  }
  
  Object deepCopy(Object original) {
    HutnObjectBuilder writer = new HutnObjectBuilder();
    valueToJson(writer, original);
    return valueFromJson(writer.build());
  }
  

  public CustomOperation operation(String qualifiedName) {
    return (CustomOperation) artifact(qualifiedName, CustomOperation.class);
  }

  public ResourceFile resourceFile(String qualifiedName) {
    return (ResourceFile) artifact(qualifiedName, ResourceFile.class);
  }

  JavaTypeSupportLevel javaTypeSupportLevel(java.lang.reflect.Type javaType, Annotation[] annotations) {
    if (annotations != null) {
      for(Annotation annotation: annotations) {
        if (annotation instanceof FgType) {
          return JavaTypeSupportLevel.FULL;
        }
      }
    }
    TypeMapping typeMapping = typeMappingForJavaType(javaType);
    return typeMapping == null ? JavaTypeSupportLevel.UNSUPPORTED : typeMapping.supportLevel;
  }

  public Type typeForJavaType(java.lang.reflect.Type javaType, Annotation[] annotations) {
    if (annotations != null) {
      for(Annotation annotation: annotations) {
        if (annotation instanceof FgType) {
          return typeForName(((FgType) annotation).value());
        }
      }
    }
    TypeMapping type = typeMappingForJavaType(javaType);
    if (type != null) {
      return type.type;
    }
    throw new RuntimeException("Unsupported Java type: " + javaType);
  }
 
  // Excludes null and Object.
  private TypeMapping typeMappingForJavaType(java.lang.reflect.Type javaType) {
    TypeMapping typeMapping = classMap.get(javaType);
    if (typeMapping != null) {
      return typeMapping;
    }
    if (javaType instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) javaType;
      javaType = pt.getRawType();
      if (List.class.isAssignableFrom((Class<?>) javaType)) {
        java.lang.reflect.Type javaElementType = pt.getActualTypeArguments()[0];
        if (javaElementType instanceof WildcardType) {
          javaElementType = ((WildcardType) javaElementType).getUpperBounds()[0];
        }
        TypeMapping elementSupport = typeMappingForJavaType(javaElementType);
        if (elementSupport == null || elementSupport.supportLevel == JavaTypeSupportLevel.CONVERT) {
          return null;
        }
        return new TypeMapping(JavaTypeSupportLevel.FULL, new ArrayType(elementSupport.type));
      }
    }
    
    if (javaType instanceof Class) {
      Class<?> javaClass = (Class<?>) javaType;

      if (javaClass.isArray()) {
        TypeMapping componentMapping = typeMappingForJavaType(javaClass.getComponentType());
        if (componentMapping != null) {
          JavaTypeSupportLevel level = componentMapping.supportLevel;
          if (level == JavaTypeSupportLevel.CONVERT || level == JavaTypeSupportLevel.FULL) {
            return new TypeMapping(JavaTypeSupportLevel.CONVERT, new ArrayType(componentMapping.type));
          }
        }
        return null;
      }

      java.lang.reflect.Type superType = javaClass.getGenericSuperclass();
      if (superType != null && superType != Object.class) {
        typeMapping = typeMappingForJavaType(superType);
        if (typeMapping != null) {
          return typeMapping;
        }
      }
      for (java.lang.reflect.Type i: javaClass.getGenericInterfaces()) {
        typeMapping = typeMappingForJavaType(i);
        if (typeMapping != null) {
          return typeMapping;
        }
      }
    }
    return null;
  }
  
  public Type type(Object object) {
    if (object == null) {
      return Type.ANY;
    }
    if (object instanceof Instance) {
      return ((Instance) object).classifier;
    }
    if (object instanceof List) {
      List<?> list = (List<?>) object;
      Type elementType = Type.ANY;
      if (list.size() > 0) {
        elementType = type(list.get(0));
        for (int i = 1; i < list.size(); i++) {
          if (type(list.get(i)) != elementType) {
            elementType = Type.ANY;
            break;
          }
        }
      }
      return new ArrayType(elementType, list.size());
    }
    return typeForJavaType(object.getClass(), null);
  }
  
  
  public Type typeForName(String name) {
    if (name.endsWith("]")) {
      int len = name.length();
      int cut = name.lastIndexOf("[");
      int typeLen = -1;
      if (cut < name.length() - 2) {
        typeLen = Integer.parseInt(name.substring(cut + 1, len - 1));
      }
      return new ArrayType(typeForName(name.substring(0, cut)), typeLen);
    }
    String nn = name.startsWith("/system/") ? name.substring(8) : name;
    if (nn.lastIndexOf("/") <= 0) {
      if (nn.startsWith("/")) {
        nn = nn.substring(1);
      }
      if (nn.equalsIgnoreCase("any") || nn.equalsIgnoreCase("object")) {
        return Type.ANY;
      }
      if (nn.equalsIgnoreCase("boolean")) {
        return PrimitiveType.BOOLEAN;
      }
      if (nn.equalsIgnoreCase("double") || nn.equalsIgnoreCase("number") || nn.equalsIgnoreCase("integer")) {
        return PrimitiveType.NUMBER;
      }
      if (nn.equalsIgnoreCase("string")) {
        return PrimitiveType.TEXT;
      }
    }
    return (Type) artifact(name, Type.class);
  }


  public void valueToJson(HutnSerializer json, Object value) {
    if (value instanceof Number) {
      json.writeDouble(((Number) value).doubleValue());
    } else if (value instanceof String) {
      json.writeString((String) value);
    } else if (value instanceof Boolean) {
      json.writeBoolean(((Boolean) value).booleanValue());
    } else if (value instanceof Instance) {
      json.startObject();
      ((Instance) value).toJson(json);
      json.endObject();
    } else if (value instanceof List) {
      json.startArray();
      for (Object o: (List<Object>) value) {
        valueToJson(json, o);
      }
      json.endArray();
    } else if (value == null) {
      json.writeString(null);
    } else {
      throw new RuntimeException("Cannot serialize " + value + " of type " + value.getClass() + " to JSON.");
    }
  }


  public void valueToJson(HutnSerializer json, String name, Object value) {
    if (value instanceof Number) {
      json.writeDouble(name, ((Number) value).doubleValue());
    } else if (value instanceof String) {
      json.writeString(name, (String) value);
    } else if (value instanceof Boolean) {
      json.writeBoolean(name, ((Boolean) value).booleanValue());
    } else if (value instanceof Instance) {
      json.startObject(name);
      ((Instance) value).toJson(json);
      json.endObject();
    } else if (value instanceof List) {
      json.startArray(name);
      for (Object o: (List<Object>) value) {
        valueToJson(json, o);
      }
      json.endArray();
    } else if (value != null) {
      throw new RuntimeException("Cannot serialize " + value + " of type " + value.getClass() + " to JSON.");
    }
  }

  public Object valueToJson(Object value) {
    HutnObjectBuilder writer = new HutnObjectBuilder();
    valueToJson(writer, value);
    return writer.build();
  }

  public Object valueFromJson(Object json) {
    if (json == null) {
      return null;
    }
    if (json instanceof Double || json instanceof Boolean || json instanceof String) {
      return json;
    }
    if (json instanceof Number) {
      return ((Number) json).doubleValue();
    }
    if (json instanceof HutnObject) {
      String typeName = ((HutnObject) json).getString("", "");
      Type type = typeForName(typeName);
      Instance instance = new Instance((Classifier) type);
      instance.fromJson((HutnObject) json);
      return instance;
    }
    if (json instanceof HutnArray) {
      HutnArray jsonArray = (HutnArray) json;
      ArrayList<Object> result = new ArrayList<Object>(jsonArray.size());
      for (int i = 0; i < jsonArray.size(); i++) {
        result.add(valueFromJson(jsonArray.get(i)));
      }
      return result;
    }
    throw new RuntimeException("Cannot parse " + json + " from JSON.");
  }

  public static String toString(Object value) {
    return toString(value, DisplayType.LIST);
  }

  public static void toString(Object value, StringBuilder sb, int limit, Set<Object> seen) {
    if (value instanceof String) {
      sb.append('"');
      sb.append(((String) value).replaceAll("\"", "\\\""));
      sb.append('"');
    } else if (value instanceof Map) {
      if (seen.contains(value)) {
        sb.append("{.recursion.}");
        return;
      }
      seen.add(value);
      Map<Object,Object> map = (Map<Object,Object>) value;
      sb.append("{");
      boolean first = true;
      for (Map.Entry<Object,Object> e : map.entrySet()) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        if (sb.length() > limit) {
          sb.append("...");
          break;
        }
        sb.append(e.getKey());
        sb.append(": ");
        toString(e.getValue(), sb, limit, seen);
      }
      sb.append("}");
    } else if (value instanceof List) {
      if (seen.contains(value)) {
        sb.append("[.recursion.]");
        return;
      }
      seen.add(value);
      List list = (List) value;
      sb.append("[");
      boolean first = true;
      for (Object element : list) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        if (sb.length() > limit) {
          sb.append("...");
          break;
        }
        toString(element, sb, limit, seen);
      }
      sb.append("}");
    } else if (value instanceof Instance) {
      ((Instance) value).toString(sb, limit, seen);
    } else {
      sb.append(toString(value));
    }
  }

  public static String toString(Object value, DisplayType displayType) {
    if (value == null) {
      return "null";
    }
    if (value instanceof Number) {
      if (displayType == DisplayType.GRAPH) {
        double d = ((Number) value).doubleValue();
        double abs = Math.abs(d);
        if (abs > 9999.9 || (abs < 0.1 && abs != 0)) {
          // Exponential
          return shortExponentialFormat.format(d);
        }
        if (abs < 9) {
          return shortDecimalFormat.format(d);
        }
        return mediumDecimalFormat.format(d);
      }
      double d = ((Number) value).doubleValue();
      if (d == (int) d) {
        return String.valueOf((int) d);
      }
      return String.valueOf(d);
    }
    if (value instanceof List) {
      if (displayType == displayType.GRAPH) {
        List<?> arr = (List<?>) value;
        return "[." + arr.size() + ".]";
      }
      StringBuilder sb = new StringBuilder();
      toString(value, sb, 160, new HashSet<Object>());
      return sb.toString();
    }
    if (value instanceof Map) {
      StringBuilder sb = new StringBuilder();
      toString(value, sb, 160, new HashSet<Object>());
      return sb.toString();
    }
    if (value instanceof Instance) {
      return ((Instance) value).toString(displayType);
    }
    return value.toString();
  }

  
  static class TypeMapping {
    final JavaTypeSupportLevel supportLevel;
    final Type type;
    TypeMapping(JavaTypeSupportLevel supportLevel, Type type) {
      this.supportLevel = supportLevel;
      this.type = type;
    }
  }

  public static HutnObject loadJson(File root, String filename) {
    try {
      InputStream is = new FileInputStream(new File(root, filename));
      InputStreamReader reader = new InputStreamReader(is, "UTF-8");
      Object result = Hutn.parse(reader);
      reader.close();
      return (HutnObject) result;
    } catch(Exception e) {
      throw new RuntimeException("Exception loading '" + filename + "'", e);
    }
  }
/*
  public static HutnObject loadJson(Filesystem filesystem, String filename) {
    try {
      InputStream is = filesystem.load(filename);
      InputStreamReader reader = new InputStreamReader(is, "UTF-8");
      Object result = Hutn.parse(reader);
      reader.close();
      return (HutnObject) result;
    } catch(Exception e) {
      throw new RuntimeException("Exception loading '" + filename + "'", e);
    }
  }
*/
  public static void saveBytes(final File root, final String filename, byte[] data) {
    try {
      boolean unchanged = true;
      File file = new File(root, filename);
      try {
        InputStream is = Files.load(root, filename);
        for (int i = 0; i < data.length; i++) {
          if (is.read() != (data[i] & 255)) {
            unchanged = false;
            break;
          }
        }
        if (is.read() != -1) {
          unchanged = false;
        }
        is.close();
      } catch(Exception e) {
        unchanged = false;
      }
      if (!unchanged) {
        OutputStream os = Files.save(root, filename);
        os.write(data);
        os.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HutnWriter saveJson(final File root, final String filename) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream() {
      boolean saved;
      public void close() throws IOException {
        super.close();
        if (!saved) {
          saveBytes(root, filename, toByteArray());
        }
      }
    };
    try {
      Writer writer = new OutputStreamWriter(baos, "UTF-8");
      return new HutnWriter(writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
