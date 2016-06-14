/**
 *  Dimmers Always Full
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
		name: "Dimmers Always Full",
		namespace: "mrnohr",
		author: "matt",
		description: "Make sure a dimmer is always 100 or 0",
		category: "My Apps",
		iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb.jpg",
		iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg",
		oauth: true)

preferences {
	section("Dimmer Switch(es)") {
		input "switches", "capability.switchLevel", multiple: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(switches, "switch", onOffHandler)
	subscribe(switches, "level", levelHandler)
}

def onOffHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if(evt.value == "on") {
		log.debug "Event was 'on', going to set the level to 99"
		setSwitchToFull(evt)
	}
}

def levelHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if(evt.value != '99') {
		log.debug "Trying to adjust the level, resetting to 99"
		setSwitchToFull(evt)
	}
}

def setSwitchToFull(evt) {
	def switch1 = switches.find { it.id == evt.deviceId }
	if(switch1) {
		log.debug "Setting level of switch ${switch1.label ?: switch1.name} to 99"
		switch1.setLevel(99)
	}
}
