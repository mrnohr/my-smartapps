/**
 *  Lights On When Occupied
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
    name: "Lights On When Occupied",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "Using combo of contact and motion, determine when a room is occupied and have lights on during that time.",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg",
    iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg")


preferences {
	section("Sensors") {
		input "door1", "capability.contactSensor", title: "Door"
        input "motion1", "capability.motionSensor", title: "Motion"
	}
    section("Lights") {
    	input "lights", "capability.switch", title: "Lights", multiple: true
    }
    section("Notification Testing") {
    	input "phone", "phone", title: "Text me updates", required: false
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
	subscribe(door1, "contact", contactHandler)
    subscribe(motion1, "motion", motionHandler)
}

def contactHandler(evt) {
	def closed = (evt.value == 'closed')
    def motion = (motion1.latestValue('motion') == 'active')
	determineOccupation(closed, motion)
}

def motionHandler(evt) {
	def closed = (door1.latestValue('contact') == 'closed')
    def motion = (evt.value == 'active')
    determineOccupation(closed, motion)
}

def determineOccupation(boolean closed, boolean motion) {
	log.debug "determineOccupation - closed=$closed - motion=$motion"
	if(!closed && !motion) {
    	/*
        Door open
        No Motion
        
        Turn lights off right away? Or delay?
        */
    	unoccupied()
    } else if(closed && motion) {
    	/*
        Door closed
        Motion
        */
    	occupied()
    } else if(!closed && motion) {
    	/*
        Door open
        Motion
        */
    	occupied()
    } else if(closed && !motion) {
    	/*
        Door closed
        No motion
        
        Probably just in the shower or something
        But need to handle case where just closing door to keep kids out
        */
        occupied()
    }
}

def occupied() {
	log.debug "occupied check"
    if(!state.occupied) {
    	log.info "room was not occupied, but now it is"
    	state.occupied = true
        state.occupiedTime = currentDateString(true)
        lights.on()
        if(phone) {
        	log.debug "sending text that room is occupied"
        	sendSms(phone, "Room is occupied")
        }
    }
}

def unoccupied() {
	log.debug "unoccupied"
    if(state.occupied) {
	    log.info "room was occupied, but now it is not"
    	state.occupied = false
        recordHistory("${state.occupiedTime}-${currentDateString(false)}")
		log.debug state.history
        
        lights.off()
        if(phone) {
	        log.debug "sending text that room is open"
        	sendSms(phone, "Room is open")
        }
    }
}

def currentDateString(includeDate = true) {
	def dateFormat = includeDate ? 'MM/dd hh:mm:ss a' : 'hh:mm:ss a'
    def sdf = new java.text.SimpleDateFormat(dateFormat)
    sdf.setTimeZone(location.timeZone)
    sdf.format(new Date())
}

def recordHistory(String history) {
	if(!state.history) {
    	state.history = []
    }
    state.history = [history, *state.history.take(19)]
}