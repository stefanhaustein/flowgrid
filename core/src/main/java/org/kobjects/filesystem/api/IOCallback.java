package org.kobjects.filesystem.api;

import java.io.IOException;

public interface IOCallback<T> {
  void onSuccess(T value);
  void onError(IOException e);
}
