/**
 *  Garage Monitor
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
    name: "Garage Monitor",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "make sure garage door is closed",
    category: "My Apps",
    iconUrl: "https://www.dropbox.com/s/ufoxb0kgi9dorkb/garage.png?raw=1",
    iconX2Url: "https://www.dropbox.com/s/ufoxb0kgi9dorkb/garage.png?raw=1",
    iconX3Url: "https://www.dropbox.com/s/ufoxb0kgi9dorkb/garage.png?raw=1")


preferences {
	section("Door") {
		input "garageDoor", "capability.contactSensor", title: "Garage Door"
	}
    section("Schedule") {
    	input "hourCron", "text", title: "Hour part of cron", required: false
    }
    section("Notifications") {
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
	//Used for when door opens/closes
    subscribe(garageDoor, "acceleration.active", activeHandler)
    
    //Used to make sure the door is closed at night
    def hours = hourCron ?: "18-21"
    def cron = "0 0 ${hours} * * ?"
    log.debug "Scheduling cron for '${cron}'"
    schedule(cron, verifyClosedAtNight)
}

//event methods
def activeHandler(evt) {
	log.debug "The garage door is active"
    if(isClosed()) {
        log.debug "The door is closed and acceleration, so it must be opening"
        runIn(60*60, "verifyDoorNotLeftOpen")
    } else {
    	log.debug "The door is open and acceleration, so it must be closing"
        runIn(2*60, "verifyClosingWorked")
    }
}

//scheduled methods
def verifyClosingWorked() {
	if(isClosed()) {
    	log.debug "Looks like the door was successfully closed"
        //messageMe("Garage OK - closed as expected")
    } else {
    	log.debug "Looks like the door did not close"
        messageMe("Check the garage door. It may not have closed.")
    }
}

def verifyDoorNotLeftOpen() {
	if(isClosed()) {
    	log.debug "Looks like the door is closed"
        //messageMe("Garage OK - not left open")
    } else {
    	log.debug "Looks like the door is still open"
        messageMe("Check the garage door. It has been open for an hour.")
    }
}

def verifyClosedAtNight() {
	if(isClosed()) {
    	log.debug "The garage door is closed"
        //messageMe("Garage OK - cron")
    } else {
    	log.debug "The garage door is still open"
        messageMe("Check the garage door. It is still open.")
    }
}

//helper methods
Boolean isClosed() {
	garageDoor.latestValue('contact') == 'closed'
}

// Notification Methods
def messageMe(message) {
	if(recipients) {
		sendNotificationToContacts(message, recipients)
	}
}