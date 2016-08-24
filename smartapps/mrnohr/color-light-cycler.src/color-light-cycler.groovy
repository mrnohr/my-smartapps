/**
 *  Tower Light Cycler
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
    name: "Color Light Cycler",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "Cycle color lights",
    category: "My Apps",
    iconUrl: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/orange-bulb.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/orange-bulb.png",
    iconX3Url: "https://dl.dropboxusercontent.com/u/2256790/smartapp-icons/orange-bulb.png")


preferences {
	section("Control these bulbs") {
		input "bulbs", "capability.colorControl", title: "Which Bulbs?", required: true, multiple: true
	}
    section("With these colors") {
    	input "colors", "enum", title: "Which Colors?", required: true, multiple:true, options: ["Red","Green","Blue","Yellow","Orange","Purple","Pink"]
    }
    section("On this schedule") {
    	input "seconds", "number", title: "How long between color changes (seconds)?", required: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unschedule()
	initialize()
}

def initialize() {
	state.colorIndex == 0
	changeColor()
}

def changeColor() {
	if(state.colorIndex > colors.size()) {
    	state.colorIndex = 0
    }
    
    def color = colors[state.colorIndex]
    
    log.debug "Changing color to $color (index = ${state.colorIndex})"

	def bulbColor = 0
	if(color == "Blue") {
		bulbColor = 70//60
    } else if(color == "Green") {
        bulbColor = 39//30
	} else if(color == "Yellow") {
		bulbColor = 25//16
	} else if(color == "Orange") {
		bulbColor = 10
	} else if(color == "Purple") {
		bulbColor = 75
	} else if(color == "Pink") {
		bulbColor = 83
    }
    
	def newValue = [hue: bulbColor, saturation: 100, level: 100]
	log.debug "Setting bulb(s) to this new value: $newValue"
	bulbs*.setColor(newValue)
    
    state.colorIndex = state.colorIndex++
    runIn(seconds, "changeColor")
}