package org.flowgrid.model;

/**
 * All type-related methods are concentrated here (opposed to having some of them directly
 * on Type) for improved clarity of what they actually do.
 */
public class Types {
  public static Type commonSuperType(Type t1, Type t2) {
    // TODO(haustein) Improve.
    return t1.equals(t2) ? t1 : Type.ANY;
  }

  public static boolean isPrimitive(Type t) {
    return t instanceof PrimitiveType;
  }

  public static boolean isArray(Type type) {
    return type instanceof ArrayType;
  }

  public static boolean isAbstract(Type type) {
    return type == Type.ANY
        || ((type instanceof Classifier) && !((Classifier) type).isInstantiable())
        || (isArray(type) && Types.isAbstract(((ArrayType) type).elementType));
  }

  public static boolean isInstantiable(Type type) {
    return !isAbstract(type);
  }

  public static boolean isInterface(Type type) {
    return type instanceof Classifier && ((Classifier) type).isInterface();
  }

  private static boolean hasImplementation(Module module, Classifier target) {
    for(Artifact child: module) {
      if (child instanceof Module) {
        boolean result = hasImplementation((Module) child, target);
        if (result) {
          return true;
        }
      }
      if (child instanceof Classifier) {
        Classifier classifier = (Classifier) child;
        if (isInstantiable(classifier) && target.isAssignableFrom(classifier)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasInstantiableImplementation(Type type) {
    if (!isInterface(type)) {
      return isInstantiable(type);
    }
    Classifier classifier = (Classifier) type;
    return hasImplementation(classifier.model().rootModule, classifier);
  }
}
