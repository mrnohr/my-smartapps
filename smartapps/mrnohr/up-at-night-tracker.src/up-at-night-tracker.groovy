/**
 *  Up At Night Tracker
 *
 *  Author: Matt Nohr
 *  Date: 2014-04-03
 */

// Automatically generated. Make future change here.
definition(
    name: "Up At Night Tracker",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "How many times a night do the kids get up",
    category: "My Apps",
    iconUrl: "https://www.dropbox.com/s/6xqbuscc184z5p3/bed-1.png?raw=1",
    iconX2Url: "https://www.dropbox.com/s/6xqbuscc184z5p3/bed-1.png?raw=1",
    iconX3Url: "https://www.dropbox.com/s/6xqbuscc184z5p3/bed-1.png?raw=1")

preferences {
	section("Doors") {
		input "doors", "capability.contactSensor", multiple: true,
        	title: "Which doors to watch?"
	}
    section("When?") {
    	input "monitorMode", "mode",
        	title: "During which mode?"
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
    subscribe(doors, "contact.open", "doorOpenHandler")
    subscribe(location, "modeChangeHandler")
}

// TODO: implement event handlers
def modeChangeHandler(evt) {
	log.debug "In modeChangeHandler for $evt.name = $evt.value"
    if(evt.value == monitorMode) {
    	log.debug "Starting monitoring of doors"
    	state.monitoring = true
        state.openCount = 0
    } else {
    	if(state.monitoring) {
        	log.debug "Done monitoring doors for the night"
            state.monitoring = false
            if(state.openCount == 0) {
            	sendPush("Congratulations! Kids didn't wake up last night!")
                state.lastQuietNight = now()
            } else {
            	def timeWord = (state.openCount == 1 ? "time" : "times")
            	sendPush("Kids were up ${state.openCount} $timeWord last night");
            }
        }
    }
}
def doorOpenHandler(evt) {
	log.debug "In doorOpenHandler for $evt.name = $evt.value"
    state.openCount = state.openCount + 1
    log.debug "So far door(s) have opened ${state.openCount} times"
}