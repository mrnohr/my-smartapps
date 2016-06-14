/**
 *  House Showing Monitor
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
    name: "House Showing Monitor",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "control a simulated light based on a door",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/home%402x.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/home%402x.png",
    iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/home%402x.png")


preferences {
	section("Devices") {
		input "door1", "capability.contactSensor", title: "Which door?"
        input "light1", "capability.switch", title: "Which light?"
	}
    section("Config") {
    	input "thresholdMin", "number", title: "How Long?"
    }
    section("Contacts") {
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

    resetState(true)
}

def initialize() {
	subscribe(door1, "contact", contactHandler)
    subscribe(light1, "switch.off", switchOffHanlder)
}

def contactHandler(evt) {
	log.debug "contactHandler - ${state.inProgress} - ${evt.value}"
    def milliThreshold = (thresholdMin * 60 * 1000) as Long
	if(state.inProgress == true) {
	    def timePassed = now() - (state.startTime as Long)
    	if(evt.value == 'closed' && (timePassed > milliThreshold)) {
        	//door closed after X minutes, must be done
			endShowing()
        } else {
        	log.debug "In progress, door ${evt.value} - time $timePassed - threshold $milliThreshold"
        }
    } else {
    	if(evt.value == 'open') {
        	startShowing()
        }
    }
}

def switchOffHanlder(evt) {
	//This is used to overwrite/reset the state. For example, if I opened the door but a showing isn't happening.
    if(atomicState.inProgress == true) {
    	resetState(false)

        def msg = "House showing reset"
        log.debug msg
        messageMe(msg)
    }
}

def startShowing() {
	state.inProgress = true
    state.startTime = now()
    light1.on()

    def msg = "Starting house showing - door opened"
    log.debug msg
    messageMe(msg)
}

def endShowing() {
	def timePassed = now() - (state.startTime as Long)
    def timePassedMinutes = (timePassed / 1000 / 60) as Integer
    def msg = "House showing complete. Took $timePassedMinutes minutes"
    log.debug msg
    messageMe(msg)
    resetState(true)
}

def resetState(boolean toggleLight) {
    atomicState.inProgress = false
    if(toggleLight) {
	    light1.off()
    }
}

def messageMe(msg) {
	if(recipients) {
		sendNotificationToContacts(msg, recipients)
	}
}
