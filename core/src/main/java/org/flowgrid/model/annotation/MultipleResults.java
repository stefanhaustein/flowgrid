package org.flowgrid.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by haustein on 05.08.15.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MultipleResults {
  int value();
}
