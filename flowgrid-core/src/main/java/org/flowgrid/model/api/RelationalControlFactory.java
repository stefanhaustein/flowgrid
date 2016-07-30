package org.flowgrid.model.api;

import org.flowgrid.model.Artifact;
import org.flowgrid.model.Environment;
import org.flowgrid.model.Model;
import org.flowgrid.model.Module;
import org.flowgrid.model.Type;


public class RelationalControlFactory extends ControlFactory {

  public static void register(Module module) {
    Artifact artifact2 = new RelationalControlFactory(module, "\u2260", -1, 1);
    module.addArtifact(artifact2);
    module.addArtifact(new RelationalControlFactory(module, "=", 0, 0));
    Artifact artifact1 = new RelationalControlFactory(module, "\u2264", -1, 0);
    module.addArtifact(artifact1);
    Artifact artifact = new RelationalControlFactory(module, "<", -1, -1);
    module.addArtifact(artifact);
    module.addArtifact(new RelationalControlFactory(module, ">", 1, 1));
    module.addArtifact(new RelationalControlFactory(module, "\u2265", 1, 0));
  }

  RelationalControlFactory(Module module, String name, final int expected1, final int expected2) {
    super(module, name, new Switchable() {
      @Override
      protected boolean process(Object left, Environment environment, int dataOffset) {
        Object right = environment.getData(dataOffset + 1);
        int cmp = ((Comparable) left).compareTo(right);
        return cmp == expected1 || cmp == expected2;
      }

      @Override
      public int inputCount() {
        return 2;
      }

      @Override
      public Type inputType(int index) {
        return Type.ANY;
      }
    });
  }
}
