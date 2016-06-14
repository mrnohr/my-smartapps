/**
 *  Dimmer Remote
 *
 *  Copyright 2014 Matt Nohr
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
		name: "Dimmer Remote",
		namespace: "mrnohr",
		author: "matt",
		description: "Remote to turn on/off lights at different levels",
		category: "My Apps",
		iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg",
		iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg",
		iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg")

preferences {
	section {
		input "lights", "capability.switchLevel", title: "Which lights?", multiple: true, required: true
		input "buttonDevice", "capability.button", title: "Minimote", multiple: false, required: true
		input "dimLevel", "number", title: "Dim Level (30 default)", required: false
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
	subscribe(buttonDevice, "button", buttonHandler)
}

// event handlers
def buttonHandler(evt) {
	def buttonNumber = evt.data
	def value = evt.value
	log.debug "buttonEvent: $evt.name = $evt.value ($evt.data)"
	log.debug "button: $buttonNumber, value: $value"

	def recentEvents = buttonDevice.eventsSince(new Date(now() - 2000)).findAll {
		it.value == evt.value && it.data == evt.data
	}
	log.debug "Found ${recentEvents.size() ?: 0} events in past 2 seconds"

	if(recentEvents.size <= 1) {
		handleButton(extractButtonNumber(buttonNumber), value)
	} else {
		log.debug "Found recent button press events for $buttonNumber with value $value"
	}
}

// button methods
def extractButtonNumber(data) {
	def buttonNumber
	//TODO must be a better way to do this. Data is like {buttonNumber:1}
	switch(data) {
		case ~/.*1.*/:
			buttonNumber = 1
			break
		case ~/.*2.*/:
			buttonNumber = 2
			break
		case ~/.*3.*/:
			buttonNumber = 3
			break
		case ~/.*4.*/:
			buttonNumber = 4
			break
	}
	return buttonNumber
}

def handleButton(buttonNumber, value) {
	switch([number: buttonNumber, value: value]) {
	//value is 'pushed' or 'held'
		case { it.number == 1 }:
			lights.setLevel(100)
			lights.on()
			break
		case { it.number == 2 }:
			lights.off()
			break
		case { it.number == 3 }:
			lights.setLevel(dimLevel ?: 30)
			lights.on()
			break
		case { it.number == 4 }:
			lights.off()
			break
		default:
			log.debug "Unhandled command: $buttonNumber $value"
	}
}
