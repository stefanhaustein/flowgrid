# FlowGrid

FlowGrid is a visual dataflow programming environment for Android. The main difference to other
visual programming apps such as [Scratch](https://scratch.mit.edu/) is that FlowGrid is based on
modelling data flow directly instead of traditional structured programming.

Basically, instead of using an indirection via variables or registers, the flow of information
through the computer is modelled in a graph, making programs look more like flow chart diagrams.
At the top level, this flow can be easily visualized, potentially making programming also more
accessible.


## Language Concepts


### Operations

The most important concept in FlowGrid is an operation. An operation typically has a set of inputs
and output and performs a transformation between them. Operations do so by connecting other
operations in various ways.

A straightforward example is examples/simple/sqr: This operation
computes the square of a number by multiplying it with itself.


#### Continuous Input

In FlowGrid, an operation usually waits for all input values to be available, and only then
consumes them and executes. This is the default and the case for all builtin operations.

An operation with continuous input receives all input directly as it arrives, i.e. it acts
like its contents were inserted in place, similar to a macro in traditional programming.

Continuous operations preserve their state as long as the calling operation is running.
They are typically used for the top level entry point -- or where state needs to be maintained
between an ordered sequence of input data.


#### Buffers and Constant Values

Operations without continuous input can have input buffers attached. Input buffers store the
last supplied value, allowing the operation to execute with the buffered value when sufficient
other new input is provided. Input buffers can be attached via the context menu by tapping
on the corresponding input in the operation editor.


### Classes

Classes encapsulate data and operations working on this data.


### Interfaces

Interfaces are class signatures without an implementation (but in contrast to most other languages,
they may have data fields).  Like in Go, classes implicitly implement interfaces if the signatures
match.


## Android-Specific Features

### Arduino Connection

To control an Arduino board via FlowGrid, please install
[StandardFirmata](https://github.com/firmata/arduino#usage) on the Arduino board.

Please find Arduino IO connectors in the editor context menu under `Data / IO` -> `Firmata`.



## Copyright

(c) 2016 Stefan Haustein, Zurich, Switzerland.

Thanks to Janine Breitbarth, Pat Coleman, Mareike Klisch, Jerry Morrison and Mario Zechner
for inspiration and help with testing this app.
