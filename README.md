# FlowGrid

FlowGrid is a visual dataflow programming environment for Android. The main difference to other
visual programming apps such as [Scratch](https://scratch.mit.edu/) is that FlowGrid is based on
modelling data flow directly instead of traditional structured programming.

Basically, instead of using an indirection via variables or registers, the flow of information
through the computer is modelled in a graph, making programs look more like flow chart diagrams.
At the top level, this flow can be easily visualized, potentially making programming also more
accessible.


## Some Meta Stuff

### Where is the App?

Please join the Google+-Group
"[FlowGrid](https://plus.google.com/u/0/communities/116001482434880598082)" for feedback and
discussions and download the public alpha from <https://play.google.com/apps/testing/org.flowgrid>.


### Tutorials

To get a basic understanding of the editor and concepts, FlowGrid provides a set of increasingly
sophisticated tutorials in the “missions” section of the App. Before diving deeper into developing
custom programs with FlowGrid, I’d recommend to play through some of them to become familiar with
the operation editor.

![Tutorial Screenshot](https://lh3.googleusercontent.com/SU350EF-5AOVfjLSVmqHFbwTIKylOgsB5pp1Jipe7BCbLIMYZawebvSjwB-5lTbH3JhVkQ=w3840-h2160-rw-no)

### Help!

I have been working on this side project for quite a while now (about two years, taking up most of
my spare time) and thought it might be a good idea to get some wider feedback before sinking
more time into this and launching the app publicly on the Google Play Store. In particular, I'd be
interested in any "real world" use cases -- or suggestions for making this suitable for realistic
ones.

- What are realistic use cases where this app makes sense?
- Are there use cases where it would make sense, but some bits are missing?
- Do you have suggestions for additional tutorials in the "missions" section?
- What in particular needs better documentation?

Please post feedback in the corresponding
[G+-group](https://plus.google.com/u/0/communities/116001482434880598082),
[file an issue](https://github.com/FlowGrid/flowgrid-android/issues) or send feedback to
feedback@flowgrid.org.


### Where is the source?

The FlowGrid source code will be available here (it's currently in a hidden repository on bitbucket).

I still need to

- do some cleanup
- decide about the source license (probably Apache for the core and GPL for the UI)
- make sure I don't expose keys in the public repository
- figure out the best way to transfer the existing repository (I know, everything! But what
  in particular is surprising / unexpected / confusing?) 


## Documentation


### Editor

Add functional blocks by tapping anywhere in the grid. Connect the blocks by dragging connections
out of the bottom of the blocks.


### Operations

The most important concept in FlowGrid is an operation. An operation typically has a set of inputs
and output and performs a transformation between them. Operations do so by connecting other
operations in various ways.

A straightforward example is examples/simple/sqr: This operation
computes the square of a number by multiplying it with itself.


#### Synchronized vs. Continuous input

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


### Arduino Connection

To control an Arduino board via FlowGrid, please install
[StandardFirmata](https://github.com/firmata/arduino#usage) on the Arduino board.


## System Library


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

Mathematical operations. Trigonometrical operations are contained in subpackages.


## Known Issues / Todos

- Editor
  - When adding a constant to an input, the type should be inferred
  - When adding an output port to an exisingt connection, the type should be inferred
  - The array editor should support drag and drop

- Code
  - Publish full source code on GitHub
  - Switch fragments to the support library

- Synchronization
  - Reduce logspam
  - Improve reload triggering
  - Use drive query to synchronize faster and to make delete work more reliable

- Documentation
  - Full system library documentation
  - Switch vs. Filter vs. Compute
  - Make sure all 3rd party stuff is mentioned in the copyright section


## Copyright

(c) 2016 Stefan Haustein, Zurich, Switzerland.

Thanks to Jerry Morrison, Mareike Klisch and Pat Coleman for inspiration
and help with testing this app.
