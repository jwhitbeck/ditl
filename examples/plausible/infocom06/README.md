Inferring Infocom'06 mobility
=============================

Description
-----------

This example will: 

1. Download the infocom'06 data from CRAWDAD. It will prompt you for
your CRAWDAD login and password

2. Import, filter, and prepare the raw using the ditl cli functions

3. Infer the node mobility


Dataset
-------

Bluetooth sightings of 70 users carrying small Bluetooth devices
(iMotes) for four to five days (April 23-27, 2006) in the Princesa
Sofia Grand Hotel, Barcelona, during the INFOCOM 2006 conference. The
iMotes iniated neighborhood scans every 2 minutes.

iMotes 1-20 are static long-range (~100m) motes that were deployed
within the conference area. The deployment map is included in the data
on Crawdad . The conference area spanned 3 different levels
(mezzanine, floor -1, and floor -2). In the inferred mobility, static
motes were placed according to two rules: (i) motes on the same floor
keep the same relative placement as on the deployment map, and (ii)
motes from different floors that could detect each other were placed
close to each other.

Original data at [CRAWDAD](http://crawdad.cs.dartmouth.edu/cambridge/haggle).

[Play](http://plausible.lip6.fr/infocom06.jnlp) inferred mobility from [plausible.lip6.fr](http://plausible.lip6.fr) (Uses Java Web Start).

Instructions
------------

1. Make sure that the DITL\_LIB variable point to the proper jar file.

2. To build, run:

    $ make

3. To play the mobility, use the ditl built-in player:

    $ java -jar ../../../ditl.jar graphs play infocom06.jar