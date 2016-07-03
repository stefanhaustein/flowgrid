package org.flowgrid.model;

import java.io.IOException;
import java.io.InputStream;

import org.kobjects.filesystem.api.Filesystem;
import org.kobjects.filesystem.api.IOCallback;
import org.kobjects.filesystem.api.StatusListener;

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
   * Returns the file system used for storing data.
   */
  Filesystem storageFileSystem();
  
  /**
   * Returns the file system for storing meta data (that can be re-created
   * from the actual data). This should be a fast local file system.
   */
  Filesystem metadataFileSystem();

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
