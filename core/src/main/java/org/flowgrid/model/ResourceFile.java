package org.flowgrid.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.flowgrid.model.hutn.HutnSerializer;
import org.kobjects.filesystem.api.Filesystems;
import org.kobjects.filesystem.api.IOCallback;
import org.kobjects.filesystem.api.StatusListener;

public class ResourceFile extends Artifact implements ActionFactory, Command {
  // Not Type instances because the types are handled as Java types.
  public enum Kind {AUDIO, IMAGE};

  public static final String[] AUDIO_EXTENSIONS = {".wav", ".mp3"};
  public static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png"};
  private static final Action[] ACTIONS = {Action.GET};

  public final Kind kind;
  private Object resource;
  
  public static Kind kind(String name) {
    for (String ext: IMAGE_EXTENSIONS) {
      if (name.endsWith(ext)) {
        return Kind.IMAGE;
      }
    }
    for (String ext: AUDIO_EXTENSIONS) {
      if (name.endsWith(ext)) {
        return Kind.AUDIO;
      }
    }
    return null;
  }
  
  public ResourceFile(Module module, String name) {
    super(module, name);
    this.kind = kind(name);
  }

  @Override
  public Action[] actions() {
    return ACTIONS;
  }


  @Override
  public void copyData(String targetName, StatusListener statusListener) throws IOException {
    super.copyData(targetName, statusListener);
    Filesystems.copyAll(model().platform.storageFileSystem(), qualifiedName(),
        model().platform.storageFileSystem(), targetName, statusListener);
  }

  @Override
  public void deleteData(StatusListener statusListener) throws IOException {
    super.deleteData(statusListener);
    Filesystems.deleteAll(model().platform.storageFileSystem(), qualifiedName(), null);
  }


  public void resourceAsync(final IOCallback<Object> callback) {
    if (resource != null) {
      callback.onSuccess(resource);
    }
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          callback.onSuccess(resource());
        } catch (IOException e) {
          callback.onError(e);
        } catch (Exception e2) {
          callback.onError(new IOException(e2));
        }
      }
      
    }).start();
  }
  
  public Object resource() throws IOException {
    if (resource == null) {
      InputStream is = null;
      try {
        is = model().platform.storageFileSystem().load(qualifiedName());
        if (kind == Kind.IMAGE) {
          resource = model().platform.image(is);
        } else if (kind == Kind.AUDIO) {
          resource = model().platform.sound(is);
        } else {
          throw new IOException("Unrecognized resource type: " + qualifiedName());
        }
      } finally {
        Filesystems.close(is);
      }
    }
    return resource;
  }
  
  
  @Override
  public Command createCommand(Action action, boolean implicitInstance) {
    return this;
  }

  @Override
  public boolean matches(List<org.flowgrid.model.Type> inputTypes) {
    return true;
  }
  
  public Type type() {
    return kind == Kind.IMAGE ? model().typeForJavaType(Image.class, null) :
      kind == Kind.AUDIO ? model().typeForJavaType(Sound.class, null) : Type.ANY;
  }
  
  public double order() {
    return ORDER_RESOURCE;
  }

  @Override
  public void detach() {
  }

  @Override
  public int hasDynamicType() {
    return 0;
  }

  @Override
  public int inputCount() {
    return 0;
  }

  @Override
  public Type inputType(int index) {
    return Type.ANY;
  }

  @Override
  public int outputCount() {
    return 1;
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return type();
  }

  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    try {
      context.sendData(cell.target(0), resource(), remainingStackDepth);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void serializeCommand(HutnSerializer json, CustomOperation owner) {
    json.writeString("artifact", moduleLocalName(owner.module()));
  }

  @Override
  public Shape shape() {
    return Shape.STORAGE;
  }
  
  @Override
  public void toJson(HutnSerializer json, SerializationType serializationType) {
    super.toJson(json, serializationType);
    json.writeString("kind", "resource");
  }
}
