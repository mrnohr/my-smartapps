/**
 *  Remember To Lock Door
 *
 *  Copyright 2016 Matt Nohr
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
    name: "Remember To Lock Door",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "If certain doors open during the day, remind me at a certain time to to check it was locked.",
    category: "My Apps",
    iconUrl: "https://www.dropbox.com/s/t5e3bjjzg0pkmxc/door.png?raw=1",
    iconX2Url: "https://www.dropbox.com/s/t5e3bjjzg0pkmxc/door.png?raw=1",
    iconX3Url: "https://www.dropbox.com/s/t5e3bjjzg0pkmxc/door.png?raw=1")


preferences {
	section("Doors") {
		input "doors", "capability.contactSensor", title: "Which doors?", multiple: true
	}
    section("Notifications") {
    	input "notificationTime", "time", title: "When to get notifications?"
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
    unschedule()
    
	initialize()
}

def initialize() {
	//listen for the doors to open
	subscribe(doors, "contact.open", doorOpenHandler)

	//schedule the reminder
    schedule(notificationTime, reminderSchedule)

	resetState()
}

//event handlers
def doorOpenHandler(evt) {
	log.debug("State 1: $state")
	def openedDeviceId = evt.device.id
	if(!state.openDoors[(openedDeviceId)]) {
    	state.openDoors[(openedDeviceId)] = evt.device.label
    }
    log.debug("State 2: $state")
}

//schedule handlers
def reminderSchedule() {
	if(state.openDoors) {
    	def labelList = state.openDoors.collect{it.value}
        def message
		if(labelList.size() == 1) {
	        message = "Check if ${labelList[0]} is locked"
        } else if(labelList.size() == 2) {
        	message = "Check if ${labelList[0]} and ${labelList[1]} are locked"
        } else {
        	message = "Check if ${labelList.join(', ')} are locked"
        }
        
        log.debug(message)
        messageMe(message)
        
        resetState()
    }
}

//helper methods
def resetState() {
	state.openDoors = [:]
}

// Notification Methods
def messageMe(message) {
	if(recipients) {
		sendNotificationToContacts(message, recipients)
	}
}