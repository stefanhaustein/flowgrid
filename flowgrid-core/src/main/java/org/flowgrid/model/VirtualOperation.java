package org.flowgrid.model;

import java.util.ArrayList;
import java.util.Map;

import org.flowgrid.model.api.PortCommand;
import org.flowgrid.model.hutn.HutnArray;
import org.flowgrid.model.hutn.HutnObject;
import org.flowgrid.model.hutn.HutnSerializer;

/**
 * An operation on an interface. 
 */
public class VirtualOperation extends Operation {
  ArrayList<Parameter> inputParameter = new ArrayList<Parameter>();
  ArrayList<Parameter> outputParameter = new ArrayList<Parameter>();
  
  public VirtualOperation(String name, Classifier classifier) {
    super(classifier, name);
  }

  public VirtualOperation addInputParameter(String name, Type type) {
    inputParameter.add(new Parameter(name, type));
    return this;
  }

  public void addInputParameter(int index, String name, Type type) {
    inputParameter.add(index, new Parameter(name, type));
  }

  public VirtualOperation addOutputParameter(int index, String name, Type type) {
    outputParameter.add(index, new Parameter(name, type));
    return this;
  }

  public void addOutputParameter(String name, Type type) {
    outputParameter.add(new Parameter(name, type));
  }

  @Override
  public void fromJson(HutnObject json, SerializationType serializationType, Map<Artifact, HutnObject> deferred) {
    super.fromJson(json, serializationType, deferred);
    paramsFromJson(json.getJsonArray("input"), inputParameter);
    paramsFromJson(json.getJsonArray("output"), outputParameter);
  }
  
  public Parameter inputParameter(int index) {
    return inputParameter.get(index);
  }

  public int inputCount() {
    return inputParameter.size() + 1;
  }

  public Parameter outputParameter(int index) {
    return outputParameter.get(index);
  }
  
  public int outputParameterCount() {
    return outputParameter.size();
  }
  
  public int inputParameterCount() {
    return inputParameter.size();
  }
  
  public int outputCount() {
    return outputParameter.size() + 1;
  }

  void paramsToJson(HutnSerializer writer, String name, ArrayList<Parameter> params) {
    writer.startArray(name);
    for (Parameter p: params) {
      writer.startObject();
      writer.writeString("name", p.name);
      writer.writeString("type", p.type.moduleLocalName(module));
      writer.endObject();
    }
    writer.endArray();
  }
  
  void paramsFromJson(HutnArray arr, ArrayList<Parameter> params) {
    for (int i = 0; i < arr.size(); i++) {
      HutnObject json = arr.getJsonObject(i);
      Type type = classifier.module().typeForName(json.getString("type", ""));
      params.add(new Parameter(json.getString("name"), type));
    }
  }
  
  public void removeInputParameter(int index) {
    inputParameter.remove(index);
  }

  public void removeOutputParameter(int index) {
    outputParameter.remove(index);
  }

  @Override
  public void toJson(HutnSerializer json, SerializationType serializationType) {
    super.toJson(json, serializationType);
    json.writeString("kind", "operation");
    paramsToJson(json, "output", outputParameter);
    paramsToJson(json, "input", inputParameter);
  }
  
  @Override
  public Type inputType(int index) {
    return index == 0 ? classifier : inputParameter(index - 1).type;
  }

  @Override
  public Type outputType(int index, Type[] inputSignature) {
    return index == 0 ? classifier : outputParameter(index - 1).type;
  }

  @Override
  public void process(Cell cell, Environment context, int remainingStackDepth) {
    Instance instance = (Instance) context.getData(cell.dataOffset);
    CustomOperation actualOperation = (CustomOperation) instance.classifier.operation(name());
    Environment child = new Environment(context.controller, context, actualOperation, instance, cell, 1);
    
    for (int i = 0; i < actualOperation.inputs.size(); i++) {
      PortCommand port = actualOperation.inputs.get(i);
      port.sendData(child, context.getData(cell.dataOffset + i + 1), remainingStackDepth);
    }
    context.sendData(cell.target(0), instance, remainingStackDepth);
  }

  public static class Parameter {
    public final String name;
    public final Type type;
    
    public Parameter(String name, Type type) {
      this.name = name;
      this.type = type;
    }
  }
}

