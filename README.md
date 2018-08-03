DITL - DynamIc Trace Library
============================

[![Build Status](https://travis-ci.org/jwhitbeck/ditl.png)](https://travis-ci.org/jwhitbeck/ditl.png)


Introduction
-----------

The DynamIc Trace Library is a Java library to efficiently handle and
combine a variety of time-indexed event traces (e.g. position,
contact, or message transfer traces), as well as store meta-data in a
standardized way.  Its core is very generic and can handle either
_stateless_ events (e.g., a received beacon), or _stateful_ events
(e.g., groups modified by join and leave events).

I am releasing the code _as is_ under popular pressure for the
[plausible mobility](http://plausible.lip6.fr) and reachability code
built on top of it. It is under active development but documentation
is unfortunately still lacking at this point. However, rudimentary
documentation is built into the CLI interface (see below).

The _core_ and _graphs_ module are being used in research projects by
myself and others can be considered quite stable. The _transfers_
module is currently serving as the basis for an opportunistic network
simulator, but will probably see some changes and improvement as
development of the simulator continues.

DITL is built around the following design choices:

* Everything is a trace. Calculations and simulations all become
transformations of one set of traces into another set of traces. For
example, an opportunistic network simulator merely converts a _edge_
trace to a _message_ and a _buffer_ trace.

* Pre-calculate as much as possible. For example, edges arising from
the proximity of two nodes (i.e., the disk-based radio model) should
only be calculated once and not for every run of a simulation. Same
goes for connected components, etc.

* Everything should be replayable. By keeping traces of everything, is
it easy to move back and forth in time to find and replay a particular
sequence of events.

* Do not reinvent the wheel. There are very good tools for mobility
generation, statistics, and others. DITL just tries to import/export
data in ways that interface with existing tools. For example, it can
import/export data from [NS](http://www.nsnam.org/) or
[ONE](http://www.netlab.tkk.fi/tutkimus/dtn/theone/). It also has
rudimentary reporting functions which mostly dump raw distributions
(we then use the R language for processing them).


Concepts
--------

### Trace

A trace is a combination of three things:

1. a time-indexed sequence of events
2. (Optional) if the trace is stateful, a time-indexed sequence of
state snapshots corresponding to the events
3. its associated meta-data

All traces have the following meta-data: name, description, type,
min/max time, min/max update interval (the time between successive
events), snapshot interval (stateful traces only), priority, and time
unit (how many time units to a second). This is easily extensible and
some traces contain more meta-data. For example, movement traces also
save min/max X and Y coordinates.

Here is a list of the trace types currently in the library:

* Graphs module
  * presence
  * arc
  * edge
  * movement
  * beacon (stateless)
  * group
* Transfers module
  * message
  * buffer
  * transfer
* Plausibile module
  * windowed edge


### Store

A store is a collection of traces. It is intended to be used as a
container for several traces relating to the same source of
events. For example, a store could be used to contain a simulated
Random Waypoint _movement_ trace, several _edge_ traces highlighting
potential contact opportunities at various transmission ranges, and
several _group_ traces representing the pre-calculated connected
components.

There are several supported store formats: a simple directory, a jar
archive file, or using the java classpath.

### Bus

A bus is associated to a specific type of event. Any number of sources
may queue events on the bus, and any number of listeners may register
to receive these events. The bus takes care of ensuring that events
are always sent to the listeners in proper chronological order, but
the sources may queue events at arbitrary times. For example, an easy
way of combining two movement traces would be to feed their events to
the same bus.

### Runner

A runner is collection of sources and buses. Its job is to trigger the
events from the source and then synchronize events and states across
buses. It supports seeking and incrementing.



Building
--------

This project assumes a recent JDK (&ge;1.6) and uses maven. At the
project root just run:

    $ mvn install

This will build a single jar archive _ditl.jar_ containing both the
core and module classes.


Download
--------

A pre-built jar archive file is available for download:
[ditl-2.0.2.jar](https://s3.amazonaws.com/share.whitbeck.net/ditl-2.0.2.jar)


CLI Interface
-------------

The _ditl.jar_ archive is an executable jar file. When calling it from
the command-line, defining a shell (assuming bash) alias is useful:

    $ alias ditl="java -jar /path/to/ditl.jar"

To see the built-in help, just type:

    $ ditl --help

Most commands are of the form MODULE COMMAND [OPTIONS] ARG1 [ARG2..],
where the first argument is usually the location of the trace store
the command will be operating on. For example, in order to import a
NS2 movement file (e.g., movement.ns2) as a trace named "movement"
into the store "store.jar", one would use:

    $ ditl graphs import-movement --movement movement --format ns2 store.jar movement.ns2

In fact, since "movement" and "ns2" are the default name and format,
respectively, when importing movement into a store, the above can be
further simplified to:

    $ ditl graphs im store.jar movement.ns2


There are many available CLI commands. The Makefiles for the plausible
mobility examples (under examples/plausible) present many different
calls to the ditl cli interface.


Player
------

![Ditl player screenshot](http://plausible.lip6.fr/img/rollernet.png)

When a movement trace is present in a store, the ditl library contains
a built-in player to view it. It will automatically detect other
traces (e.g., edge or static group traces) and add the relevant
information to the player.

For example, the [Plausible Mobility](http://plausible.lip6.fr) page
serves the player as a Java Web Start application. Alternatively, you
could download a sample dataset from the plausible mobility project
and run it locally:

    $ wget http://plausible.lip6.fr/rollernet.jar
    $ ditl graphs play rollernet.jar


Publications
------------

The following publications are based on this code.

* [Temporal Reachability Graphs](http://dx.doi.org/10.1145/2348543.2348589) - _ACM Mobicom 2012_
* [Push-and-Track: Saving Infrastructure Bandwidth Through Opportunistic Forwarding](http://dx.doi.org/10.1016/j.pmcj.2012.02.001) - _Pervasive and Mobile Computing_ (August 2012)
* [From Encounters to Plausible Mobility](http://www-npa.lip6.fr/~whitbeck/img/doi.png) - _Pervasive and Mobile Computing_ (April 2011)


License
-------

This program is free software; you can redistribute it
and/or modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Library General Public License for more details.
