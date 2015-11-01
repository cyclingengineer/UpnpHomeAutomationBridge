/**
 *  UPnP Home Automation Bridge
 *
 *  Copyright 2015 Paul Hampson
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
metadata {
	definition (name: "UPnP Home Automation Bridge", namespace: "cyclingengineer", author: "Paul Hampson") {
    	attribute "udn", "string"
        attribute "networkAddress", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("icon", "icon", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "UPnP Bridge", action: "", icon: "st.Electronics.electronics6", backgroundColor: "#FFFFFF"
		}
		valueTile("udn", "device.udn", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'UDN: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'${currentValue}', height: 1, width: 2, inactiveLabel: false
		}

		main (["icon"])
		details(["networkAddress","udn"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	def results = []
    def result = parent.parse(this, description)
    
    //log.trace "parent.parse ${result}"
    
    // TODO: handle 'udn' attribute
	// TODO: handle 'networkAddress' attribute

}

