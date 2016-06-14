/**
 *  Two Light Toggle
 *
 *  Copyright 2013 Matt Nohr
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
    name: "Two Light Toggle",
    namespace: "mrnohr",
    author: "matt",
    description: "Toggle between 2 lights",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb.jpg",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg",
    iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/bulb%402x.jpg")

preferences() {
	return [
		sections: [
			[
				title: "With this master switch...",
				input: [
					[
						name: "switch1",
						title: "Which?",
						type: "capability.switch",
						description: "Tap to set",
						multiple: false
					]
				]
			],
			[
				title: "And this second switch...",
				input: [
					[
						name: "switch2",
						title: "Which?",
						type: "capability.switch",
						description: "Tap to set",
						multiple: false
					]
				]
			]
		]
	]
}

def installed() {
	log.debug "installed"
	commonSubscribe()
}

def updated() {
	log.debug "updated"
	unsubscribe()
	commonSubscribe()
}

def commonSubscribe() {
	subscribe(switch1.switch, [filterEvents: false])
	state.appTurningOff = false
}

def "switch"(evt) {
	log.info "Switch event: ${evt.value} - Type: ${evt.type} (physical: ${evt.isPhysical()})"
	log.debug "Current state: ${state.currentSwitchState}"

	if(evt.isPhysical()){
		if(evt.value == 'on'){
			handleOn()
		} else if(evt.value == 'off'){
			handleOff()
		}
	} else {
		log.info "Not taking action since this is not a physical event"
	}
}

private def handleOn(){
	//move to the next step in the cycle
	switch (state.currentSwitchState){
//		case switchStates.ON_ON:
//			manageLights(true, false)
//			state.currentSwitchState = switchStates.ON_OFF
//			break
		case switchStates.ON_OFF:
			manageLights(false, true)
			state.currentSwitchState = switchStates.OFF_ON
			break
		case switchStates.OFF_ON:
		default:
			manageLights(true, false)
			state.currentSwitchState = switchStates.ON_OFF
			break
	}
}

private def handleOff(){
    if(state.appTurningOff) {
        log.debug "Do nothing since the app is turning me off"
        state.appTurningOff = false
    } else {
        //then also turn off the other lights
        log.debug "Turn off all the lights"
        switch2.off()
        state.currentSwitchState = switchStates.OFF_OFF
    }
}

private def manageLights(boolean s1On, boolean s2On){
	def s1OnNow = (switch1.latestValue('switch') == 'on')
	def s2OnNow = (switch2.latestValue('switch') == 'on')

	log.debug "manage lights: $s1On, $s2On"
	log.debug "current state: $s1OnNow, $s2OnNow"

	if(s1On){
		//don't do anything since s1 is already on from the event
	} else {
		//record that it is this smartapp that is turning off the light
		// so we don't turn off any of the other lights
		state.appTurningOff = true
		switch1.off()
	}

	//switch 2
	if(s2On && !s2OnNow){ switch2.on() } 		//should be on but isn't
    else if (!s2On && s2OnNow){ switch2.off() }	//should be off but isn't
}

private def getSwitchStates() {
	//just map the states to ints to store in the state
	[
		ON_ON:0, OFF_OFF:1, 	//both the same
		ON_OFF:2, OFF_ON:3,		//combinations of 2 on
	]
}
