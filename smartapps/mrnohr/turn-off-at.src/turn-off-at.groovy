/**
 *  Turn Off At
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
		name: "Turn Off At",
		namespace: "mrnohr",
		author: "Matt Nohr",
		description: "Turn off multiple lights at a given time",
		category: "My Apps",
		iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/clock.png",
		iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/clock%402x.png",
		iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/clock%402x.png")

preferences {
	section("At a specific time..") {
		input "time1", "time", title: "When?"
	}
	section("Turn off some lights..") {
		input "switches1", "capability.switch", multiple: true
	}
}

def installed() {
	log.debug "installed"
	handleSchedule()
}

def updated() {
	log.debug "updated"
	unschedule()
	handleSchedule()
}

def scheduleTurnOff() {
	log.debug "turning off now"
	turnOffLights()
}

def appTouch(evt) {
	log.debug "appTouch: $evt"
	turnOnLights()
}

private def handleSchedule() {
	schedule(time1, "scheduleTurnOff")
	subscribe(app)
}

private def turnOffLights() {
	switches1?.off()
}
