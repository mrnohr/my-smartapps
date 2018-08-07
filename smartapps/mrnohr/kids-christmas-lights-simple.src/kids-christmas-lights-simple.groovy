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
definition(
		name: "Kids Christmas Lights - Simple",
		namespace: "mrnohr",
		author: "matt",
		description: "turn them off when appropiate",
		category: "My Apps",
		iconUrl: "https://www.dropbox.com/s/6yh38zxpd3c6gn7/light-bulb-9.png?raw=1",
		iconX2Url: "https://www.dropbox.com/s/6yh38zxpd3c6gn7/light-bulb-9.png?raw=1",
		iconX3Url: "https://www.dropbox.com/s/6yh38zxpd3c6gn7/light-bulb-9.png?raw=1")

preferences {
	section {
		input "lights", "capability.switch", title: "Which lights?", multiple: false, required: true
		input "timeout", "number", title: "Timer length", required: true
		input "door1", "capability.contactSensor", title: "Which door?", multiple: false, required: true
        input "offWithDoor", "bool", title: "Turn off with door opening?"
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
    
	log.debug "subscribing to lights on event"
    subscribe(lights, "switch.on", onHandler)
    
    log.debug "subscribing to door closing"
    subscribe(door1, "contact.closed", closedHandler)
    
    if(offWithDoor) {
		log.debug "subscribing to door opening"
		subscribe(door1, "contact.open", openHandler)
    }
}

// Event handlers
def onHandler(evt) {
	startIfNeeded()
}

def closedHandler(evt) {
	startIfNeeded()
}

def openHandler(evt) {
	if(state.currentlyRunning) {
		log.debug "The door opened, turning off the lights"
		stopTimer()
	} else {
		log.debug "The timer is not currently running"
	}
}

// Helper Methods
def startIfNeeded() {
	if(!state.currentlyRunning) {
    	if(door1.currentContact == "closed" && lights.currentSwitch == "on") {
        	log.debug "Starting the timer since the door is closed and the lights are on"
            startTimer()
        }
    }
}

// Timer methods
def startTimer() {
	log.debug "in start timer"
	state.currentlyRunning = true

    sendPush("${lights.displayName} will turn off in $timeout minutes")
	
    runIn(60 * timeout, "stopTimer")
}

def stopTimer() {
	log.debug "in stop timer"
	state.currentlyRunning = false
	
    lights.off()
	
    unschedule("stopTimer")

	sendPush("Turned off ${lights.displayName}")
}