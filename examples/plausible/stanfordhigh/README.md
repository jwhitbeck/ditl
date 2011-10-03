Inferring Stanford High School mobility
=======================================

Description
-----------

This example will: 

1. Download the stanford high school data from the salathe group's
webpage.

2. Import, filter, and prepare the raw using the ditl cli functions

3. Infer the node mobility


Dataset
-------

Face-to-face contacts among all people in a US high school captured by
ZigBee motes (TelosB Crossbow) between 7AM and 4PM. Beacons were send
every 20 seconds. The 789 participants were divided into four groups:
student, teacher, staff, and other.

Original data at [www.salathegroup.com](http://www.salathegroup.com/guide/school_2010.html).

[Play](http://plausible.lip6.fr/stanford.jnlp) inferred mobility from [plausible.lip6.fr](http://plausible.lip6.fr) (Uses Java Web Start).

Instructions
------------

1. Make sure that the DITL\_LIB and PLAUSIBLE\_LIB variables point to
the proper jar files.

2. To build, run:

    $ make

3. To play the mobility, use the ditl built-in player:

    $ java -jar ../../../ditl.jar graphs play stanford.jar