Inferring PMTR mobility
=======================

Description
-----------

This example will: 

1. Download the rollernet data from CRAWDAD. It will prompt you for
your CRAWDAD login and password

2. Import, filter, and prepare the raw using the ditl cli functions

3. Infer the node mobility


Instructions
------------

1. Make sure that the DITL_LIB and PLAUSIBLE_LIB variables point to
the proper jar files.

2. To build, run:

     $ make

3. To play the mobility, use the ditl builtin player:

     $ java -jar ../../../ditl.jar graphs play pmtr.jar