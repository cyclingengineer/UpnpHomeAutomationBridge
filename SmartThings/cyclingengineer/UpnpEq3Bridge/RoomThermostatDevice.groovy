/**
 *  Upnp Bridge Eq3 Room Heating Controller
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
	definition (name: "Upnp Bridge Eq3 Room Heating Controller", namespace: "cyclingengineer", author: "Paul Hampson") {
		capability "Temperature Measurement"
		capability "Thermostat Heating Setpoint"
        capability "Polling"
		capability "Refresh"

		attribute "valvePosition", "number"        
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"thermostat", type: "thermostat", width:6, height: 4, canChangeIcon: true) {
        	tileAttribute ("device.heatingSetpoint", key: "PRIMARY_CONTROL") {
            	attributeState "heatingSetpoint", label: '${name}'
            }
            tileAttribute ("device.heatingSetpoint", key: "VALUE_CONTROL") {
            	attributeState "heatingSetpoint", action:"thermostatHeatingSetpoint  heatingSetpoint.setHeatingSetpoint"
            }
     	}        
        
        valueTile("setpoint", "device.heatingSetpoint", width: 2, height: 2) {
        	state("heatingSetpoint", label:'${currentValue}°C', action:"thermostatHeatingSetpoint  heatingSetpoint.setHeatingSetpoint",
            	backgroundColors:[
                	[value: 0, color: "#153591"],
                	[value: 7, color: "#1e9cbb"],
                	[value: 15, color: "#90d2a7"],
                	[value: 23, color: "#44b621"],
                	[value: 29, color: "#f1d801"],
                	[value: 35, color: "#d04e00"],
                	[value: 36, color: "#bc2323"]
            	]
        	)
    	}
        
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
        	state("temperature", label:'${currentValue}°C',
            	backgroundColors:[
                	[value: 0, color: "#153591"],
                	[value: 7, color: "#1e9cbb"],
                	[value: 15, color: "#90d2a7"],
                	[value: 23, color: "#44b621"],
                	[value: 29, color: "#f1d801"],
                	[value: 35, color: "#d04e00"],
                	[value: 36, color: "#bc2323"]
            	]
        	)
    	}
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}
        
        main "thermostat"
        
        details(["thermostat", "setpoint", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    
    def msg = parseLanMessage(description)

    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
    
	// TODO: handle 'temperature' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'valvePosition' attribute

}

// handle commands
def setHeatingSetpoint() {
	log.debug "Executing 'setHeatingSetpoint'"
	// TODO: handle 'setHeatingSetpoint' command
}

def pollHeatingSetpoint() {
	def setpointEventUrl = getDeviceDataByName("HeatingSetpointServiceEventUrl")
    def setpointActionPath = setpointEventUrl.replaceAll("event", "action")
	def result = doUpnpAction("GetCurrentSetpoint", "TemperatureSetpoint", setpointActionPath, [:])
    log.debug "result: ${result}"
}

def poll() {
	
}

def refresh() {
	log.debug "EQ3 Upnp Bridge Zone Thermostat refresh requested"
    //subscribeToServices()
    pollHeatingSetpoint()
}

def subscribeToServices() {
	//def zoneTempEventUrl = getDeviceDataByName("ZoneTemperatureServiceEventUrl")
	//log.trace "result ="+subscribeUpnpAction(zoneTempEventUrl, "/zoneTemp")
    def setpointEventUrl = getDeviceDataByName("HeatingSetpointServiceEventUrl")
    subscribeUpnpAction(setpointEventUrl)
    //def valveEventUrl = getDeviceDataByName("HeatingValveServiceEventUrl")
    //subscribeUpnpAction(valveEventUrl)    
}

// subscribe to a UPnP event
private subscribeUpnpAction(path, callbackPath="") {
    log.trace "subscribe($path, $callbackPath)"
    def address = getCallBackAddress()
    log.trace "callback address = "+address
    def ip = getHostAddress()

    def result = new physicalgraph.device.HubAction(
        method: "SUBSCRIBE",
        path: path,
        headers: [
            HOST: ip,
            CALLBACK: "<http://${address}/notify$callbackPath>",
            NT: "upnp:event",
            TIMEOUT: "Second-28800"
        ]
    )

    log.trace "SUBSCRIBE $path"

    return result
}

// execute a Upnp action
def doUpnpAction(action, service, path, Map body) {
	log.debug "doUpnpAction(${action}, ${service}, ${path}, ${body}"
    def result = new physicalgraph.device.HubSoapAction(
        path:    path,
        urn:     "urn:schemas-upnp-org:service:$service:1",
        action:  action,
        body:    body,
        headers: [Host:getHostAddress(), CONNECTION: "close"]
    )
    return result
}

// gets the address of the hub
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

// gets the address of the device
private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def fullDniParts = device.deviceNetworkId.split("/")
        def hostParts = fullDniParts[0].split(":")
        if (hostParts.length == 2) {
            ip = hostParts[0]
            port = hostParts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    log.debug "Using IP: ${convertHexToIP(ip)} and port: ${convertHexToInt(port)} for device: ${device.id}"
    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}