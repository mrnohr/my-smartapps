Warn if a switch is turned on manually instead of through SmartThings.

This does not actually work for most switches any more. It depends on if the event records "isPhysical", which is not
always correct. For example, with a GE in-wall Z-wave outlet, I sometimes get false notifications even when SmartThings
turns on the switch and it was not manually turned on.
