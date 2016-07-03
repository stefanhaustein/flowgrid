package org.flowgrid.model;

@SuppressWarnings("serial")
public class FlowGridException extends RuntimeException {
  final CustomOperation operation;
  final Cell cell;
  
  public FlowGridException(String message, CustomOperation operation, Cell cell, Throwable cause) {
    super(message, cause);
    this.operation = operation;
    this.cell = cell;
  }
  
  public Cell cell() {
    return cell;
  }
  
  public CustomOperation operation() {
    return operation;
  }
}
