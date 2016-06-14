/**
 *  Once a Day Motion Alert
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
    name: "Once a Day Motion Alert",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "Get notified once a day if there is motion",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/motion%402x.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/motion%402x.png",
    iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/motion%402x.png")


preferences {
	section("Title") {
		input "motion1", "capability.motionSensor"
        input "starting", "time", title: "Starting", required: false
		input "ending", "time", title: "Ending", required: false
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
	subscribe(motion1, "motion.active", "motionHandler")
}

def motionHandler(evt){
	if(!hasRunToday() && timeOk){
    	state.lastRun = now()
        sendPush("There was motion near ${motion1.displayName}")
    }
}

private getTimeOk() {
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

private hasRunToday() {
	def hasRun = false
	if(state.lastRun) {
        def sdf = new java.text.SimpleDateFormat('yyyy-MM-dd')
        sdf.setTimeZone(location.timeZone)
        
        log.debug "Last run: ${state.lastRun}"
        
        def lastRunDate = new Date(state.lastRun)
        log.debug "Last run date: $lastRunDate"
        
        def lastRunString = sdf.format(lastRunDate)
        log.debug "Last run string: $lastRunString"
        
        def nowString = sdf.format(new Date())
        log.debug "Now string: $nowString"
        
        hasRun = (lastRunString >= nowString)
    }
    log.trace "hasRun = $hasRun"
    return hasRun
}