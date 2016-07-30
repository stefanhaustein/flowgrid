package org.flowgrid.model.io;

import java.io.IOException;

public interface IOCallback<T> {
  void onSuccess(T value);
  void onError(IOException e);
}
