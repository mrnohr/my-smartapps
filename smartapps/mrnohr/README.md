
# close-garage-door-at-night

Double check your garage door. First warn if it is still open, and then always close when switching to a specific mode.
For example, send a notification at 8:00 PM if the garage is still open, and then close it when switching to "overnight"
mode.

		
# color-light-cycler

Cycle some color-control bulbs on a schedule

		
# colored-bulb-controller

Use a minimote to control colored light bulbs. Turn on/off, cycle colors, etc.

		
# dimmer-remote

Using a button device (like Aeon Minimote), and a dimmer switch, have 1 button to turn the light on full (button 1), one
button to dim the light (button 3), and the other buttons to turn off the light.

		
# dimmers-always-full

Any time a dimmeable light turns on or changes level, reset the level to 99

		
# door-color-notifier

I have 2 color-control light bulbs downstairs that I use as indicators when a door is open upstairs (like a kid's
bedroom door). This adds on to the functionality in other apps in that when the door opens it changes a bulb a color,
and then when it closes, the bulb gets reset back to soft white or turns off. It checks the status of a 2nd bulb to know
what to do.

		
# execute-phrase-on-light-on

Use a virtual switch to control phrases. When the light turns on, execute the phrase. Then automatically turn off the
light a few minutes later.

		
# extra-phrase-execution-on-departure

This handles a specific case where not everyone leaves, which is typically what triggers a phrase to execute, but also
execute it at specific time when only 1 person leaves.

		
# garage-lights

Using a combination of contact sensors and motion sensors, try to know when the lights should be on in the garage and
turn them on/off accordingly. 

		
# garage-monitor

Watch a garage door and send notifications if it didn't close or if it was left open.

		
# house-showing-monitor

When selling our house, this app was used to let us know when people were looking at our house, and how long they were
there.

		
# kid-monitor-2

This is an old app that I was trying to use to monitor a baby's room for doors opening and noise levels. I never really
got it to work.

		
# kids-christmas-lights-advanced

The kids like Christmas lights going in their room as they fall asleep during the holidays. This sets a timer to turn
the lights off after a certain number of minutes. However, to keep the kids in bed, it also turns off the lights if the
kids get up and open the door.

This 'advanced' version uses a Minimote to turn on the lights and set the timer. The top-left button turns on the
lights. The top-right (and bottom-right) turns them off. The bottom-right turns on the light and starts the timer.

		
# kids-christmas-lights-simple

The kids like Christmas lights going in their room as they fall asleep during the holidays. This sets a timer to turn
the lights off after a certain number of minutes. However, to keep the kids in bed, it also turns off the lights if the
kids get up and open the door.

		
# light-up-the-night-advanced

Turns on lights when it gets dark, and off when it gets light. Uses an sensor for illuminance like the Aeon Multisensor.
It also turns on the lights at a given time, and off at a different time. Finally, it has a delay, so the lights don't
go on and off if the light level is right on the threshold.

		
# lights-on-when-occupied

This was an experimental app to see if I could tell when someone was in the bathroom and automatically turn on the
lights. It uses a combination of a contact sensor and motion detector to try to know if someone is in the room and
keep the lights on.

		
# mode-change-tracker

Send a notification (using contact book) whenever a mode changes

		
# motion-mode-tracker

This detects how many times during a specific mode was motion detected. For example, when in "Night" mode, how many
times was motion detected outside?

		
# once-a-day-motion-alert

Get notified once a day when there is motion.

		
# physical-switch-detector

Warn if a switch is turned on manually instead of through SmartThings.

This does not actually work for most switches any more. It depends on if the event records "isPhysical", which is not
always correct. For example, with a GE in-wall Z-wave outlet, I sometimes get false notifications even when SmartThings
turns on the switch and it was not manually turned on.

		
# remember-to-lock-door

I have some doors that don't have connected locks, but if they are ever opened I know they were unlocked first. For
example my shed or patio door. If one of these doors opens (and was therefore unlocked), send a reminder to make sure it
has been locked up.

		
# thermostat-mode-change-reporter

When the thermostat mode changes (from Cool to Off for example), send a notification.

		
# three-light-toggle

Using one master switch and 2 other switches, cycle through which lights are on with each 'on' command from the master.
This will only work with physical switches that allow you to send multiple "on" commands even if the switch is already
on.

This is fairly limiting and I haven't actually used this in years.

		
# turn-light-back-on

Simple app to turn a light back on after it has been turned off.

		
# turn-off-at

Just turn off some lights at a given time, or when tapped

Date: 2013-01-25

		
# turn-on-at

Just turn on some lights at a given time, or when tapped

Date: 2013-01-25

		
# turn-on-lights-when-door-opens-after-sunset

When a door opens after sunset, turn on some lights

TODO: This doesn't work the first day

		
# two-light-toggle

Using one master switch and 1 other switch, cycle through which lights are on with each 'on' command from the master.
This will only work with physical switches that allow you to send multiple "on" commands even if the switch is already
on.

This is fairly limiting and I haven't actually used this in years.

		
# up-at-night-tracker

Count how many times during a mode a door opens. I use this to see how many times a night my kids wake up.

		
# web-service-phrase-executor

Example of using web service SmartApp to execute a phrase.

		