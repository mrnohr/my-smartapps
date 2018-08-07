/**
 *  Turn Light Back On
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
    name: "Turn Light Back On",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "When a light turns off, turn it back on",
    category: "My Apps",
    iconUrl: "https://www.dropbox.com/s/kd8keb0553nrnbz/light-bulb-1.png?raw=1",
    iconX2Url: "https://www.dropbox.com/s/kd8keb0553nrnbz/light-bulb-1.png?raw=1",
    iconX3Url: "https://www.dropbox.com/s/kd8keb0553nrnbz/light-bulb-1.png?raw=1")


preferences {
	section("Light") {
		input "switch1", "capability.switch"
        input "delay", "number", title: "After how many seconds?", required: false
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
	subscribe(switch1, "switch.off", offHandler)
}

def offHandler(evt) {
	def seconds = delay ?: 60
	runIn(seconds, scheduledTurnOn)
}

def scheduledTurnOn() {
	switch1.on()
}