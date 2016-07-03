package org.flowgrid.model;


/**
 * Type must be an interface because Classifier iplements it, but is already a subclass of
 * Container.
 */
public interface Type extends ActionFactory {
  public static final Type[] EMPTY_ARRAY = new Type[0];
  public static final AbstractType ANY = new AbstractType("Any", Object.class) {
    @Override
    public String qualifiedName() {
      return "/system/Any";
    }
  };

  String name();
  String qualifiedName();
  String moduleLocalName(Module module);

  boolean isAssignableFrom(Type type);
  public Class<?> javaClass();
}
