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
 import grails.converters.JSON
 
 /**
  * Requires OAuth!
  */
definition(
    name: "Temperature Web Chart",
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
		input "monitor1", "capability.temperatureMeasurement", title: "Temperature Monitor", multiple: false, required: false
	}
}

mappings {
	path("/graph") {
    	action: [
      		GET: "getGraph"
		]
	}
    path("/reading") {
    	action: [
      		GET: "extraReading"
		]
	}
}

// Lifecycle
def installed() {
	log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

    initialize()
}

def initialize() {
	if(!state.temperatures){
    	state.temperatures = []
    }
    if(!state.reset){
    	state.temperatures = []
    	state.reset = true
    }
    
	runEvery15Minutes(recordTemperatureSchedule)
}

// Scheduled event
def recordTemperatureSchedule(){
	//Get the current temperature
	def currentTemperature = monitor1.currentState("temperature").integerValue
    log.debug "Current temperature is $currentTemperature"
	if(currentTemperature instanceof List){
    	currentTemperature = currentTemperature[0]
    }
    log.debug "Current temperature 2 is $currentTemperature"
    
    //Get the current time
    def df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
    df.setTimeZone(location.timeZone)
    def formattedTime = df.format(new Date())
    
    //Update state
    state.temperatures << [time: formattedTime, temperature: currentTemperature]
    state.currentTemperature = currentTemperature
        
    //Drop old readings
    int maxReadings = 24*4
    if(state.temperatures.size() > maxReadings) {
		state.temperatures = state.temperatures.drop(state.temperatures.size() - maxReadings)
    }
    log.debug "State temperatures = ${state.temperatures}"
}

// Endpoints
def getGraph() {
	def html = buildHTML()
    render contentType: 'text/html', data: html
}

def extraReading() {
	recordTemperatureSchedule()	
}

// HTML
String buildHTML() {
	String html = buildHead()
	html+= buildBody()

	return html
}

String buildHead() {
	return """
        <meta charset="utf-8">
		<html>
        <head>
        	<title>Temperature Monitor</title>
            <link href="https://fonts.googleapis.com/css?family=PT+Sans+Narrow" rel="stylesheet">
            <style>
                ${buildStyle()}
            </style>
        </head>
    """
}

String buildStyle() {
	return """
        body {
            font: 20px Arial;
            margin: 10px;
            padding: 10px;
        }

        svg {
            background-color: rgb(255, 255, 255);
            padding: 40px;
        }

        path {
            fill: none;
            stroke: steelblue;
            stroke-width: 2;
        }

        .axis path, .axis line {
            fill: none;
            shape-rendering: crispEdges;
            stroke: #BBB;
            stroke-width: 4;
        }

        .axis text {
            fill: #766;
            font-family: 'PT Sans Narrow', sans-serif;
            font-size: 32px;
        }
        """
}

String buildBody() {
	return """
        <body>
        <!-- load the d3.js library -->
        <script src="https://d3js.org/d3.v4.min.js"></script>

        <script>
            // Get the data
            var data = ${getData().toString()};
            
            var parseDate = d3.timeParse("%Y-%m-%d %H:%M");
            data.forEach(function(d) {
                d.time = parseDate(d.time);
            });

            var width = 800;
            var height = 800;
            
            var x = d3.scaleTime().range([0, width]);
            var y = d3.scaleLinear().range([height, 0]);

            var xAxis = d3.axisBottom()
                .scale(x)
                .ticks(5);

            var yAxis = d3.axisLeft()
                .scale(y)
                .ticks(5);

            var valueline = d3.line()
                .x(function (d) {
                    return x(d.time);
                })
                .y(function (d) {
                    return y(d.temperature);
                });

            var svg = d3.select("body")
                .append("svg")
                .attr("width", width)
                .attr("height", height)
                .append("g");

            // Scale the range of the data
            x.domain(d3.extent(data,
                function (d) {
                    return d.time;
                }));
            y.domain([
            	68,
                78
            ]);

            svg.append("path") // Add the valueline path.
                .attr("d", valueline(data));
                
            svg.append("g") // Add the X Axis
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);

            svg.append("g") // Add the Y Axis
                .attr("class", "y axis")
                .call(yAxis);
        </script>
        
        <h1>Current Temperature is: ${getCurrentTemp()}</h1>
        <h1>Highest Temperature was: ${getHighTemp()}</h1>
        <h1>Lowest Temperature was: ${getLowTemp()}</h1>
        <h1>Average Temperature was: ${getAverageTemp()}</h1>
    </body>

    </html>
    """
}

String getData() {
/*
    List data = [
        [time: "1300", temperature: "75"],
        [time: "1400", temperature: "76"],
        [time: "1500", temperature: "78"],
        [time: "1600", temperature: "72"],
        [time: "1700", temperature: "74"]
    ]
    
    log.debug "JSON data = ${data as JSON}"
    */
    return state.temperatures as JSON
}

String getCurrentTemp() {
	return state.currentTemperature
}

String getHighTemp() {
	return getAllTemperatures().max()
}

String getLowTemp() {
	return getAllTemperatures().min()
}

String getAverageTemp() {
	return ((getAllTemperatures().sum() / state.temperatures.size()) as float).round(1)
}

List getAllTemperatures() {
	return state.temperatures.collect{
        if(it.temperature instanceof List){
            //handle case where it was one day
            return it.temperature[0]
        } else {
            return it.temperature
        }
    }
}