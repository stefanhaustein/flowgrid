package org.flowgrid.model.api;


import org.flowgrid.model.Callback;
import org.flowgrid.model.Image;
import org.flowgrid.model.JavaOperation;
import org.flowgrid.model.Model;
import org.flowgrid.model.Module;
import org.flowgrid.model.PrimitiveType;
import org.flowgrid.model.Sound;

public class ApiSetup extends Callback<Model> {

  @Override
  public void run(Model model) {
    Module rootModule = model.rootModule;

    Module containerModule = rootModule.systemModule("container");
    Module logicModule = rootModule.systemModule("logic");
    Module systemModule = rootModule.systemModule("system");
    Module textModule = rootModule.systemModule("text");

    Module controlModule = rootModule.systemModule("control");
    Module branchModule = controlModule.systemModule("branch");
    Module compareModule = controlModule.systemModule("compare");

    Module graphicsModule = rootModule.systemModule("graphics");
    Module colorModule = graphicsModule.systemModule("color");
    Module soundModule = rootModule.systemModule("sound");

    Module mathModule = rootModule.systemModule("math");
    Module degreeModule = mathModule.systemModule("degree");
    Module radianModule = mathModule.systemModule("radian");
    Module vectorModule = mathModule.systemModule("vector");

    systemModule.addArtifact(new LiteralCommandFactory(systemModule));
    systemModule.addArtifact(new PortCommandFactory(systemModule));

    controlModule.addArtifact(new LogOperation(controlModule));
    controlModule.addArtifact(new SyncOperation(controlModule));
    controlModule.addArtifact(new IfFactory(controlModule));
    controlModule.addArtifact(new LoopOperation(controlModule));

    RelationalControlFactory.register(compareModule);
    BranchOperation.register(branchModule);

    colorModule.addArtifact(PrimitiveType.COLOR);
    logicModule.addJavaOperations(BooleanOperations.class, Boolean.TYPE);

    mathModule.addJavaOperations(MathOperations.class, null);
    mathModule.addJavaOperations(Math.class, Double.TYPE,
        "abs(double)", "ceil(double)", "exp(double)", "floor(double)",
        "log(double)", "log10(double)", "max(double,double)", "min(double,double)",
        "pow(double,double)", "signum(double)", "sqrt(double)");

    radianModule.addJavaOperations(Math.class, Double.TYPE,
        "acos(double)", "asin(double)", "atan(double)", "atan2(double,double)",
        "cos(double)", "cosh(double)", "sin(double)", "sinh(double)",
        "tan(double)", "tanh(double)", "toDegrees(double)");
    degreeModule.addJavaOperations(DegreeOperations.class, null);

    textModule.addJavaOperations(String.class, String.class,
        "concat", "endsWith(String)", "equalsIgnoreCase",
        "hashCode", "indexOf", "isEmpty", "lastIndexOf", "length", "replaceAll",
        "replaceFirst", "split", "startsWith(String)",
        "substring(int):from", "substring(int,int):substring",
        "toLowerCase", "toUpperCase", "trim");

    colorModule.addJavaOperations(Color.class, null);

    containerModule.addJavaOperations(ArrayOperations.class, null);
    vectorModule.addJavaOperations(VectorOps.class, null);
    try {
      textModule.addArtifact(new JavaOperation(textModule,
          model.getClass().getDeclaredMethod("toString", Object.class)));
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    graphicsModule.addJavaClass("Image", Image.class);
    soundModule.addJavaClass("Sound", Sound.class);
  }
}
