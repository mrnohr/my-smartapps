/**
 *  Outside Lights on When Door Opens
 *
 *  Copyright 2015 Matt Nohr
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
definition(
		name: "Turn on Lights when Door Opens after Sunset",
		namespace: "mrnohr",
		author: "Matt Nohr",
		description: "Turn on lights after sunset when a door opens",
		category: "My Apps",
		iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/garage%402x.jpg",
		iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/garage%402x.jpg",
		iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/garage%402x.jpg")


preferences {
	section("Door") {
		input "door1", "capability.contactSensor", title: "Door"
	}
	section("Lights") {
		input "switch1", "capability.switch", title: "Light"
		input "offTime", "number", title: "Turn off after X minutes"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	//know if it is daylight
	subscribe(location, "sunset", sunsetHandler)
	subscribe(location, "sunrise", sunriseHandler)
	state.afterSunset = true

	//door events
	subscribe(door1, "contact", doorHandler)
	subscribe(door1, "acceleration.active", doorHandler)
}

//daylight handlers
def sunsetHandler(evt) {
	state.afterSunset = true
}

def sunriseHandler(evt) {
	state.afterSunset = false
}

//door handler
def doorHandler(evt) {
	log.debug "doorHandler ${evt.name} = ${evt.value} (afterSunset = ${state.afterSunset})"
	if(isOpening(evt)) {
		log.debug "Door is opening, turning on lights"
		switch1.on()
	} else if(isOpen(evt)) {
		log.debug "Door open, turning on lights"
		//the door opened and the lights aren't on yet
		switch1.on()
	} else if(isClosed(evt)) {
		log.debug "Door closed, scheduling lights to turn off in $offTime minutes"
		//the door closed, schedule the lights to turn off
		runIn(offTime * 60, 'turnOff')
	}
}

//scheduled to turn off light
def turnOff() {
	log.debug "Turn off light"
	switch1.off()
}

//test state methods
def isOpening(evt) {
	return state.afterSunset &&
			evt.name == 'acceleration' &&
			evt.value == 'active' &&
			door1.currentValue('contact') == 'closed'
}

def isOpen(evt) {
	return state.afterSunset &&
			evt.name == 'contact' &&
			evt.value == 'open' &&
			switch1.currentValue('switch') == 'off'
}

def isClosed(evt) {
	return evt.name == 'contact' &&
			evt.value == 'closed'
}
