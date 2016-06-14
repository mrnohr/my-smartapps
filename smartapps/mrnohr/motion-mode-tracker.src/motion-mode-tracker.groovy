/**
 *  Motion Mode Tracker
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
    name: "Motion Mode Tracker",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "How many times during a mode was motion detected",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bed%402x.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bed%402x.png")

preferences {
	section("Sensors") {
		input "motionSensors", "capability.motionSensor", multiple: true,
        	title: "Which motion sensor(s) to watch?"
	}
    section("When?") {
    	input "monitorMode", "mode",
        	title: "During which mode?"
    }
    section("Notifications") {
    	input "phone", "phone",
        	title: "Phone number"
        input "notifyOnQuiet", "boolean", defaultValue: false,
        	title: "Send message if no motion detected"
        input "notificationPrefix", "text",
        	title: "Text to append before '5 times during X mode'"
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
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(motionSensors, "motion.active", "motionActiveHandler")
    subscribe(location, "modeChangeHandler")
}

// TODO: implement event handlers
def modeChangeHandler(evt) {
	log.debug "In modeChangeHandler for $evt.name = $evt.value"
    if(evt.value == monitorMode) {
    	log.debug "Starting monitoring of motion"
    	state.monitoring = true
        state.motionCount = 0
    } else {
    	if(state.monitoring) {
        	log.debug "Done monitoring motion for the night"
            state.monitoring = false
            if(state.motionCount == 0) {
	            if(notifyOnQuiet) {
            		sendMessage("No motion was detected during $monitorMode mode!")
                }
                state.lastQuietNight = now()
            } else {
            	def timeWord = (state.motionCount == 1 ? "time" : "times")
            	sendMessage("${notificationPrefix.trim()} ${state.motionCount} $timeWord during $monitorMode mode");
            }
        }
    }
}
def motionActiveHandler(evt) {
	log.debug "In motionActiveHandler for $evt.name = $evt.value"
    state.motionCount = state.motionCount + 1
    log.debug "So far motion has been detected ${state.motionCount} times"
}

def sendMessage(message) {
	log.debug "Sending message: $message"
	sendSms(phone, message)
}
