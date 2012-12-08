Inferring PMTR mobility
=======================

Description
-----------

This example will: 

1. Download the pmtr data from CRAWDAD. It will prompt you for
your CRAWDAD login and password

2. Import, filter, and prepare the raw using the ditl cli functions

3. Infer the node mobility


Dataset
-------

This dataset contains mobility traces from 44 custom made Pocket
Mobile Trace Recorders (PMTRs) at University of Milano. The PMTRs were
distributed to faculty members, PhD students, and technical
staff. Beacons were sent every second. These people work in offices
and laboratories located in a three-floor building, roughly 200x100 m
large and take lunches or coffee breaksin a nearby cafeteria. The data
was collected during 10 days in November 2008.

Original data at [CRAWDAD](http://crawdad.cs.dartmouth.edu/unimi/pmtr).

[Play](http://plausible.lip6.fr/pmtr.jnlp) inferred mobility from [plausible.lip6.fr](http://plausible.lip6.fr) (Uses Java Web Start).

Instructions
------------

1. To build, run:

    $ make

2. To build & play the mobility, use the ditl built-in player:

    $ make play