/**
 *  Close Garage Door At Night
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
		name: "Close Garage Door At Night",
		namespace: "mrnohr",
		author: "Matt Nohr",
		description: "If the garage door is open, close it",
		category: "My Apps",
		iconUrl: "https://www.dropbox.com/s/ufoxb0kgi9dorkb/garage.png?raw=1",
		iconX2Url: "https://www.dropbox.com/s/ufoxb0kgi9dorkb/garage.png?raw=1")

preferences {
	section("What Garage Door?") {
		input "contact1", "capability.contactSensor", title: "Open/Closed Sensor"
		input "opener1", "capability.momentary", title: "Garage Door Button"
	}
	section("Actions") {
		input "alertTime", "time", title: "Alert Me At This Time", required: false
		input "closeMode", "mode", title: "Close when switching to this mode", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	if(closeMode) {
		subscribe(location, "modeChangeHandler")
	}
	if(alertTime) {
		runDaily(alertTime, "checkAndAlert")
	}
}

def checkAndAlert() {
	def doorOpen = isDoorOpen()
	log.debug "In checkAndAlert - is the door open: $doorOpen"
	if(doorOpen) {
		def message = "The garage door is still open!"
		log.info message
		sendNotification(message, [method: "push"])
	}
}

def modeChangeHandler(evt) {
	log.debug "In modeChangeHandler for $evt.name = $evt.value"
	if(evt.value == closeMode && isDoorOpen()) {
		def message = "Closing garage door since it was left open"
		log.info message
		sendNotification(message, [method: "push"])

		opener1.push()
	}
}

def isDoorOpen() {
	return contact1.latestValue("contact") == "open"
}
