Inferring Rollernet mobility
============================

Description
-----------

This example will: 

1. Download the rollernet data from CRAWDAD. It will prompt you for
your CRAWDAD login and password

2. Import, filter, and prepare the raw using the ditl cli functions

3. Infer the node mobility


Dataset
-------

Opportunistic sighting of Bluetooth devices by groups of rollerbladers
carrying iMotes in a roller tour in Paris, France, on August 20,
2006. The iMotes iniated neighborhood scans every 15 seconds.

The iMotes were distributed to three types of skaters: staff, members
of the skating association, and friends of the experimenters. Nodes 27
and 38 were at all times at the front and back, respectively, of the
rollerblading tour. Furthermore, the inferred mobility was constrained
within a thin rectangular area to simulate movement along a road. All
times are relative to the first unix timestamp in the raw trace.

Original data at [CRAWDAD](http://crawdad.cs.dartmouth.edu/upmc/rollernet).

[Play](http://plausible.lip6.fr/rollernet.jnlp) inferred mobility from [plausible.lip6.fr](http://plausible.lip6.fr) (Uses Java Web Start).

Instructions
------------

1. To build, run:

    $ make

2. To build & play the mobility, use the ditl built-in player:

    $ make play
