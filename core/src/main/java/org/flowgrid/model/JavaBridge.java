package org.flowgrid.model;

import org.flowgrid.model.annotation.Name;
import org.flowgrid.model.api.ControlFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Helpers for adding Java methods and classes.
 */
class JavaBridge {
  static String getJavaSignature(Method m) {
    StringBuilder sb = new StringBuilder(m.getName());
    sb.append('(');
    Class<?>[] params = m.getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(params[i].getSimpleName());
    }
    sb.append(')');
    return sb.toString();
  }

  static void addAllImpl(Module module, Classifier classifier, Class<?> javaClass, Class<?> firstArgFilter, String... names) {
    HashMap<String,String> filter = null;
    if (names.length != 0) {
      filter = new HashMap<String,String>();
      for (String name: names) {
        int cut = name.indexOf(':');
        if (cut == -1) {
          filter.put(name, name);
        } else {
          filter.put(name.substring(0, cut), name.substring(cut + 1));
        }
      }
    }
    for (Method method: javaClass.getMethods()) {
      if (!JavaOperation.isSupported(module.model(), method, firstArgFilter, filter == null)) {
    //    Log.d("FlowGrid", "Not supported: " + method);
        continue;
      }
      String name;
      if (method.getAnnotation(Name.class) != null || filter == null) {
        name = JavaOperation.methodName(method);
      } else {
        String signature = getJavaSignature(method);
        name = filter.get(signature);
        if (name == null) {
          name = filter.get(method.getName());
        }
        if (name == null) {
       //   Log.d("FlowGrid", "Not mapped: " + signature);
          continue;
        }
        int cut = name.indexOf('(');
        if (cut != -1) {
          name = name.substring(0, cut);
        }
      }

      // We don't support turning static Java functions into FG members,
      // but we do support turning java methods int standalone FG functions.
      // We use the former for string
      if (Modifier.isStatic(method.getModifiers()) && classifier != null) {
        continue;
      }

      Class<?>[] paramTypes = method.getParameterTypes();
      int effectiveParamCount = paramTypes.length + (((method.getModifiers() & Modifier.STATIC) != 0) ? 0 : 1);
      if (classifier == null && method.getReturnType() == Boolean.TYPE &&
          effectiveParamCount <= 2 && effectiveParamCount > 0 &&
          (paramTypes.length == 0 || paramTypes[0] != Boolean.TYPE)) {
        module.addArtifact(new ControlFactory(module, name,
            new JavaOperation(module, method, name, ActionFactory.Action.COMPUTE),
            new JavaOperation(module, method, name, ActionFactory.Action.FILTER),
            new JavaOperation(module, method, name, ActionFactory.Action.SWITCH)));
      } else {
        // Name is guaranteed to be not null here.
        JavaOperation javaOperation = new JavaOperation(classifier != null ? classifier : module, method, name);

        if (classifier != null) {
          classifier.addOperation(javaOperation);
        } else {
          module.addArtifact(javaOperation);
        }
      }
    }
  }
}
