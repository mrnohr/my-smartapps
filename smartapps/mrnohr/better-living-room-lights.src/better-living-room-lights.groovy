/**
 *  Better Living Room Lights
 *
 *  Copyright 2017 Matt Nohr
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
    name: "Better Living Room Lights",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "Control the lights based on: illuminance, mode, and time",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2256790/st-icons/lights.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/st-icons/lights.png",
    iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/st-icons/lights.png")


preferences {
	section("Control these lights...") {
		input "lights", "capability.switch", multiple: true
	}
    section("During these times...") {
    	input "morningOn", "time", title: "Turn on at this time in the morning"
        input "midMorningOff", "time", title: "Turn off at this time in the morning if nobody is home"
        input "afternoonOn", "time", title: "Turn on at this time in the afternoon if nobody is home"
        input "eveningOff", "time", title: "Turn off at this time at night"
    }
    section("With these light levels...") {
    	input "lightSensor", "capability.illuminanceMeasurement", title: "Light sensor"
		input "turnOnBrightness", "number", title: "Turn on under this lux"
		input "turnOffBrightness", "number", title: "Turn off over this lux"
    }
    section("Turn off during the day unless I'm in this mode...") {
    	mode name:"homeMode", title: "Which mode?"
    }
    section("Notifications for fine tuning") {
    	input "debugLevel", "enum", title: "Send notifications for fine tuning?", options: ["every":"Every Execution","changes":"Only On Change","none":"Never"]
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
            input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, options: ["Yes", "No"]
        }
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
	//run every 15 minutes (2, 17, etc), in the 9th second
    schedule("9 2/15 * * * ?", scheduleHandler)
}

def scheduleHandler() {

}
