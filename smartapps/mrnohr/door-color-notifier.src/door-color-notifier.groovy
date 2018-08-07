/**
 *  Door Color Notifier
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
    name: "Door Color Notifier",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "Change a light to a certain color when a door opens",
    category: "My Apps",
    iconUrl: "https://www.dropbox.com/s/2prjfzikcz0p88m/lamp-4.png?raw=1",
    iconX2Url: "https://www.dropbox.com/s/2prjfzikcz0p88m/lamp-4.png?raw=1",
    iconX3Url: "https://www.dropbox.com/s/2prjfzikcz0p88m/lamp-4.png?raw=1")


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
    section("Which Door") {
      input "door1", "capability.contactSensor", title: "Which door?", multiple: false, required: true
      input "contactClosesTime", "number", title: "Turn off after this number of minutes", required: true
    }
    section("Which lamps?") {
      input "bulb1", "capability.colorControl", title: "Which lamp to control?", multiple: false, required: true
      input "bulb2", "capability.colorControl", title: "Which lamp to check when done?", multiple: false, required: true
      input "notifyColor", "enum", title: "Color On Open?", required: false, multiple: false, options: ["Red", "Green", "Orange", "Pink", "Blue", "Yellow", "Purple"]
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

// lifecycle methods
def installed() {
  log.debug "Installed with settings: ${settings}"

  initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"

  unsubscribe()
  unschedule()

  initialize()
}

def initialize() {
  log.trace "initialize"
  subscribe(door1, "contact.open", openHandler)
  subscribe(door1, "contact.closed", closedHandler)
}

//event handlers
def openHandler(evt) {
  log.trace "openHandler($evt.name: $evt.value)"
    if(allOk) {
        setColor()
        unschedule() //new open event, don't need any old scheduled color resets
    }
}

def closedHandler(evt) {
  log.trace "closedHandler($evt.name: $evt.value)"
    if(allOk) {
    runIn(contactClosesTime * 60, scheduledColorReset)
    }
}

// schedules
def scheduledColorReset() {
  def otherBulbState = bulb2.currentValue("switch")
  log.trace "scheduledReset ($otherBulbState)"

  if("on" == otherBulbState) {
    setWarmWhite() //assume this is the "default" state
  } else {
    bulb1.off()
  }
}

//color control
private setWarmWhite() {
  log.trace "setWarmWhite"
  bulb1.setLevel(100)
  bulb1.on()
  bulb1.setColorTemperature(2700)
}

private setColor() {
  log.trace "setColor($notifyColor)"
  def hueColor = 0
  def saturation = 100

  switch(notifyColor) {
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

  bulb1.setColor(value)
}

// Control the more options and when this should run
// copy/paste
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
