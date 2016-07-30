package org.flowgrid.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.flowgrid.model.io.IOCallback;
import org.flowgrid.model.io.StatusListener;

public interface Platform extends StatusListener {
  /**
   * Create an image from the given stream.
   */
  Image image(InputStream is) throws IOException;

  /**
   * Create an sound object from the given stream.
   */
  Sound sound(InputStream is) throws IOException;
  
  /**
   * Returns a callback to register platform builtins. Called from the
   * model constructor to set up builtins before the model is loaded.
   */
  Callback<Model> platformApiSetup();

  /**
   * Returns the root directory for storing data.
   */
  File storageRoot();

  /**
   * Returns the root directory for caching data.
   */
  File cacheRoot();

  /**
   * Returns a void IO callback that logs the given message and 
   * calls fatalError on failure.
   * 
   * @param message
   * @return
   */
  IOCallback<Void> defaultIoCallback(String message);

  /**
   * Returns a string describing the current hardware platform.
   * May be useful for debugging distributed data corruption
   * issues.
   */
  String platformId();
  
  /**
   * Called in case of an error that may not be recoverable.
   * Should display a dialog that offers doing nothing, 
   * going to the main activity and leaving the app.
   */
  void error(String message, Exception e);
  
  /**
   * Shows the given message in the UI in a non-obtrusive 
   * way (e.g. as a toast on android) and logs it for 
   * reference.
   */
  void info(String message, Exception e);
  
  /**
   * Logs a message; originally declared in StatusListener.
   */
  void log(String message, Exception e);
}
