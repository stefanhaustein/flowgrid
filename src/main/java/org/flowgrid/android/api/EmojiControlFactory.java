package org.flowgrid.android.api;

import java.util.List;
import java.util.Locale;

import org.flowgrid.model.Environment;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Type;
import org.flowgrid.model.api.ControlFactory;
import org.flowgrid.model.api.Switchable;
import org.kobjects.emoji.Emoji;

public class EmojiControlFactory extends ControlFactory {

  public static void register(Module emojiModule) {
    for (Emoji.Category category : Emoji.Category.values()) {
      Module module = emojiModule.systemModule(category.name().toLowerCase(Locale.US));
      for (Emoji.Property property: category) {
        module.addArtifact(new EmojiControlFactory(module, property));
      }
    }
  }

  private EmojiControlFactory(Module module, final Emoji.Property property) {
    super(module, property.toString(), new Switchable() {
      @Override
      protected boolean process(Object input, Environment environment, int dataOffset) {
        return Emoji.contains((String) input, property);
      }

      @Override
      public int inputCount() {
        return 1;
      }

      @Override
      public Type inputType(int index) {
        return PrimitiveType.TEXT;
      }
    });
  }

  @Override
  public boolean matches(List<Type> inputTypes) {
    return inputTypes.size() == 0 || inputTypes.get(0) == PrimitiveType.TEXT;
  }
}
