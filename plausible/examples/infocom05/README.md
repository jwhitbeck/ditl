Inferring Infocom'05 mobility
=============================

Description
-----------

This example will: 

1. Download the infocom'05 data from CRAWDAD. It will prompt you for
your CRAWDAD login and password

2. Import, filter, and prepare the raw using the ditl cli functions

3. Infer the node mobility


Dataset
-------

Bluetooth sightings of 41 users carrying small Bluetooth devices
(iMotes) for four days (March 7-10, 2005) in the Grand Hyatt Miami
during the INFOCOM 2005 conference. The iMotes iniated neighborhood
scans every 2 minutes.

Original data at [CRAWDAD](http://crawdad.cs.dartmouth.edu/cambridge/haggle).

[Play](http://plausible.lip6.fr/infocom05.jnlp) inferred mobility from [plausible.lip6.fr](http://plausible.lip6.fr) (Uses Java Web Start).

Instructions
------------

1. Make sure that the DITL\_LIB and PLAUSIBLE\_LIB variables point to
the proper jar files.

2. To build, run:

    $ make

3. To play the mobility, use the ditl built-in player:

    $ java -jar ../../../ditl.jar graphs play infocom05.jar