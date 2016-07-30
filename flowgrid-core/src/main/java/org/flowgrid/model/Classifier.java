package org.flowgrid.model;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.flowgrid.model.VirtualOperation.Parameter;
import org.flowgrid.model.api.ConstructorCommand;
import org.flowgrid.model.api.ThisCommand;
import org.flowgrid.model.api.TypeFilterCommand;
import org.flowgrid.model.hutn.HutnSerializer;

public class Classifier extends Container implements Type, ActionFactory {
  public static final String CLASS_FILE_EXTENSION = ".fgc";
  public static final String INTERFACE_FILE_EXTENSION = ".fgi";
  public static final Action[] ACTIONS = {Action.CREATE, Action.FILTER};

  public enum Kind {
    JAVACLASS, INTERFACE, CLASS
  }

  Module module;
  int instanceCount;
  private Kind kind;
  private final Class<?> javaClass;

  public Classifier(Module module, String name, Kind type) {
    super(module, name); 
    this.javaClass = Instance.class;
    this.module = module;
    this.kind = type;
  }

  public Classifier(Module module, String name, Class<?> javaClass) {
    super(module, name);
    this.javaClass = javaClass;
    this.module = module;
    this.kind = Kind.JAVACLASS;
    this.isPublic = true;
    this.state = State.LOADED;
    this.builtin = true;
  }

  @Override
  public Action[] actions() {
    return ACTIONS;
  }

  public void addProperty(Property property) {
    artifacts.put(property.name(), property);
  }

  public Classifier addPublicProperty(String name, Type type, Object initialValue) {
    Property p = new Property(this, name, type, initialValue);
    p.isPublic = true;
    addProperty(p);
    return this;
  }

  public void addOperation(Operation operation) {
    artifacts.put(operation.name(), operation);
  }

  public VirtualOperation addVirtualOperation(String name) {
    VirtualOperation op = new VirtualOperation(name, this);
    op.isPublic = true;
    this.addOperation(op);
    return op;
  }

  public String fileExtension() {
    return isInterface() ? INTERFACE_FILE_EXTENSION : CLASS_FILE_EXTENSION;
  }

  public boolean isAssignableFrom(Type type) {
    if (!isInterface()) {
      return this.equals(type);
    }
    if (!(type instanceof Classifier)) {
      return false;
    }
    Classifier specific = (Classifier) type;
    
    // Check if all operations and properties of this kind are available in
    // the specific kind.
    for (Artifact member: artifacts.values()) {
      if (!((Member) member).matches(specific.artifact(member.name()), true)) {
        return false;
      }
    }
    return true;
  }

  public boolean isInterface() {
    return kind == Kind.INTERFACE;
  }

  public boolean hasProperty(String name, Type type) {
    Artifact artifact = artifact(name);
    return artifact instanceof Property && ((Property) artifact).type() == type;
  }

  public Member member(String name) {
    return artifact(name, Member.class);
  }
  
  public Module module() {
    return module;
  }

  public Operation operation(String operationName) {
    Member result = member(operationName);
    if (!(result instanceof Operation)) {
      throw new RuntimeException("Operation '" + operationName + "' not found in classifier '" + name + "'.");
    }
    return (Operation) result;
  }

  public List<Operation> operations(List<Type> types) {
    ArrayList<Operation> result = new ArrayList<Operation>();
    for (Artifact member: artifacts.values()) {
      if (member instanceof Operation) {
        Operation o = (Operation) member;
        if (o.matches(types)) {
          result.add(o);
        }
      }
    }
    return result;
  }
  
  public Property property(String propertyName) {
    Member result = member(propertyName);
    if (!(result instanceof Property)) {
      throw new RuntimeException("Property '" + propertyName + "' not found in classifier '" + name + " artifacts: " + artifacts);
    }
    return (Property) result;
  }
  
  /**
   * Properties settable to kind if kind != null; all properties
   * otherwise; excludes constants if a kind is given.
   */
  public List<Property> properties(Type type) {
    ensureLoaded();
    ArrayList<Property> result = new ArrayList<Property>();
    for (Artifact member: artifacts.values()) {
      if (member instanceof Property) {
        Property p = (Property) member;
        if (type == null || p.type().isAssignableFrom(type)) {
          result.add(p);
        }
      }
    }
    return result;
  }

  @Deprecated
  public void removeOperation(Operation operation) {
    artifacts.remove(operation.name());
  }

  @Override
  public void toJson(HutnSerializer json, SerializationType serializationType) {
    super.toJson(json, serializationType);
    json.writeString("kind", kind.name().toLowerCase(Locale.US));
  }
  
  public String toString(DisplayType type) {
    if (type == DisplayType.TITLE) {
      return (kind == Kind.INTERFACE ? "Interface '" : "Class '") + name() + "'";
    }
    return super.toString(type);
  }

  public boolean hasProperty(String name) {
    return artifact(name) instanceof Property;
  }

  public boolean hasOperation(String name) {
    return artifact(name) instanceof Operation;
  }

  @Override
  public Command createCommand(Action action, boolean implicitInstance) {
    if (action == null) {  // Remove
      action = Action.CREATE;
    }
    switch (action) {
      case CREATE:
        return new ConstructorCommand(this);
      case FILTER:
        return new TypeFilterCommand(this);
      case THIS:
        return new ThisCommand(this);
      default:
        throw new IllegalArgumentException("Unsupported action: " + action);
    }
  }

  public boolean hasDirectory() {
    return !isInterface();
  }

  @Override
  public boolean matches(List<Type> inputTypes) {
    return true;
  }

  public boolean isInstantiable() {
    if (javaClass == Instance.class) {
      return !isInterface();
    }
    try {
      javaClass.getConstructor((Class<?>) null);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  public Object newInstance() {
    try {
      return javaClass == Instance.class ? new Instance(this) : javaClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Instantiation issue for " + this, e);
    }
  }

  @Override
  public Class<?> javaClass() { return javaClass; }

  @Override
  public double order() { return ORDER_TYPE; }

  @Override
  public String jsonFilename() {
    return isInterface() ? qualifiedName() + fileExtension() : 
      (qualifiedName() + "/class" + fileExtension());
  }

  /**
   * Adds all interfaces and operations to implement the given interface
   */
  public List<String> implementInterface(Classifier inter) {
    ArrayList<String> incompatible = new ArrayList<String>();
    for (Artifact iMember: inter) {
      Member localMember = (Member) artifact(iMember.name());
      if (localMember != null && !localMember.matches(iMember, false)) {
        incompatible.add(iMember.name());
      }
    }
    if (incompatible.size() == 0) {
      boolean makePublic = inter.module != module;
      for (Artifact iMember: inter) {
        Member localMember = (Member) artifact(iMember.name());
        if (localMember != null) {
          if (makePublic) {
            localMember.setPublic(true);
          }
        } else {
          if (iMember instanceof Property) {
            Property iProperty = (Property) iMember;
            Property localProperty = new Property(this, iProperty.name(), iProperty.type(), null);
            if (makePublic) {
              localProperty.setPublic(true);
            }
            addProperty(localProperty);
          } else if (iMember instanceof Operation) {
            VirtualOperation iOperation = (VirtualOperation) iMember;
            CustomOperation operation = new CustomOperation(this, iMember.name(), true);
            int col = 1;
            for (Parameter p: iOperation.inputParameter) {
              operation.addPort(1, col, true, false, p.type, p.name);
              col += 3;
            }
            col = 1;
            for (Parameter p: iOperation.outputParameter) {
              operation.addPort(5, col, false, true, p.type, p.name);
              col += 3;
            }
            if (makePublic) {
              operation.setPublic(true);
            }
            addOperation(operation);
          }
        }
      }
    }
    return incompatible;
  }
}
