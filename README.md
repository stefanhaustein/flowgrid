# FlowGrid Documentation

FlowGrid is a visual dataflow programming environment for Android. The main difference to other
visual programming apps such as Scratch is that FlowGrid is based on modelling data flow directly
instead of traditional structured programming.

Basically, instead of using an indirection via variables or registers, the flow of information
through the computer is modelled in a graph, making programs look more like flow chart diagrams.
At the top level, this flow can be easily visualized, potentially making programming also more
accessible.


## Where is the App?


Please join the Google+-Group "FlowGrid" to participate in the public alpha.


## Where is the source?

Will be available here -- I still needs to do some cleanup and to make sure I don't expose
keys in the public repository.


## Tutorials

To get a basic understanding of the editor and concepts, FlowGrid provides a set of increasingly
sophisticated tutorials in the “missions” section of the App. Before diving deeper into developing
custom programs with FlowGrid, I’d recommend to play through some of them to become familiar with
the operation editor.


## Editor

Add functional blocks by tapping anywhere in the grid. Connect the blocks by dragging connections
out of the bottom of the blocks.


## Operations

The most important concept in FlowGrid is an operation. An operation typically has a set of inputs
and output and performs a transformation between them. Operations do so by connecting other
operations in various ways.

A straightforward example is examples/simple/sqr: This operation
computes the square of a number by multiplying it with itself.


### Synchronized vs. Continuous input

In FlowGrid, “synchronized input” means that an operation waits for all input values to be
available, and only then consumes them and executes. This is the default and the case for all
builtin operations.

A continuous operation just passes on any input directly, i.e. it acts like its contents were
inserted in place, similar to a macro in traditional programming.

Continuous operations preserve their state as long as the calling operation is running.


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
  - Make sure all 3rd party stuff is mentioned in credits


## Credits

(c) 2015 Stefan Haustein, Zurich, Switzerland.

Thanks to Jerry Morrison, Mareike Klisch and Pat Coleman for inspiration
and help with testing this app.
