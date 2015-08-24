SmartApps
=========

I've been working at [SmartThings](http://www.smartthings.com) since 2012. One of my favorite parts of the SmartThings platform is being able to write SmartApps, which are groovy scripts that can change how my house behaves.

Here are some of the [apps](smartapps/mrnohr/) that I run in my own home.

## List of apps
* **Close Garage Door**
Double check your garage door. First warn if it is still open, and then always close when switching to a specific mode. For example, send a notification at 8:00 PM if the garage is still open, and then close it when switching to "overnight" mode.

* **Dimmer Remote**
Using a button device (like Aeon Minimote), and a dimmer switch, have 1 button to turn the light on full (button 1), one button to dim the light (button 3), and the other buttons to turn off the light.

* **Dimmers Always Full**
Any time a dimmeable light turns on or changes level, reset the level to 99

* **Execute Phrase When a Light Turns On**
When a light turns on, execute a phrase

* **Extra Phrase Execution**
This handles a specific case where not everyone leaves, which is typically what triggers a phrase to
execute, but also execute it at specific time when only 1 person leaves.

* **Kids Christmas Lights**
The kids like Christmas lights going in their room as they fall asleep during the holidays. This sets a timer to turn the lights off after a certain number of minutes. However, to keep the kids in bed, it also turns off the lights if the kids get up and open the door.

* **Light Up The Night Advanced**
Turns on lights when it gets dark, and off when it gets light. Uses an sensor for illuminance like the Aeon Multisensor. It also turns on the lights at a given time, and off at a different time. Finally, it has a delay, so the lights don't go on and off if the light level is right on the threshold.

* **Turn On Lights When A Door Opens After Sunset**
When a door opens after sunset, turn on some lights. Known bug - may not work the first day

* **Turn On/Off At**
Just turn on (or off) switches at a specific time. There are better ways to do that now, but these were written in early 2013 and were some of my first apps.
