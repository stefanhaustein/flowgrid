package org.flowgrid.android.api;

import org.flowgrid.android.MainActivity;
import org.flowgrid.model.Callback;
import org.flowgrid.model.Classifier;
import org.flowgrid.model.JavaOperation;
import org.flowgrid.model.Model;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;


public class AndroidApiSetup extends Callback<Model> {

  private final MainActivity platform;

  public AndroidApiSetup(MainActivity platform) {
    this.platform = platform;
  }

  @Override
  public void run(Model model) {
    try {
      Module root = model.rootModule;
      Module graphicsModule = root.systemModule("graphics");
      Module spriteModule = graphicsModule.systemModule("sprite");
      Module soundModule = root.systemModule("sound");
      Module controlModule = root.systemModule("control");
      Module emojiModule = controlModule.systemModule("emoji");

      EmojiControlFactory.register(emojiModule);

      JavaOperation cmd = new JavaOperation(soundModule, Tone.class.getMethod("newTone", Double.TYPE, Double.TYPE));
      soundModule.addArtifact(cmd);

      Classifier graphicsClass = graphicsModule.addJavaClass("Graphics", Graphics.class);

      spriteModule.addBuiltinInterface("Box")
          .addPublicProperty("x", PrimitiveType.NUMBER, 0.0)
          .addPublicProperty("y", PrimitiveType.NUMBER, 0.0)
          .addPublicProperty("width", PrimitiveType.NUMBER, 100.0)
          .addPublicProperty("height", PrimitiveType.NUMBER, 100.0);

      spriteModule.addBuiltinInterface("Disc")
          .addPublicProperty("x", PrimitiveType.NUMBER, 0.0)
          .addPublicProperty("y", PrimitiveType.NUMBER, 0.0)
          .addPublicProperty("radius", PrimitiveType.NUMBER, 100.0);

      spriteModule.addBuiltinInterface("Drawable")
          .addPublicProperty("x", PrimitiveType.NUMBER, 0.0)
          .addPublicProperty("y", PrimitiveType.NUMBER, 0.0)
          .addPublicProperty("width", PrimitiveType.NUMBER, 100.0)
          .addPublicProperty("height", PrimitiveType.NUMBER, 100.0)
          .addVirtualOperation("draw").addInputParameter(0, "draw", graphicsClass);

      spriteModule.addBuiltinInterface("Placeable")
          .addPublicProperty("x", PrimitiveType.NUMBER, 0.0)
          .addPublicProperty("y", PrimitiveType.NUMBER, 0.0);

      spriteModule.addBuiltinInterface("Sprite")
          .addPublicProperty("x", PrimitiveType.NUMBER, 0.0)
          .addPublicProperty("y", PrimitiveType.NUMBER, 0.0)
          .addPublicProperty("image", model.typeForName("/graphics/Image"), null);

      spriteModule.addBuiltinInterface("OnDrag").addVirtualOperation("onDrag")
          .addInputParameter("dx", PrimitiveType.NUMBER)
          .addInputParameter("dy", PrimitiveType.NUMBER);

      graphicsModule.addBuiltinInterface("OnClick").addVirtualOperation("onClick")
          .addInputParameter("x", PrimitiveType.NUMBER)
          .addInputParameter("y", PrimitiveType.NUMBER);


      Classifier canvasClass = graphicsModule.addJavaClass("Canvas", CanvasView.class);

      spriteModule.addBuiltinInterface("OnAttach")
          .addVirtualOperation("onAttach").addInputParameter("canvas", canvasClass);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
