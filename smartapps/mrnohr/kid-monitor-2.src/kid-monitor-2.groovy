/**
 *  Kid Monitor
 *
 *  Author: matt
 *  Date: 2014-03-20
 */

// Automatically generated. Make future change here.
definition(
    name: "Kid Monitor 2",
    namespace: "mrnohr",
    author: "matt",
    description: "Individual Kid Monitor",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/kids%402x.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/kids%402x.png",
    iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/kids%402x.png")

preferences {
	section("Which Room?") {
    	input "roomName", "text", required: true,
        	title: "Who's room is this?"
    }
    section("Select Devices") {
        input "roomDoor", "capability.contactSensor", required: true,
        	title: "Door"
        input "roomSound", "device.arduinoSoundShield", required: false,
        	title: "Listen In"
        input "lamps", "capability.switch", multiple: true, required: false,
        	title: "Lamps"
    }
    
    section("Notification Settings") {
    	input "notifyWhenOpen", "bool", required: true, default: true,
        	title: "Notify when the door opens?"
        input "notifyWithNoise", "bool", required: true, default: true,
        	title: "Notify when there is noise?"
        input "notifyNoiseThresholdCount", "number", required: false,
        	title: "With noise, how many noises before notification?"
        input "notifyNoiseThresholdTimePeriod", "number", required: false,
            title: "With noise, the above count in how many minutes?"
        input "notifyOffWhenClear", "bool", required: true, default: true,
        	title: "Notify when all clear?"

    }
    
    section("Lamp Settings") {
    	input "lampWhenOpen", "bool", required: true, default: true,
        	title: "Turn on when the door opens?"
        input "lampWithNoise", "bool", required: true, default: true,
        	title: "Turn on when there is noise?"
        input "lampOffWhenClear", "bool", required: true, default: true,
        	title: "Turn off when all clear?"
        input "lampAutoOffMinutes", "number", required: false,
        	title: "Automatically turn off after this many minutes"
    }
    
    section("When To Monitor?") {
        input "starting", "time", required: false,
        	title: "Starting"
        input "ending", "time", required: false,
        	title: "Ending"
    }
    
    section("Reset?") {
    	input "resetMinutes", "number", required: false,
        	title: "Reset after this many minutes?"
    }
}

// setup methods
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
    subscribe(roomDoor, "contact", contactHandler)
    subscribe(roomSound, "voiceStatus", noiseHandler)
}

// event handlers
def noiseHandler(evt) {
	if(roomSound && getTimeOk()) { // if noise is set up and 
    	log.debug "Noise $evt.value"
        def doorOpen = ("open" == roomDoor.latestValue("contact"))
        if(!doorOpen) { // only when door is closed
	   		if(evt.value == "active") { // there is noise
                if(isNoiseThresholdMet()) {
                	state.noiseAlert = true
                    if(notifyWithNoise) {
                        def message = "There is noise in ${getPossessive(roomName)} room"
                        log.info message
                        sendPush(message)
                    }
                    if(lampWithNoise) {
                        log.info "Turning on lamp due to noise in ${getPossessive(roomName)} room"
                        lamps?.on()
                        if(lampAutoOffMinutes) {
                            runIn(lampAutoOffMinutes * 60, "lampsOffSchedule")
                        }
                    }
                }
            } else { // no more noise
            	if(state.noiseAlert) {
                	state.noiseAlert = false
                    if(notifyOffWhenClear) {
                        def message = "It is all quiet in ${getPossessive(roomName)} room"
                        log.info message
                        sendPush(message)
                    }
                    if(lampOffWhenClear) {
                        log.info "Turning off lamp because it is quiet in ${getPossessive(roomName)} room"
                        lamps?.off()
                    }
                }
            }
        }
    }
}

def contactHandler(evt) {
	if(getTimeOk()) {
    	log.debug "Door $evt.value"
    	if(evt.value == "open") {
        	if(notifyWhenOpen) {
            	def lastNotifyTime = state.lastDoorOpenNotify ?: 0
                def notifyThreshold = 2 * 60 * 1000 //2 minutes
                def timePast = now() - lastNotifyTime
                
            	if(timePast > notifyThreshold){
                    def message = "${getPossessive(roomName)} door is open"
                    log.info message
                    sendPush(message)
                    state.lastDoorOpenNotify = now()
                } else {
                	log.debug "Not sending notifiction on door since only $timePast ms have elapsed"
                }
            }
            if(lampWhenOpen) {
                log.info "Turning on lamp due to ${getPossessive(roomName)} door opening"
                lamps?.on()
                if(lampAutoOffMinutes) {
               		runIn(lampAutoOffMinutes * 60, "lampsOffSchedule")
                }
            }
        } else {
        	//door closed
            
            //only alert if the door has been opened in past X minutes
            def resetStatus = true
            if(resetMinutes) {
            	def resetMillis = resetMinutes * 60 * 1000
            	def recentOpens = roomDoor.eventsSince(new Date(now() - resetMillis)).findAll{it.name == "contact" && it.value == "open"}
                log.debug "Found ${recentOpens.size()} open events"
                resetStatus = (recentOpens.size() > 0)
                
            }
            if(resetStatus){
                if(notifyOffWhenClear) {
                    def message = "${getPossessive(roomName)} door is closed"
                    log.info message
                    sendPush(message)
                }
                if(lampOffWhenClear) {
                    log.info "Turnng off lamp because ${getPossessive(roomName)} door is closed"
                    lamps?.off()
                }
            }
        }
    }
}

// scheduled methods
def lampsOffSchedule() {
	lamps?.off()
}

// helper methods
def getPossessive(name) {
	if(name.endsWith('s')) {
    	return "${name}'"
    } else {
    	return "${name}'s"
    }
}

def isNoiseThresholdMet() {
	def noiseThresholdMet = false
    if(!notifyNoiseThresholdCount || !notifyNoiseThresholdTimePeriod) {
        noiseThresholdMet = true
    } else {
        //check to see if we've met the threshold
        def alerts = roomSound.eventsSince(new Date(now() - notifyNoiseThresholdTimePeriod * (1000 * 60)))
        if(alerts.findAll{it.value == "active"}.size() >= notifyNoiseThresholdCount) {
            noiseThresholdMet = true
        }
    }
    log.debug "Noise threshold met = $noiseThresholdMet"
    return noiseThresholdMet
}

def getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}