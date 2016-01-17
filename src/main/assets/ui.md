## User Interface

## Operation Editor

Add functional blocks by tapping anywhere in the grid, bringing up a context menu.
The `edit` sub-menu contains options to insert and delete columns, rows and previously
copied areas.

Connect operations by dragging connections out of the bottom of the corresponding blocks.

A long press triggers selection mode. Touch the screen for a while until the selection controls
show up, then move (without releasing) to select a rectangular area.


### Context Menu

The context menu allows you to modify the current operation at the current touch position
by adding, changing or removing operational blocks. The current touch position is highlighted
in light blue to confirm the selected location.


#### Add Buffer

Add a buffer to the selected input. Available when an unbuffered input for a rgular
operation (=non-continuous) is selected.


#### Remove Buffer

Removes a buffer (including potentially contained constants). Only shown if
an input connection with a buffer or constant is selected.


#### Data / IO

Add constant values and various types of input and output connectors.


##### Constant value

Opens a dialog for inserting a constant in the selected field.


###### Compute

Compute a boolean result for the selected condition or boolean operation.


###### Filter

Filter the primary input value according to the selected condition or boolean operation:
The primary input value is passed to the output only if the condition is true.


###### Switch

Switch the primary input value according to the selected condition or boolean operation:
If the condition is true, the primary input value is passed to the left output, otherwise
it is passed to the right output.


##### Input field

Adds a regular input field to the UI and an operator that providese the corresponding value.
If the operation is called from another operation, all input fields become operation
parameters.


##### Combined field

Adds an input field to the UI where the current value can be overwritten in code. When
this operation is used form another operation, this adds an input and output parameter.
This probably makes only sense for continuous operations.


##### Sensor

Adds an operator that provides phone sensor data (such as accelleration and orientation).
This probably makes only sense for continuous operations. As for other inputs, this becomes
a regular parameter when the operation is called from another operation.


##### Output

Adds an output element to the UI (e.g. a text field). When this operation is used from
another operation, all output elements (except for Canvas) become return values.


###### Output field

Adds a regular text bases output field to the UI. When the operation is used from another
operation, all output fields become return values.


###### Canvas

Adds a graphical canvas to the UI. In contrast to other output fields, this emits a reference
to the canvas that can be used for drawing and callback registration. Like other IO operators,
this turns into a parameter when the operation containing the canvas is called from another
operation.


###### Histogram

Adds a graphical output element that visualizes the distribution of input values. Like other
ouput elements, this becomes a return parameter when the containing operation is called
from another operation.


###### Percent bar

Adds a graphical output element that visualizes a value between 0 and 100 in the form of
a bar.


###### Run chart

Adds a graphical output element that visualizes numerical input values over time. Like other
ouput elements, this becomes a return parameter when the containing operation is called
from another operation.


###### Web view

Adds a web view to the UI. If a URL is supplied to this operator, the corresponding web
page is rendered in the web view. If the user changes the URL (following a link), the
URL is sent as an output value of the operator.

When the containing operation is called from another operation, this turns into an input
and output parameter.


##### Firmata

Receives or sends digital or analog data to a device connected via USB using the Firmata
protocol; i.e. typically an Arduino board. As for other IO elements, the corresponding
operators turn into parameters when this operation is used from other operations.


##### Test

The operators in this sub-menu sends test input or check test output.


#### Control

This context menu option contains control flow and comparison operations.


#### This

Provides access to properties and operations of the local class with an implicit `this`
reference. The `this` operator available here turns the implicit `this` reference into an
explicit one.


#### This module

This option is a short cut to local modules, which are also contained in the
`Operations / classes` option.


#### Operations / classes

This option provides access to all regular built-in and user provided classes and operations,
except for IO connectors and control structures available via the `Data / IO` and `Control`
menu options.


#### Edit

This sub-menu contains options to insert and delete columns, rows and previously
copied areas.


##### Paste

Enabled after an area was copied or cut. Inserts the area at the current position, expanding
to the right an below as needed. Existing content in this area will be overwritten.


##### Insert row

Inserts a new row at the current position. The current row and all rows below will be shifted
down.


##### Insert column

Insert a new column at the current position. The current row and all rows below will be shifted
down.


##### Delete row

Remove the row at the current position. The whole row will be deleted, and all rows below will
be shifted up accordingly.


##### Delete column

Remove the column at the current position. The whole column will be deleted, and all columns
to the right will be shifted to the left accordingly.

