/**
 *  Web Service Phrase Executor
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
    name: "Web Service Phrase Executor",
    namespace: "mrnohr",
    author: "Matt Nohr",
    description: "Execute a phrase with REST endpoints",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)

mappings {
  path("/phrases") {
    action: [
      GET: "listPhrases"
    ]
  }
  path("/phrases/:phrase") {
    action: [
      PUT: "executePhrase"
    ]
  }
}

preferences {
	section("Install to make the endpoints active") {
		paragraph "This exposes endpoints to call to execute phrases."
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
}

def updated() {
	log.debug "Updated with settings: ${settings}"
}

// Endpoints
def listPhrases() {
	log.debug "Getting a list of phrases"
	return location.helloHome.getPhrases().label
}

def executePhrase() {
	def phraseToExecute = params.phrase
    log.debug "Executing phrase: $phraseToExecute"
    location.helloHome.execute(phraseName)
}