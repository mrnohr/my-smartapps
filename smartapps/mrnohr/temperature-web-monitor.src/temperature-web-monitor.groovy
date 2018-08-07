/**
 *  Temperature Monitor
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
 
 /**
  * Requires OAuth!
  */
definition(
    name: "Temperature Web Monitor",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "What is the temp?",
    category: "My Apps",
    iconUrl: "https://www.dropbox.com/s/b835d7il9r417sz/temperature-1.png?raw=1",
    iconX2Url: "https://www.dropbox.com/s/b835d7il9r417sz/temperature-1.png?raw=1",
    iconX3Url: "https://www.dropbox.com/s/b835d7il9r417sz/temperature-1.png?raw=1",
    oauth: true)


preferences {
	section ("Pick the temperature monitors") {
		input "monitors1", "capability.temperatureMeasurement", title: "Upstairs", multiple: true, required: false
        input "monitors2", "capability.temperatureMeasurement", title: "Main Floor", multiple: true, required: false
        input "monitors3", "capability.temperatureMeasurement", title: "Basement", multiple: true, required: false
	}
}

mappings {
	path("/status") {
    	action: [
      		GET: "listStatus"
		]
	}
}

// Lifecycle
def installed() {
	log.debug "Installed with settings: ${settings}"
}

def updated() {
	log.debug "Updated with settings: ${settings}"
}

// Endpoints
def listStatus() {
	def html = buildHTML()
    render contentType: 'text/html', data: html
}

// HTML
String buildHTML() {
	String html = buildHeader()

	html+= buildFloorHTML('Upstairs', monitors1)
	html+= buildFloorHTML('Main Floor', monitors2)
	html+= buildFloorHTML('Basement', monitors3)

	html+= buildFooter()

	return html
}

String buildHeader() {
	return """
        <!doctype html>
        <html lang="en">
        <head>
            <meta charset="utf-8">
            <title>Temperature Monitor</title>
			${buildStyle()}
        </head>
        <body>
    """
}

String buildFooter() {
	return """
        </body>
        </html>
    """
}

String buildStyle() {
	Map colors = getColors()
	return """
        <style>
        body {
            background: ${colors.background};
            margin: 0;
            padding: 20px;
            font-family: "HelveticaNeue-Light", "Helvetica Neue Light", "Helvetica Neue", Helvetica, Arial, "Lucida Grande", sans-serif;
            font-weight: 300;
            font-size: 31px;
        }
        .caption {
            display: block;
            width: 100%;
            background: ${colors.header};
            height: 70px;
            padding-left: 10px;
            color: #fff;
            font-size: 60px;
        	line-height: 70px;
            text-shadow: 1px 1px 1px rgba(0,0,0,.3);
            box-sizing: border-box;
        }
        .table {
            display: table;
            width: 100%;
            background: ${colors.row};
            margin: 0;
            box-sizing: border-box;
        }
        .row {
            display: table-row;
            background: ${colors.row};
        }
        .header-row {
            background: ${colors.subHeader};
            color: #fff;
        }
        .cell {
            display: table-cell;
            padding: 6px;
            border-bottom: 1px solid #e5e5e5;
            width: 33%;
        }
        .cell-centered {
        	text-align: center;
        }
        .space {
            padding: 6px;
        }
        .warning {
            background: #DB4658;
            color: #ffffff;
            padding-left: 10px;
            padding-right: 10px;
        }
        </style>
    """
}

String buildFloorHTML(String title, List monitorList) {
	def values = getValues(monitorList)
    
    String html = """
        <div class="caption">${title}</div>
        <div class="table">
            <div class="header-row row">
                <span class="cell">Room</span>
                <span class="cell cell-centered">Temperature</span>
                <span class="cell cell-centered">Updated</span>
            </div>
    """

	values.each {
    	html += """
        <div class="row">
            <span class="cell">${formatName(it.name)}</span>
            <span class="cell cell-centered">${formatValue(it.value)}</span>
            <span class="cell cell-centered">${buildDate(it.lastUpdated)}</span>
        </div>
        """
    }

    html += """
	    </div>
	    <div class="space"></div>
    """
    
    return html
}

String buildDate(Date lastUpdated) {
	def acceptableWindow = 2 * 24 * 60 * 60 * 1000 //2 days

	if(!lastUpdated) {
    	return 'Unknown <span class="warning">!</span>'
    } else if((lastUpdated.time + acceptableWindow) < now()) {
    	return "${formatDate(lastUpdated)} <span class=\"warning\">!</span>"
    } else {
    	return formatDate(lastUpdated)
    }
}

// Colors
Map getColors() {
	return [
    	background: "#373E40",
        header: "#B0A084",
        subHeader: "#2F394D",
        row: "#D8DBE2"
     ]
}

// Get status
List getValues(monitorList) {
	def status = []
    monitorList.each {
    	def tempState = it.currentState("temperature")
	    //TODO check latest state
		status << [name: it.displayName, value: tempState.numberValue, lastUpdated: tempState.date]
	}
    return status
}

// Format Methods
String formatDate(Date d) {
    String format
    def oneDay = 24 * 60 * 60 * 1000
    if(d.time > now() - oneDay) {
    	//if in past day, just show hour/minute
    	format = "hh:mm a"
    } else {
    	//if older, then show date
    	format = "MM/dd/yy hh:mm a"
    }
    
	def df = new java.text.SimpleDateFormat(format)
	if (location.timeZone) {
		df.setTimeZone(location.timeZone)
	}

    df.format(d)
}

String formatName(String name) {
	return name - " Aeon" - " Motion" - " Temp"
}

String formatValue(Number value) {
	return value.toInteger().toString()
}