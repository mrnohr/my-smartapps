/**
 *  Mode Change Tracker
 *
 *  Author: Matt Nohr
 *  Date: 2014-04-03
 */

definition(
    name: "Mode Change Tracker",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "Send notification when mode changes",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bed%402x.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bed%402x.png",
    iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bed%402x.png")

preferences {
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
	initialize()
}

def initialize() {
    subscribe(location, "modeChangeHandler")
}

def modeChangeHandler(evt) {
	log.debug "In modeChangeHandler for $evt.name = $evt.value"
    def msg = "Mode changed to ${evt.value} (state=${state.lastMode}, change=${evt.isStateChange()})"
    log.info msg
    state.lastMode = evt.value
    
    if(recipients) {
		sendNotificationToContacts(msg, recipients)
	}
}
