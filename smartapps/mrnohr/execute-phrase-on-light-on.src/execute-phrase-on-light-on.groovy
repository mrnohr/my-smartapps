/**
 *  Execute Phrase on Light On
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
		name: "Execute Phrase on Light On",
		namespace: "mrnohr",
		author: "Matt Nohr",
		description: "Execute a phrase when a light turns on",
		category: "My Apps",
		iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/home%402x.png",
		iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/home%402x.png",
		iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/home%402x.png")

preferences {
	page(name: "firstPage")
}

def firstPage() {
	dynamicPage(name: "firstPage", install: true, uninstall: true) {
		section("Execute a phrase when this switch turns on") {
			input(name: "switch1", type: "capability.switch", title: "Which Switch")
			input(name: "phrase", type: "enum", title: "Execute The Phrase", options: location.helloHome.getPhrases().label)
		}

		section("Label") {
			label title: "Assign a name", required: false
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
	subscribe(switch1, "switch.on", lightHandler)
}

def lightHandler(evt) {
	log.debug "$evt.name: $evt.value"
	location.helloHome.execute(phrase)

	//turn back off the light
	runIn(60 * 5, turnOffLight)
}

def turnOffLight() {
	switch1.off()
}
