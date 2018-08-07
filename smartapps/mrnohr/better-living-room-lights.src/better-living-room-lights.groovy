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
	iconUrl: "https://www.dropbox.com/s/92tx2muwd0ptvt6/lights.png?raw=1",
	iconX2Url: "https://www.dropbox.com/s/92tx2muwd0ptvt6/lights.png?raw=1",
	iconX3Url: "https://www.dropbox.com/s/92tx2muwd0ptvt6/lights.png?raw=1")


preferences {
	section("Control these lights...") {
		input "lights", "capability.switch", multiple: true
	}

	section("During these times...") {
		paragraph "This will run at :02, :17, :32, and :47 each hour between 5:00am and 11:00pm. Plan accordingly."
		input "morningOn", "time", title: "Turn on at this time in the morning"
		input "midMorningOff", "time", title: "Turn off at this time in the morning if nobody is home"
		input "afternoonOn", "time", title: "Turn on at this time in the afternoon if nobody is home"
		input "eveningOff", "time", title: "Turn off at this time at night"
	}
	section("With these light levels...") {
		input "lightSensor", "capability.illuminanceMeasurement", title: "Light sensor"
		input "turnOnBrightness", "number", title: "Turn on under this lux (default 100)", required: false
	}
	section("Notifications for fine tuning") {
		input "debugLevel", "enum", title: "Send notifications for fine tuning?", options: ["every":"Every Execution","changes":"Only On Change","none":"Never"]
		input("recipients", "contact", title: "Send notifications to") {
			input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
			input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, options: ["Yes", "No"]
		}
	}
}

// *********** Lifecycle methods
def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unschedule()
	initialize()
}

def initialize() {
	// 9     9th second (random)
	// 2/15  Every 15 minutes offset by 2 (2, 17, 32, 47)
	// 5-23  Between the hours of 5:00 and 23:00
	schedule("9 2/15 5-23 * * ?", scheduleHandler)
}

// *********** Schedule methods

def scheduleHandler() {
	// control what notification is sent
	boolean sendMessage = true
	boolean isStateChange = false
	def currentLux = lightSensor.currentValue("illuminance")
	String logMessage = "Ran, No Change: lux $currentLux"

	if(withinOuterTime()) {
		if(isDarkEnough()) {
			if(isCurrentlyHomeMode() || !withinInnerTime()) {
				isStateChange = turnOn()
				if(isStateChange) {
					logMessage = "Dark enough ($currentLux), turned on lights"
				}
			} else {
				isStateChange = turnOff()
				if(isStateChange) {
					logMessage = "Away ($currentLux), turned off lights"
				}
			}
		} else {
			isStateChange = turnOff()
			if(isStateChange) {
				logMessage = "Bright enough ($currentLux), turned off lights"
			}
		}
	} else {
		isStateChange = turnOff()
		if(isStateChange) {
			sendMessage = true
			logMessage = "Outside of time window, turned off lights"
		} else {
			//prevent getting messages all night
			sendMessage = false
		}
	}

	log.trace "scheduleHandler: $logMessage (stateChange = $isStateChange, sendMessage = $sendMessage)"
	if(sendMessage) {
		messageMe(logMessage, isStateChange)
	}
}

// *********** Light control methods

boolean turnOn() {
	boolean isStateChange = false
	def currSwitches = lights.currentValue("switch")
	def offSwitches = currSwitches.findAll { switchVal ->
		switchVal == "off" ? true : false
	}

	if(offSwitches) {
		lights.on()
		isStateChange = true
	}
	log.trace "turnOn = $isStateChange"
	return isStateChange
}

boolean turnOff() {
	boolean isStateChange = false
	def currSwitches = lights.currentValue("switch")
	def onSwitches = currSwitches.findAll { switchVal ->
		switchVal == "on" ? true : false
	}

	if(onSwitches) {
		lights.off()
		isStateChange = true
	}
	log.trace "turnOff = $isStateChange"
	return isStateChange
}

// *********** Time methods

private boolean withinOuterTime() {
	def result = true
	if (morningOn && eveningOff) {
		def currTime = now()
		def start = timeToday(morningOn, location?.timeZone).time
		def stop = timeToday(eveningOff, location?.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "withinOuterTime = $result"
	result
}

private boolean withinInnerTime() {
	def result = true
	if (midMorningOff && afternoonOn) {
		def currTime = now()
		def start = timeToday(midMorningOff, location?.timeZone).time
		def stop = timeToday(afternoonOn, location?.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "withinInnerTime = $result"
	result
}

// *********** Mode methods
private boolean isCurrentlyHomeMode() {
	boolean result = ("Home" == location.mode)
	log.trace "isHomeMode = $result"
	return result
}

// *********** Illuminance methods
private boolean isDarkEnough() {
	def turnOnLux = turnOnBrightness ?: 100
	def currentLux = lightSensor.currentValue("illuminance")
	boolean result = currentLux < turnOnLux
	log.trace "isDarkEnough = $result"
	return result
}

// *********** Notification methods
private void messageMe(String message, boolean isStateChange) {
	boolean sendMessage = false
	if(debugLevel == "every") {
		sendMessage = true;
	} else if (debugLevel == "changes" && isStateChange) {
		sendMessage = true;
	}

	if(sendMessage && recipients) {
		log.info message
		sendNotificationToContacts("BL: $message", recipients)
	}
}
