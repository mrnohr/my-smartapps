/**
 *  Extra Phrase Execution on Departure
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
		name: "Extra Phrase Execution on Departure",
		namespace: "mrnohr",
		author: "Matt Nohr",
		description: "Execute a phrase when leaving",
		category: "My Apps",
		iconUrl: "https://dl.dropboxusercontent.com/u/2256790/st-icons/house.png",
		iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/st-icons/house.png",
		iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/st-icons/house.png")

preferences {
	page(name: "firstPage")
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def firstPage() {
	dynamicPage(name: "firstPage", install: true, uninstall: true) {
		section("Execute a phrase on departure") {
			input(name: "presence1", type: "capability.presenceSensor", title: "Presence Sensor")
			input(name: "phrase", type: "enum", title: "Execute The Phrase", options: location.helloHome.getPhrases().label)
		}

		section("Label") {
			label title: "Assign a name", required: false
		}

		def timeLabel = timeIntervalLabel()
		section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
					options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
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
	subscribe(presence1, "presence.not present", notPresent)
}

def notPresent(evt) {
	log.debug "$evt.name: $evt.value"
	if(allOk) {
		location.helloHome.execute(phrase)
	}
}

// TODO - centralize somehow
private hideOptionsSection() {
	(starting || ending || days || modes) ? false : true
}

private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if(days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if(location.timeZone) {
			df.setTimeZone(location.timeZone)
		} else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if(starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private timeIntervalLabel() {
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}
