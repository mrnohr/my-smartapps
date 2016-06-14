/**
 *  Thermostat Mode Change Reporter
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
    name: "Thermostat Mode Change Reporter",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "When the thermostat mode changes (from Cool to Off for example), send a notification",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/thermometer-icon.jpg",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/thermometer-icon.jpg"
)

preferences {
	section("Choose a thermostat") {
		input "thermostat", "capability.thermostat", multiple: false, required: true
	}
}

// initialize methods
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
	//subscribe to the thermostat events I want to monitor
    subscribe(thermostat, "thermostatMode", thermostatEventHandler) //off, heat, cool
}

// event handlers
def thermostatEventHandler(evt) {
	log.debug "thermostatEventHandler - $evt.name = $evt.value"

    // since you need to cycle through, don't send message right away
    def secs = 5
    log.debug "will check the value in $secs secs and report if different"
    runIn(secs, "sendNotification", [overwrite: false])
}

def sendNotification() {
	def currentMode = thermostat.latestValue("thermostatMode")
    log.debug "in sendNotification, going to see if $currentMode == ${atomicState.lastMode}"

    if(currentMode != atomicState.lastMode) {
    	def msg
    	if(currentMode == 'cool') {
        	msg = getFormattedMessage('A/C', 'on')
        } else if (currentMode == 'heat') {
        	msg = msg = getFormattedMessage('A/C', 'off')
        } else if (currentMode == 'off') {
        	if(atomicState.lastMode == 'cool') {
	            msg = getFormattedMessage('A/C', 'off')
            } else if (atomicState.lastMode == 'heat') {
            	msg = getFormattedMessage('heat', 'off')
            }
        } else {
        	msg = "The thermostat is in $currentMode mode"
        }

        atomicState.lastMode = currentMode

        log.info msg
        sendNotification(msg, [method:'push'])
    } else {
    	log.info "The thermostat mode has not changed"
    }
}

def getFormattedMessage(String heatOrCool, String onOrOff) {
	return "The $heatOrCool is now $onOrOff at ${location.name}"
}
