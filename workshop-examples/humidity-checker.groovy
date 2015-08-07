/**
 *  Humidity Checker
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
		name: "Humidity Checker",
		namespace: "mrnohr",
		author: "Matt Nohr",
		description: "Compare the humidity between rooms",
		category: "My Apps",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

/**
 * In the In the virtual SmartApp Workshop on 8/6/14, a request was made to make an app that monitors humidity between
 * two rooms and turn on a switch if the difference is over a specified threshold.
 *
 * Written in about 10 minutes, so use at your own risk
 */
preferences {
	section("When the humidity in this here...") {
		input "monitoredRoom", "capability.relativeHumidityMeasurement"
	}
	section("Is higher than this room...") {
		input "baseRoom", "capability.relativeHumidityMeasurement"
	}
	section("By this amount...") {
		input "percentageDifferance", "number", title: "Percentage difference", defaultValue: 10
	}
	section("Turn on this switch...") {
		input "switch1", "capability.switch"
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
	//Need to know anytime the humidity changes
	subscribe(monitoredRoom, "humidity", "humidityHandler")
}

def humidityHandler(evt) {
	//get the humidity value for the event, which is for the "monitored" room
	def monitoredValue = evt.value.toDouble()

	//get the baseline humidity value for comparison
	def baseValue = baseRoom.latestValue("humidity").toDouble()

	log.info "Monitored value = $monitoredValue - base value = $baseValue - differance = $percentageDifferance"

	//If the difference is higher than the allowed value, then turn on the switch
	if((monitoredValue - baseValue) > percentageDifferance) {
		switch1.on()
	}
}
