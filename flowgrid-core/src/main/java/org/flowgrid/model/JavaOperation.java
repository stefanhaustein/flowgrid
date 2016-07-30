package org.flowgrid.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.flowgrid.model.Model.JavaTypeSupportLevel;
import org.flowgrid.model.annotation.Blocking;
import org.flowgrid.model.annotation.MultipleResults;
import org.flowgrid.model.annotation.Name;
import org.flowgrid.model.hutn.HutnSerializer;


public class JavaOperation extends Operation {
  enum ResultHandling {
    RETURN_THIS_AND_RESULT, VOID, RETURN_THIS_ONLY, RETURN_RESULT, RETURN_SPLIT_RESULT, FILTER, SWITCH, COMPUTE
  };

  private final Method method;
  private final Type[] inputSignature;
  private final Type[] outputSignature;
  /** bit pattern, not counting self */
  private int inputNeedsConversion;
  private boolean outputNeedsConversion;
  private final Class<?>[] javaParameterTypes;

  private final ResultHandling resultHandling;
  private boolean blocking;
  
  private static Object toJava(Class<?> javaType, Object value) {
    if (javaType == Integer.class || javaType == Integer.TYPE) {
      return Integer.valueOf(((Double) value).intValue());
    }
    return value;
  }
  
  private static Object fromJava(Object value) {
    if (value instanceof Integer) {
      return Double.valueOf(((Integer) value).intValue());
    }
    return value;
  }

  public JavaOperation(Container owner, Method method) {
    this(owner, method, methodName(method), null);
  }

  public JavaOperation(Container owner, Method method, String name) {
    this(owner, method, name, null);
  }

  public JavaOperation(Container owner, Method method, String name, Action action) {
    super(owner, name);
    this.method = method;
    boolean isJavaMethod = !Modifier.isStatic(method.getModifiers());
    this.isPublic = true;
    Model model = module.model();
    javaParameterTypes = method.getParameterTypes();
    int targetOffset;
    if (isJavaMethod) {
      inputSignature = new Type[javaParameterTypes.length + 1];
      inputSignature[0] = model.typeForJavaType(method.getDeclaringClass(), null);
      targetOffset = 1;
    } else {
      inputSignature = new Type[javaParameterTypes.length];
      targetOffset = 0;
    }
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    for (int i = 0; i < javaParameterTypes.length; i++) {
      Type parameterType = null;
      if (parameterType == null) {
        inputSignature[i + targetOffset] = model.typeForJavaType(javaParameterTypes[i], parameterAnnotations[i]);
        inputNeedsConversion |= model.javaTypeSupportLevel(javaParameterTypes[i], parameterAnnotations[i]) ==
            JavaTypeSupportLevel.CONVERT ? 1 << i : 0;
      } else {
        inputSignature[i + targetOffset] = parameterType;
      }
    }

    boolean fgMethod = classifier != null;
    if (action != null && action != Action.INVOKE) {
      outputNeedsConversion = false;
      switch (action) {
        case SWITCH:
          outputSignature = new Type[]{inputSignature[0], inputSignature[0]};
          resultHandling = ResultHandling.SWITCH;
          break;
        case FILTER:
          outputSignature = new Type[]{inputSignature[0]};
          resultHandling = ResultHandling.FILTER;
          break;
        case COMPUTE:
          outputSignature = new Type[]{PrimitiveType.BOOLEAN};
          resultHandling = ResultHandling.COMPUTE;
          break;
        default:
          throw new IllegalArgumentException("Unsupported action: " + action);
      }
    } else if (method.getReturnType() == Void.TYPE) {
      outputSignature = fgMethod ? new Type[]{inputSignature[0]}:Type.EMPTY_ARRAY;
      this.resultHandling = fgMethod ? ResultHandling.RETURN_THIS_ONLY : ResultHandling.VOID;
      outputNeedsConversion = false;
    } else if (method.getAnnotation(MultipleResults.class) != null) {
      if (fgMethod) {
        throw new IllegalArgumentException("MultipleResults not supported for methods.");
      }
      MultipleResults multipleResults = method.getAnnotation(MultipleResults.class);
      outputNeedsConversion = false;
      Type type = model.typeForJavaType(method.getReturnType().getComponentType(), null);
      outputSignature = new Type[multipleResults.value()];
      for (int i = 0; i < outputSignature.length; i++) {
        outputSignature[i] = type;
      }
      this.resultHandling = ResultHandling.RETURN_SPLIT_RESULT;
    } else {
      outputNeedsConversion = model.javaTypeSupportLevel(method.getGenericReturnType(), method.getAnnotations()) ==
          JavaTypeSupportLevel.CONVERT;
      if (fgMethod) {
        outputSignature = new Type[]{inputSignature[0], model.typeForJavaType(method.getGenericReturnType(), method.getAnnotations())};
        this.resultHandling = ResultHandling.RETURN_THIS_AND_RESULT;
      } else {
        outputSignature = new Type[]{model.typeForJavaType(method.getGenericReturnType(), method.getAnnotations())};
        this.resultHandling = ResultHandling.RETURN_RESULT;
      }
    }
    blocking = method.getAnnotation(Blocking.class) != null;
  }

  public static String methodName(Method method) {
    Name nameAnnotation = method.getAnnotation(Name.class);
    return nameAnnotation != null ? nameAnnotation.value() : method.getName();
  }

  public double order() {
    if (name.length() == 1 && !Character.isLetterOrDigit(name.charAt(0))) {
      return ORDER_OPERATOR;
    }
    return ORDER_OPERATION;
  }

  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    try {
      Object result;
      Object receiver;
      Object[] args;
      if (Modifier.isStatic(method.getModifiers())) {
        receiver = null;
        args = new Object[inputSignature.length];
        for (int i = 0; i < args.length; i++) {
          args[i] = context.getData(cell.dataOffset + i);
        }
      } else {
        receiver = context.getData(cell.dataOffset);
        args = new Object[inputSignature.length - 1];
        for (int i = 0; i < args.length; i++) {
          args[i] = context.getData(cell.dataOffset + i + 1);
        }
      }
      if (inputNeedsConversion != 0) {
        for (int i = 0; i < args.length; i++) {
          if ((inputNeedsConversion & (1 << i)) != 0) {
            args[i] = toJava(javaParameterTypes[i], args[i]);
          }
        }
      }
      result = method.invoke(receiver, args);
      if (blocking) {
        remainingStackDepth = 0;
      }
      if (outputNeedsConversion) {
        result = fromJava(result);
      }
      switch (resultHandling) {
        case VOID:
          break;
        case COMPUTE:
        case RETURN_RESULT:
          context.sendData(cell.target(0), result, remainingStackDepth);
          break;
        case RETURN_THIS_ONLY:
          context.sendData(cell.target(0), receiver, remainingStackDepth);
          break;
        case RETURN_THIS_AND_RESULT:
          context.sendData(cell.target(1), result, remainingStackDepth);
          context.sendData(cell.target(0), receiver, remainingStackDepth);
          break;
        case RETURN_SPLIT_RESULT:
          Object[] resultArray = (Object[]) result;
          for (int i = 0; i < resultArray.length; i++) {
            context.sendData(cell.target(i), resultArray[i], remainingStackDepth);
          }
          break;
        case SWITCH:
          context.sendData(cell.target(Boolean.TRUE.equals(result) ? 0 : 1), context.getData(cell.dataOffset), remainingStackDepth);
          break;
        case FILTER:
          if (Boolean.TRUE.equals(result)) {
            context.sendData(cell.target(0), context.getData(cell.dataOffset), remainingStackDepth);
          }
          break;
        default:
          throw new IllegalArgumentException("Illegal result handling: " + resultHandling);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Shape shape() {
    org.flowgrid.model.annotation.Shape shapeAnnotation = method.getAnnotation(org.flowgrid.model.annotation.Shape.class);
    return shapeAnnotation != null ? shapeAnnotation.value() :
        resultHandling == ResultHandling.SWITCH ? Shape.RHOMBUS
        : resultHandling == ResultHandling.FILTER ? Shape.TRAPEZOID_DOWN
            : resultHandling == ResultHandling.COMPUTE ? Shape.HEXAGON
        : super.shape();
  }

  public static boolean isSupported(Model model, Method m, Class<?> firstArgFilter, boolean strict) {
    if (!Modifier.isPublic(m.getModifiers())) {
      return false;
    }
    if (m.getGenericReturnType() != Void.TYPE) {
      JavaTypeSupportLevel support = model.javaTypeSupportLevel(m.getReturnType(), m.getAnnotations());
      if (support == JavaTypeSupportLevel.UNSUPPORTED || (strict && support == JavaTypeSupportLevel.AVOID)) {
        return false;
      }
    }
    Class<?>[] parameterTypes = m.getParameterTypes();
    Annotation[][] parameterAnnotations = m.getParameterAnnotations();
    java.lang.reflect.Type[] genericParameterTypes = m.getGenericParameterTypes();
    for (int i = 0; i < genericParameterTypes.length; i++) {
      java.lang.reflect.Type pt = genericParameterTypes[i];
      JavaTypeSupportLevel support = model.javaTypeSupportLevel(pt, parameterAnnotations[i]);
      if (support == JavaTypeSupportLevel.UNSUPPORTED || (strict && support == JavaTypeSupportLevel.AVOID)) {
        return false;
      }
    }
    if (firstArgFilter != null) {
      if (Modifier.isStatic(m.getModifiers())) {
        if (parameterTypes.length == 0 ||
            !parameterTypes[0].isAssignableFrom(firstArgFilter)) {
          return false;
        }
      } else { // not static
        // Skip generic toString and hashCode (for now)
        if (!m.getDeclaringClass().equals(firstArgFilter)) {
          return false;
        }
      }
    } else {
      if (m.getDeclaringClass().equals(Object.class)) {
        return false;
      }
    }

    if (!Modifier.isStatic(m.getModifiers()) &&
        model.javaTypeSupportLevel(m.getDeclaringClass(), null) != JavaTypeSupportLevel.FULL) {
      return false;
    }

    return true;
  }
  
  @Override
  public int hasDynamicType() {
    return 0;
  }

  /*
    public boolean isMethod() {
      return classifier != null;
    }
  */
  @Override
  public int inputCount() {
    return inputSignature.length;
  }

  @Override
  public int outputCount() {
    return outputSignature.length;
  }

  @Override
  public Type inputType(int index) {
    return inputSignature[index];
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return outputSignature[index];
  }

  @Override
  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    super.serializeCommand(json, owner);
    if (resultHandling == ResultHandling.SWITCH){
      json.writeString("action", Action.SWITCH.toString());
    } else if (resultHandling == ResultHandling.FILTER) {
      json.writeString("action", Action.FILTER.toString());
    } else if (resultHandling == ResultHandling.COMPUTE) {
      json.writeString("action", Action.COMPUTE.toString());
    }
  }

}
