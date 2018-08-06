/**
 *  Downstairs Bulbs
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
    name: "Colored Bulb Controller",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "Control the Color Bulbs",
    category: "My Apps",
    iconUrl: "https://www.dropbox.com/s/2prjfzikcz0p88m/lamp-4.png?raw=1",
    iconX2Url: "https://www.dropbox.com/s/2prjfzikcz0p88m/lamp-4.png?raw=1",
    iconX3Url: "https://www.dropbox.com/s/2prjfzikcz0p88m/lamp-4.png?raw=1")


preferences {
	section("Devices") {
    	input "bulbs", "capability.colorControl", title: "Which lamp(s)?", multiple: true, required: true
        input "buttonDevice", "capability.button", title: "Minimote", multiple: false, required: true
        input "otherSwitches", "capability.switch", title: "Other lights to turn off", multiple: true, required: false
	}
    section("Button Information") {
    	paragraph "UPPER LEFT: Push to turn on to soft white\nHold to turn on lamps and off the other lights"
        paragraph "UPPER RIGHT: Push to turn off the lamps"
        paragraph "LOWER LEFT: Push to go to the next color\nHold to start the cycle of colors every 30 seconds and push again to stop cycle"
        paragraph "LOWER RIGHT: Push to go to the previous color"
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
	state.colorIndex = 0
	subscribe(buttonDevice, "button", buttonHandler)
}

// Event handlers
def buttonHandler(evt) {
	def buttonNumber = evt.data
	def value = evt.value
	log.debug "buttonEvent: $evt.name = $evt.value ($evt.data)"
	log.debug "button: $buttonNumber, value: $value"

	def recentEvents = buttonDevice.eventsSince(new Date(now() - 2000)).findAll {
		it.value == evt.value && it.data == evt.data
	}
	log.debug "Found ${recentEvents.size() ?: 0} events in past 2 seconds"

	if(recentEvents.size <= 1) {
		handleButton(extractButtonNumber(buttonNumber), value)
	} else {
		log.debug "Found recent button press events for $buttonNumber with value $value"
	}
}

// Color control
private setWarmWhite() {
	bulbs.setLevel(100)
	bulbs.on()
	bulbs.setColorTemperature(2700)
}

private nextColor() {
	def colorList = getColorList()
    def currentIndex = state.colorIndex
    log.debug "Next color: Current index = $state.colorIndex"
    if(currentIndex >= (colorList.length-1)) {
    	currentIndex = 0
    } else {
    	currentIndex++
    }
    log.debug "Next color: new index = $currentIndex"
    state.colorIndex = currentIndex
    setColor(colorList[currentIndex])
}
private previousColor() {
	def colorList = getColorList()
    def currentIndex = state.colorIndex
    log.debug "Prev color: Current index = $state.colorIndex"
    if(currentIndex <= 0) {
    	currentIndex = colorList.length - 1
    } else {
    	currentIndex--
    }
    log.debug "Prev color: new index = $currentIndex"
    state.colorIndex = currentIndex
    setColor(colorList[currentIndex])
}

private startCycle() {
	state.inCycle = true
    state.cycleCount = 0
    nextColor()
    runIn(30, "changeOnSchedule")
}

private changeOnSchedule() {
	if(state.inCycle) {
    	nextColor()
        def cycleCount = state.cycleCount
        
        if(cycleCount > 20) {
        	state.inCycle = false
        } else {
        	cycleCount++
            state.cycleCount = cycleCount
        	runIn(30, "changeOnSchedule")
        }
    }
}

private String[] getColorList() {
    ["Red", "Green", "Orange", "Pink", "Blue", "Yellow", "Purple"]
}

private setColor(String color) {

	def hueColor = 0
	def saturation = 100

	switch(color) {
		case "Blue":
			hueColor = 70
			break;
		case "Green":
			hueColor = 39
			break;
		case "Yellow":
			hueColor = 25
			break;
		case "Orange":
			hueColor = 10
			break;
		case "Purple":
			hueColor = 75
			break;
		case "Pink":
			hueColor = 83
			break;
		case "Red":
			hueColor = 100
			break;
	}

	def value = [switch: "on", hue: hueColor, saturation: saturation, level: level as Integer ?: 100]
	log.debug "color = $value"

	bulbs.each {
	    log.debug "$it.displayName, setColor($value)"
		it.setColor(value)
	}
}

// button methods
def extractButtonNumber(data) {
	def buttonNumber
	//TODO must be a better way to do this. Data is like {buttonNumber:1}
	switch(data) {
		case ~/.*1.*/:
			buttonNumber = 1
			break
		case ~/.*2.*/:
			buttonNumber = 2
			break
		case ~/.*3.*/:
			buttonNumber = 3
			break
		case ~/.*4.*/:
			buttonNumber = 4
			break
	}
	return buttonNumber
}

def handleButton(buttonNumber, value) {
	switch([number: buttonNumber, value: value]) {
	//value is 'pushed' or 'held'
		case { it.number == 1 && it.value == 'pushed'}:
            setWarmWhite()
			break
		case { it.number == 1 && it.value == 'held'}:
            setWarmWhite()
            otherSwitches?.off()
			break
		case { it.number == 2 }:
			bulbs.off()
			break
		case { it.number == 3 && it.value == 'pushed' }:
			if(state.inCycle) {
            	state.inCycle = false
            } else {
				nextColor()
            }
			break
        case { it.number == 3 && it.value == 'held' }:
			startCycle()
			break
		case { it.number == 4 }:
			previousColor()
			break
		default:
			log.debug "Unhandled command: $buttonNumber $value"
	}
}