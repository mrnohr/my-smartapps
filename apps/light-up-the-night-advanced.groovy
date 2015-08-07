/**
 *  Light Up The Night Advanced
 *
 *  Copyright 2013 Matt Nohr
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

/**
 * Turns on lights when it gets dark, and off when it gets light. Uses an sensor for illuminance like the
 * Aeon Multisensor. It also turns on the lights at a given time, and off at a different time. Finally, it has
 * a delay, so the lights don't go on and off if the light level is right on the threshold.
 */

definition(
		name: "Light Up The Night Advanced",
		namespace: "mrnohr",
		author: "matt",
		description: "Like the standard Light Up The Night app, but with start/end times and an optional lux configuration",
		category: "My Apps",
		iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb.jpg",
		iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg",
		iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg")

preferences {
	section("Monitor the luminosity...") {
		input "lightSensor", "capability.illuminanceMeasurement"
		input "turnOnBrightness", "number", title: "Turn on under this lux (default 30)", required: false
		input "turnOffBrightness", "number", title: "Turn off over this lux (default 50)", required: false
	}
	section("Turn on some lights...") {
		input "lights", "capability.switch", multiple: true
	}
	section("Between these times...") {
		input "morningStart", "time", title: "Not before..."
		input "eveningEnd", "time", title: "Not after..."
	}
	section("Do not flip back for this many minutes") {
		input "flickerThreshold", "number", title: "Minutes (optional)", required: false
	}
}


def installed() {
	commonSubscribe()
}

def updated() {
	unsubscribe()
	commonSubscribe()
}

def commonSubscribe() {
	subscribe(lightSensor, "illuminance", illuminanceHandler)
	schedule(eveningEnd, "turnOffAtNight")
	schedule(morningStart, "turnOnnInMorning")
}

def illuminanceHandler(evt) {
	if(isOutsideFlickerThreshold() && isAppActive()) {
		def lastStatus = state.lastStatus
		def turnOnLux = turnOnBrightness ?: 30
		def turnOffLux = turnOffBrightness ?: 50

		if(lastStatus != "on" && evt.integerValue < turnOnLux) {
			lights.on()
			state.lastStatus = "on"
			state.lastUpdateTime = now()
		} else if(lastStatus != "off" && evt.integerValue > turnOffLux) {
			lights.off()
			state.lastStatus = "off"
			state.lastUpdateTime = now()
		}
	}
}

def turnOffAtNight() {
	lights.off()
	state.lastStatus = "off"
}

def turnOnnInMorning() {
	lights.on()
	state.lastStatus = "on"
}

private getTimeZoneNohr() {
	location.timeZone ?: timeZone(morningStart)
}

private isAppActive() {
	def now = now()
	def earliestTimeToday = timeToday(morningStart, timeZoneNohr).time
	def latestTimeToday = timeToday(eveningEnd, timeZoneNohr).time

	now >= earliestTimeToday && now <= latestTimeToday
}

private isOutsideFlickerThreshold() {
	boolean outside = true
	def lastUpdateTime = state.lastUpdateTime
	if(flickerThreshold && lastUpdateTime) {
		def nextRunTime = lastUpdateTime + (flickerThreshold * 60 * 1000)
		if(nextRunTime > now()) {
			outside = false
		}
	}
	if(!outside) {
		log.debug "Not outside flicker threshold"
	}
	outside
}
