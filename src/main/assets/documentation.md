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


#### Synchronized vs. Continuous Input

In FlowGrid, “synchronized input” means that an operation waits for all input values to be
available, and only then consumes them and executes. This is the default and the case for all
builtin operations.

A continuous operation just passes on any input directly, i.e. it acts like its contents were
inserted in place, similar to a macro in traditional programming.

Continuous operations preserve their state as long as the calling operation is running.


#### Buffers and Constant Values

Operations with synchronized input can have input buffers attached. Input buffers store the
last supplied value, allowing the operation to execute with the buffered value when sufficient
other new input is provided.


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


## User Interface

### Operation Editor

Add functional blocks by tapping anywhere in the grid, bringing up a context menu.
The `edit` sub-menu contains options to insert and delete columns, rows and previously
copied areas.

Connect operations by dragging connections out of the bottom of the corresponding blocks.

A long press triggers selection mode. Touch the screen for a while until the selection controls
show up, then move (without releasing) to select a rectangular area.


#### Context Menu

The context menu allows you to modify the current operation at the current touch position
by adding, changing or removing operational blocks.


##### Data / IO

This context menu option allows you to add constant values and various types of input
and output connectors.


###### Constant value

Opens a dialog for inserting a constant in the selected field.


###### Input field

Adds a regular input field to the UI and an operator that providese the corresponding value.
If the operation is called from another operation, all input fields become operation
parameters.


###### Combined field

Adds an input field to the UI where the current value can be overwritten in code. When
this operation is used form another operation, this adds an input and output parameter.
This probably makes only sense for continuous operations.


###### Sensor

Adds an operator that provides phone sensor data (such as accelleration and orientation).
This probably makes only sense for continuous operations. As for other inputs, this becomes
a regular parameter when the operation is called from another operation.


###### Output

Adds an output element to the UI (e.g. a text field). When this operation is used from
another operation, all output elements (except for Canvas) become return values.


####### Output field

Adds a regular text bases output field to the UI. When the operation is used from another
operation, all output fields become return values.


####### Canvas

Adds a graphical canvas to the UI. In contrast to other output fields, this emits a reference
to the canvas that can be used for drawing and callback registration. Like other IO operators,
this turns into a parameter when the operation containing the canvas is called from another
operation.


####### Histogram

Adds a graphical output element that visualizes the distribution of input values. Like other
ouput elements, this becomes a return parameter when the containing operation is called
from another operation.


####### Percent bar

Adds a graphical output element that visualizes a value between 0 and 100 in the form of
a bar.


####### Run chart

Adds a graphical output element that visualizes numerical input values over time. Like other
ouput elements, this becomes a return parameter when the containing operation is called
from another operation.


####### Web view

Adds a web view to the UI. If a URL is supplied to this operator, the corresponding web
page is rendered in the web view. If the user changes the URL (following a link), the
URL is sent as an output value of the operator.

When the containing operation is called from another operation, this turns into an input
and output parameter.


###### Firmata

Receives or sends digital or analog data to a device connected via USB using the Firmata
protocol; i.e. typically an Arduino board. As for other IO elements, the corresponding
operators turn into parameters when this operation is used from other operations.


###### Test

The operators in this sub-menu sends test input or check test output.


##### Control

This context menu option contains control flow and comparison operations.


##### This

Provices access to properties and operations of the local class with an implicit `this`
reference. The `this` operator available here turns the implicit `this` reference into an
explicit one.


##### This module

This option is a short cut to local modules, which are also contained in the
`Operations / classes` option.


##### Operations / classes

This option provides access to all regular built-in and user provided classes and operations,
except for IO connectors and control structures available via the `Data / IO` and `Control`
menu options.


##### Edit

This sub-menu contains options to insert and delete columns, rows and previously
copied areas.




## System Library


### /graphics

Graphics classes, interfaces and operations.


#### /graphics/sprite/Placeable

An object that can be added to the canvas. It will be rendered at the position specified
by the x and y properties.


#### /graphics/sprite/Sprite

An object that can be added to the canvas. It will render its image at the position 
specified by the x and y properties.


### /logic

Logic operations such as "and", "or" and "not".


### /math

Mathematical operations, including formatting operations. Trigonometrical operations are
contained in subpackages.


### /text

Text string operations.


## Copyright

(c) 2016 Stefan Haustein, Zurich, Switzerland.

Thanks to Jerry Morrison, Mareike Klisch and Pat Coleman for inspiration
and help with testing this app.
