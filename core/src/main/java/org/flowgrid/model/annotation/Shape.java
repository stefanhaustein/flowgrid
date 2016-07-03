package org.flowgrid.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Shape {
  org.flowgrid.model.Shape value();
}
