/**
 *  Kids Christmas Lights
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
 *  The kids like Christmas lights going in their room as they fall asleep during the holidays. This sets a
 *  timer to turn the lights off after a certain number of minutes. However, to keep the kids in bed, it also
 *  turns off the lights if the kids get up and open the door.
 */

definition(
		name: "Kids Christmas Lights - Simple",
		namespace: "mrnohr",
		author: "matt",
		description: "turn them off when appropiate",
		category: "My Apps",
		iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/tree%402x.png",
		iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/tree%402x.png",
		iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/tree%402x.png")

preferences {
	section {
		input "lights", "capability.switch", title: "Which lights?", multiple: true, required: true
		input "timeout", "number", title: "Timer length", required: true
		input "door1", "capability.contactSensor", title: "Which door?", multiple: false, required: true
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
	log.debug "In initialize"
	state.currentlyRunning = false
	log.debug "subscribing to door"
	subscribe(door1, "contact.open", openHandler)
	log.debug "subscribing to app touch event"
	subscribe(app, appTouch)
}

// TODO: implement event handlers
def appTouch(evt) {
	log.debug "Staring the timer since you touched the app"
	startTimer()
}

def openHandler(evt) {
	if(state.currentlyRunning) {
		log.debug "The door opened, turning off the lights"
		stopTimer()
	} else {
		log.debug "The timer is not currently running"
	}
}

// helper methods
def startTimer() {
	log.debug "in start timer"
	state.currentlyRunning = true
	lights.on()
	sendPush("Christmas lights will turn off in $timeout minutes")
	runIn(60 * timeout, "stopTimer")
}

def stopTimer() {
	log.debug "in stop timer"
	state.currentlyRunning = false
	lights.off()
	unschedule("stopTimer")

	sendPush("Turned off Christmas lights")
}
