package org.flowgrid.model;

import org.flowgrid.model.Classifier.Kind;
import org.flowgrid.model.Model.JavaTypeSupportLevel;
import org.flowgrid.model.hutn.HutnSerializer;
import org.flowgrid.model.io.Files;

/**
 * This is not named "Package" to avoid conflicts with the java keyword for 
 * variables.
 */
public class Module extends Container {
  public static final String MODULE_FILE_EXTENSION = ".fgm";
  public static final String MODULE_FILENAME = "module" + MODULE_FILE_EXTENSION;
  
  private final Model model;

  public Module(final Model model, Module parent, String moduleName, boolean systemModule) {
    super(parent, moduleName);
    this.model = model;
    this.builtin = systemModule;
    this.state = systemModule ? State.LOADED : State.STUB;
    this.isPublic = true;
  }


  public void addArtifact(Artifact artifact) {
    ensureLoaded();
    artifacts.put(artifact.name, artifact);
    if (artifact instanceof Type) {
      Type type = (Type) artifact;
      if (type.javaClass() != Instance.class) {
        model.mapJavaClass(type.javaClass(),
            type == Type.ANY ? JavaTypeSupportLevel.AVOID : JavaTypeSupportLevel.FULL, type);
      }
    }
  }

  public Classifier addBuiltinInterface(String name) {
    Classifier c = new Classifier(this, name, Kind.INTERFACE);
    c.setPublic(true);
    c.builtin = true;
    c.state = State.LOADED;
    addArtifact(c);
    return c;
  }

  public void addJavaOperations(Class<?> javaClass, Class<?> firstArgFilter, String... names) {
    JavaBridge.addAllImpl(this, null, javaClass, firstArgFilter, names);
  }

  public Classifier addJavaClass(String localName, Class<?> javaClass, String... operationNames) {
    Classifier classifier = new Classifier(this, localName, javaClass);
    model.mapJavaClass(javaClass, JavaTypeSupportLevel.FULL, classifier);
    addArtifact(classifier);
    JavaBridge.addAllImpl(this, classifier, javaClass, null, operationNames);
    return classifier;
  }

  public Classifier classifier(String name) {
    return (Classifier) artifact(name, Classifier.class);
  }

  public boolean hasArtifact(String name) {
    ensureLoaded();
    return artifacts.containsKey(name);
  }

  public boolean hasDirectory() {
    return true;
  };

  public boolean isRoot() {
    return owner == null;
  }
  
  public Model model() {
    return model;
  }

  public Module module(String name) {
    return (Module) artifact(name, Module.class);
  }

  public Module parent() {
    return (Module) owner;
  }

  public String path() {
    return isRoot() ? "/" : (parent().path() + name() + "/");
  }

  @Override 
  public void toJson(HutnSerializer json, SerializationType serializationType) {
    int level = json.level();
    super.toJson(json, serializationType);
    json.assertLevel(level);
    json.writeString("kind", "module");
  }
  
  public boolean systemModule() {
    return builtin;
  }

  public void remove(Artifact artifact) {
    ensureLoaded();
    artifacts.remove(artifact.name());
  }

  public String toString(DisplayType type) {
    if (type == DisplayType.LIST) {
      return name + "/";
    }
    return super.toString(type);
  }

  public Module systemModule(String name) {
    // Does not call ensureLoaded because the builtins are needed to actually load stuff...
    Module module = (Module) artifacts.get(name); 
    if (module == null) {
      module = new Module(model(), this, name, true);
      artifacts.put(name, module);
    }
    return module;
  }

  @Override
  public double order() {
    return Artifact.ORDER_MODULE;
  }

  public void deleteFile(String name) {
    Files.delete(model.platform.storageRoot(), qualifiedName() + "/" + name);
  }

  public boolean isTutorial() {
    return owner == null ? name.equals("missions") : parent().isTutorial();
  }

  @Override
  public String jsonFilename() {
    return path() + MODULE_FILENAME;
  }


  public Type typeForName(String name) {
    if (!name.startsWith("/") && !name.equals("Number[]")) {  // TODO(haustein): Remove this hack!
      name = qualifiedName() + "/" + name;
    }
    return model.typeForName(name);
  }

}
