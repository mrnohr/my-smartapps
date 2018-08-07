/**
 *  Garage Lights
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
		name: "Garage Lights",
		namespace: "mrnohr",
		author: "Matt Nohr",
		description: "Using combo of contact and motion, determine when a garage is occupied and have lights on during that time.",
		category: "My Apps",
		iconUrl: "https://www.dropbox.com/s/hkqbk4smns2dczu/light-bulb-3.png?raw=1",
		iconX2Url: "https://www.dropbox.com/s/hkqbk4smns2dczu/light-bulb-3.png?raw=1",
		iconX3Url: "https://www.dropbox.com/s/hkqbk4smns2dczu/light-bulb-3.png?raw=1")


preferences {
	section("Sensors") {
    	input "garageDoors", "capability.contactSensor", title: "Garage Door", multiple: true
		input "door1", "capability.contactSensor", title: "Interior Door"
		input "motion1", "capability.motionSensor", title: "Motion Sensor"
	}
	section("Lights") {
		input "lights", "capability.switch", title: "Lights", multiple: true
	}
    section("Delays") {
    	input "doorDelay", "number", title: "Turn off after door close delay (minutes)"
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
	subscribe(garageDoors, "contact", garageDoorHandler)
	subscribe(door1, "contact", interiorDoorHandler)
	subscribe(motion1, "motion", motionHandler)
}

// event handlers
def interiorDoorHandler(evt) {
	def closed = (evt.value == 'closed')
	def motion = (motion1.latestValue('motion') == 'active')

	log.debug "Interior Door Event: ${evt.value} - motion=$motion, closed=$closed"
    
	if(motion || !closed) {
    	lightsShouldBeOn()
    } else {
    	runIn(60*doorDelay, "doorScheduleHandler")
    }
}

def motionHandler(evt) {
	def closed = (door1.latestValue('contact') == 'closed')
	def motion = (evt.value == 'active')
    
    log.debug "Motion event: ${evt.value} - motion=$motion, closed=$closed"

	if(motion || !closed) {
    	lightsShouldBeOn()
    } else {
    	lightsShouldBeOff()
    }
}

def garageDoorHandler(evt) {
	log.debug "Garage door event: ${evt.value}"
	if(evt.value == 'open' || evt.value == 'opening') {
    	lightsShouldBeOn()
    } else {
    	runIn(60*doorDelay, "doorScheduleHandler")
    }
}

// schedule handlers
def doorScheduleHandler() {
	def closed = (door1.latestValue('contact') == 'closed')
    def motion = (motion1.latestValue('motion') == 'active')
    
    log.debug "Schedule - door closed $doorDelay minutes ago - motion=$motion, closed=$closed"
    if(motion || !closed) {
    	lightsShouldBeOn()
    } else {
    	lightsShouldBeOff()
    }
}

// light handlers
def lightsShouldBeOn() {
	log.debug "Lights should be on"
    lights.on()
    /*
	lights.each { light ->
    	if(light.latestValue('switch') == 'off') {
        	light.on()
        }
    }
    */
}

def lightsShouldBeOff() {
	log.debug "Lights should be off"
    lights.off()
    /*
	lights.each { light ->
    	if(light.latestValue('switch') == 'on') {
        	light.off()
        }
    }
    */
}