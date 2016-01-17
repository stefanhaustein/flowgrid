# Flowgrid System Library


## /container

Contains operations applicable to container types such as arrays and vectors.


### /container/size

Returns the number of elements contained in this container.


## /control

Contains flow control operators.


### /control/if

Filters or switches the first input value, depending on the second input value, which must
be a boolean value.


### /control/log

Logs the input value to the log that can be pulled out of the right hand side of the screen
and passes it on as the output value.


### /control/loop

Emits the integer numbers from 0 to the input value.


### /control/sync

Waits until two input values are available, then passes on both values.


### /control/emoji

Contains operators for checking whether emoji have certain properties such as colors
or whether they represent food or certain different classes of animals and plants.

Emoji classification operators will probably be moved to the text module in the future.


## /graphics

Graphics classes, interfaces and operations.


### /graphics/Canvas

A canvas represents a paintable area on the screen. It is added to the user interface via
the `Data / IO` menu. The corresponding operator emits a reference to the corresponding canvas
object, which can be used to add shapes and sprites or to obtain a graphics object for
drawing.
 
The canvas coordinate system ranges at least from -500 to 500 in both dimensions.
Coordinates increase up and to the right. The origin is at the center of the canvas.
If the canvas is a rectangle that is not a square, the coordinates of the wider dimension
extend beyond (-)500.


#### /graphics/Canvas/add

Add the given placeable object to the canvas.


#### /graphics/Canvas/createGraphics

Creates a stateful Graphics object that can be used for drawing.


#### /graphics/Canvas/remove

Removes the given placeable object from the canvas.


#### /graphics/Canvas/setOnClickListener

The onCLick operation of the given object will be called when the canvas is touched by the user,
if the corresponding area is not covered by a placeable object that has its own click listener.


#### /graphics/Canvas/removeAll

Removes all objects from the canvas.


### /graphics/Color

A color value. Represented internally as a tuple of red, green, blue and
opacity values with an 8 bit resolution each.


#### /graphics/Color/fromRgb

Construct an opaque color value from the red, green and blue components in the range of 0 to 100.


#### /graphics/Color/fromRgbA

Construct a color value from the red, green, blue and alpha component inputs in the range of
0 to 100.

#### /graphics/Color/fromHsv

Return a color value with the given hue, saturation and value. The hue ranges from 0 to 360,
the other values from 0 to 100.

#### /graphics/Color/fromHsvA

Return a color value with the given hue, saturation, value and alpha (opacity).
The hue ranges from 0 to 360 (exclusive), the other values from 0 to 100.


#### /graphics/Color/toHsv

Return the hue, saturation and value of the input color.


#### /graphics/Color/toHsvA

Return the hue, saturation, value and opacity value of the input color.


#### /graphics/Color/toRgb

Return the red, green and blue component value of the input color.


#### /graphics/Color/toRgbA

Return the red, green, blue and alpha component of the input color.


### /graphics/Graphics

A stateful graphics context, used to draw on a canvas.


#### /graphics/Graphics/clearRect

Clears the rectangle defined by the given coordinates.


#### /graphics/Graphics/drawImage

Draw an image at the given coordinate.


#### /graphics/Graphics/drawRect

Draw the rectangle defined by the given coordinates.


#### /graphics/Graphics/drawLine

Draw a line between the given coordinates.


#### /graphics/Graphics/drawText

Draw text at the given position. The operations to set the alignment define how
the given coordinates relate to where the text is drawn.

By default, the coordinate is defines the top left corner of the text.


#### /graphics/Graphics/setFillColor

Set the current fill color to the given value.


#### /graphics/Graphics/setStrokeColor

Set the current stroke color to the given value.


#### /graphics/Graphics/setStokeWidth

Set the current stroke widht to the given value.


#### /graphics/Graphics/setEraseTextBackground

Set whether the background behind text is erased when drawing text.


#### /graphics/Graphics/setAlignRight

Set the horizontal alignment to the right hand side when drawing text.


#### /graphics/Graphics/setAlignLeft

Set the horizontal alignment to the left hand side when drawing text.


#### /graphics/Graphics/setAlignHCenter

Set the horizontal text alignment to the center.


#### /graphics/Graphics/setAlignTop

Set the vertical text alignment to the top.


#### /graphics/Graphics/setAlignBaseline

Set the vertical text alignment to the text baseline.


#### /graphics/Graphics/setAlignBottom

Set the vertical text alignment to the bottom of the text.


#### /graphics/Graphics/setAlignVCenter

Set the vertical text alignment to the center of the text.


#### /graphics/Graphics/setTextSize

Sets the text size.


### /graphics/Image

#### /graphics/Image/width

Return the width of the input image.


#### /graphics/Image/height

Return the height of the input image.


### /graphics/sprite

Contains interfaces for sprites displayed on a Canvas.


#### /graphics/sprite/Box

A placeable object with a width, height and color, rendered as a rectangular box.


#### /graphics/sprite/Disc

A placeable object with a radius and color, rendered as a disk.


#### /graphics/sprite/Drawable

A placeable object that is rendered by calling a draw method (this is an untested
experimental feature and may not fully work yet).


#### /graphics/sprite/OnAttach

Called when the implementing object is added to a canvas object.


#### /graphics/sprite/OnDrag

Called when the implementing object is dragged.


#### /graphics/sprite/OnClick

Called when the implementing object is clicked.


#### /graphics/sprite/Placeable

Interface for objects that can be added to the canvas. It will be rendered at the position
specified by the x and y properties. If no more specific placeable such as Box or Sprite
is implemented, a simple dot is rendered.


#### /graphics/sprite/Sprite

An object that can be added to the canvas. It will render its image at the position
specified by the x and y properties.


## /logic

Logic operations such as "and", "or" and "not" and "xor".


### /logic/and

Returns true if both input values are true; false otherwise.


### /logic/not

Returns true if the input value is false; false if the input value is true.


### /logic/or

Returns true if any of the input values is true; false if both input values are false.


### /logic/xor

Exclusive or: Returns true if one of the input values is true; false if both are true
or false.


## /math

Mathematical operations, including formatting operations. Trigonometrical operations are
contained in subpackages.


### /math/abs

Return the absolute value of the input value, i.e. the negated input value if it is
negative, otherwise the unmodified input value.

### /math/ceil

Return the closest integer value that is bigger than the input value, or the input value
itself if it is an integer value already.

### /math/exp

Return the result of the e-function applied to the input value.


### /math/floor

Return the closest integer value that is smaller than the input value, or the input value
itself if it is an integer value already.


### /math/log

Return the natural logarithm of the input value.


### /math/log10

Return the logarithm to the base 10 of the input value.


### /math/max

Return the maximum of the input values.


### /math/min

Return the minimum of the input values.


### /math/pow

Return the first input value to the power of the second input value.


### /math/signum

Return 1 if the input value is positive, -1 if the input value is negative or 0 if the input
value is 0.


### /math/sqrt

Return the square root of the input values.


### /math/degree

Trigonometric operations working with parameters or return values in degrees.


#### /math/degree/acos°

Compute the arccosine (in degrees) of the input value.


#### /math/degree/asin°

Compute the arcsine (in degrees) of the input value.


#### /math/degree/atan°

Compute the arctangent (in degrees) of the input value.


#### /math/degree/cos°

Return the cosine of the input angle in degrees.


#### /math/degree/sin°

Return the sine of the input angle in degrees.


#### /math/degree/tan°

Return the tangens of the input angle in degrees.


#### /math/degree/toCarteseian°

Convert the input angle (in degrees) and distance to cartesian coordinates.


#### /math/degree/toPolar°

Convert the input coordinates to an angle in degrees and the distance from the origin.


#### /math/degree/toRadians

Convert the input angle from degree to radians.


### /math/radian

Trigonometric operations working with parameters or return values in radians.


#### /math/radian/acos

Compute the arccosine of the input value.


#### /math/radian/asin

Compute the arcsine of the input value.


#### /math/radian/atan

Compute the arctangent of the input value.

#### /math/radian/atan2


#### /math/radian/cos

Return the cosine of the input angle in radians.


#### /math/radian/cosh


#### /math/radian/sin

Return the sine of the input angle in radians.


#### /math/radian/sinh


#### /math/radian/tan

Return the tangens of the input angle in radians.


#### /math/radian/tanh


#### /math/radian/toDegrees

Convert the input value from radians to degrees.


### /math/vector

Vector operations


#### /math/vector/length

Return the length of the input vector.


## /system

Contains the built-in types.


### /system/Boolean

A boolean value (true or false).


### /system/Number

A 64-bit floating point number.


### /system/Text

A character string.


## /text


### /text/endsWith

Determines if the first argument ends with the second argument.


### /text/equalsIgnoreCase

Determines if arguments match without taking the case into account.


### /text/from

Returns the substring of the input that starts at the position denoted by the second argument.


### /text/hashCode

Returns a hash code for the input.


### /text/indexOf

Returns the position of the second argument in the first argument, or -1 if not contained.


### /text/isEmpty

Determines if the argument is an empty string.


### /text/lastIndexOf

Returns the last position of the second argument in the first argument, or -1 if not contained.


### /text/length

Return the length of the string in characters.


### /text/replaceAll

Returns the first argument with all occurrences of the second argument replaced by the third
argument.


### /text/replaceFirst

Returns the first argument with the first occurence of the second argument replaced by the third
argument.


### /text/split

Returns an array created by splitting the first argument at occurences of the second argument.


### /text/startsWith

Determines if the first argument starts with the second argument.


### /text/substring

Returns the substring of the first input, starting at the second input up to (excluding) the
third argument.


### /text/toLowerCase

Returns the input text converted to lower case.


### /text/toUpperCase

Returns the input text converted to upper case.


### /text/trim

Returns the input with all leading and trailing whitespace removed.


## /sound

This module contains operations on audio data.


### /sound/Sound

Represents audio data.


### /sound/samplingRate

Returns the sampling rate of the given sound object in Hz (1/s).


### /sound/sampleCount

Returns the number of samples in the given sound object.


### /sound/length

Returns the length of the given sound object in seconds.


### /sound/newTone

Generates a tone of the given frequency (Hz) and duration (seconds).
