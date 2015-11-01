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
        
        command "heatingSetpointUp"
		command "heatingSetpointDown"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"thermostat", type: "thermostat", width:6, height: 4, canChangeIcon: true) {
        	tileAttribute ("device.temperature", key: "PRIMARY_CONTROL") {
            	attributeState "temperature", label: '${currentValue}°C', icon: "st.home.Home1"
            }
            tileAttribute ("device.heatingSetpoint", key: "VALUE_CONTROL") {
            	attributeState "heatingSetpoint", action:"heatingSetpoint.setHeatingSetpoint"
            }
     	}        
        
        valueTile("setpoint", "device.heatingSetpoint", width: 2, height: 2) {
        	state("heatingSetpoint", label:'Set Point:\n${currentValue}°C',
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
        	state("temperature", label:'Room Temp:\n${currentValue}°C',
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
        
        valueTile("valve", "device.valvePosition", width: 2, height: 2) {
        	state("temperature", label:'Valve Position:\n${currentValue} %',
            	/*backgroundColors:[
                	[value: 0, color: "#153591"],
                	[value: 7, color: "#1e9cbb"],
                	[value: 15, color: "#90d2a7"],
                	[value: 23, color: "#44b621"],
                	[value: 29, color: "#f1d801"],
                	[value: 35, color: "#d04e00"],
                	[value: 36, color: "#bc2323"]
            	]*/
        	)
    	}
        
        standardTile("heatingSetpointUp", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#bc2323"
		}

		standardTile("heatingSetpointDown", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#bc2323"
		}
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}
        
        main "temperature"
        
        details(["heatingSetpointUp", "setpoint", "heatingSetpointDown", "valve", "temperature", "refresh"])
	}
}

Map getRequestMap() {
	state.requestMap = state.requestMap ?: [:]
}

private def setRequestMap( Map newMap ) {
	state.requestMap = newMap
}

// parse events into attributes
def parse(String description) {
	log.debug "Heating Controller Parsing '${description}'"
    
	// TODO: handle 'temperature' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'valvePosition' attribute

}

// handle commands
def setHeatingSetpoint(temp) {
	log.debug "Executing 'setHeatingSetpoint(${temp})'"
	
    // update our local value, then when we poll or refresh then we send it
    sendEvent(name: "heatingSetpoint", value: temp)
    state.setPointUpdated = true
}

private def sendHeatingSetpointUpdate() {
	log.debug "Executing 'sendHeatingSetpointUpdate(${temp})'"
	def temp = state.newHeatingSetpoint
	def setpointEventUrl = getDeviceDataByName("HeatingSetpointServiceEventUrl")
    def setpointActionPath = setpointEventUrl.replaceAll("event", "action")
    
    // default to sending the current one if it's a dodgy type
    double currentSetPoint = device.currentValue("heatingSetpoint")
    currentSetPoint = currentSetPoint*100.0
    def intTemp = currentSetPoint.intValue()
    if (temp instanceof Double) {
    	intTemp = (temp*100).intValue()
    } else if (temp instanceof Integer) {
    	intTemp = temp*100
    }
    log.trace "intTemp = ${intTemp}"
	def requestId = parent.doUpnpAction("SetCurrentSetpoint", "TemperatureSetpoint", 
    					setpointActionPath, [NewCurrentSetpoint: intTemp], 
    					this)
    
    Map reqMap = getRequestMap()
    reqMap << ["${requestId}":"setHeatingSetpoint"]
    log.trace "appended reqMap = ${getRequestMap()}"    
}

def pollHeatingSetpoint() {
	log.debug "Polling Heating Setpoint"
	def setpointEventUrl = getDeviceDataByName("HeatingSetpointServiceEventUrl")
    def setpointActionPath = setpointEventUrl.replaceAll("event", "action")
	def requestId = parent.doUpnpAction("GetCurrentSetpoint", "TemperatureSetpoint", 
    					setpointActionPath, [:], 
    					this)
    
    Map reqMap = getRequestMap()
    reqMap << ["${requestId}":"HeatingSetpoint"]
    log.trace "appended reqMap = ${getRequestMap()}"
}

def pollTemperature() {
	log.debug "Polling Temperature"
	def temperatureEventUrl = getDeviceDataByName("ZoneTemperatureServiceEventUrl")
    def temperatureActionPath = temperatureEventUrl.replaceAll("event", "action")
	def requestId = parent.doUpnpAction("GetCurrentTemperature", "TemperatureSensor", 
    					temperatureActionPath, [:], 
    					this)
    
    Map reqMap = getRequestMap()
    reqMap << ["${requestId}":"Temperature"]
    log.trace "appended reqMap = ${getRequestMap()}"
}

def pollValve() {
	log.debug "Polling Valve"
	def valveEventUrl = getDeviceDataByName("HeatingValveServiceEventUrl")
    def valveActionPath = valveEventUrl.replaceAll("event", "action")
	def requestId = parent.doUpnpAction("GetPosition", "ControlValve", 
    					valveActionPath, [:], 
    					this)
    
    Map reqMap = getRequestMap()
    reqMap << ["${requestId}":"ValvePos"]
    log.trace "appended reqMap = ${getRequestMap()}"
}

private def doUpdateRoutine() {
	// do manual update sequence
	if (state.setPointUpdated) {
    	sendHeatingSetpointUpdate() // this will trigger a read pollHeatingSetpoint so no need to do it separately        
        state.setPointUpdated = false // done update, clear flag
    } else {
    	pollHeatingSetpoint()
    }
    pollTemperature()
    pollValve()
}

def poll() {
	doUpdateRoutine()
}

def refresh() {
	log.debug "EQ3 Upnp Bridge Zone Thermostat refresh requested"
    subscribeToServices()
    doUpdateRoutine()
}

def subscribeToServices() {
	//def zoneTempEventUrl = getDeviceDataByName("ZoneTemperatureServiceEventUrl")
	//log.trace "result ="+subscribeUpnpAction(zoneTempEventUrl, "/zoneTemp")
    //def setpointEventUrl = getDeviceDataByName("HeatingSetpointServiceEventUrl")
    //subscribeUpnpAction(setpointEventUrl)
    //def valveEventUrl = getDeviceDataByName("HeatingValveServiceEventUrl")
    //subscribeUpnpAction(valveEventUrl)    
}

def heatingSetpointUp() {
	double newSetpoint = device.currentValue("heatingSetpoint") + 0.5
	log.debug "Increment heat set point to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def heatingSetpointDown() {
	double newSetpoint = device.currentValue("heatingSetpoint") - 0.5
	log.debug "Decrement heat set point to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

private def handleRequestResponse(type, body)
{
	// handle the request
    def results = []
    switch(type) {
    	case "HeatingSetpoint":
        	log.trace "handling HeatingSetpoint request response"
            if (body.contains("xml"))
            {
            	// got xml - so parse it
                def parsedXml = new XmlSlurper().parseText(body)
                def setpointString = parsedXml.Body.GetCurrentSetpointResponse.CurrentSetpoint
                log.trace "setpointString = "+setpointString
                def setpoint = new Double(setpointString.text()) / 100.0
                sendEvent(name: "heatingSetpoint", value: setpoint)
            } else {
            	log.error "Expected XML response for HeatingSetpoint"
            }
        	break
        case "Temperature":
        	log.trace "handling Temperature request response"
            if (body.contains("xml"))
            {
            	// got xml - so parse it
                def parsedXml = new XmlSlurper().parseText(body)
                def tempString = parsedXml.Body.GetCurrentTemperatureResponse.CurrentTemperature
                log.trace "tempString = "+tempString
                def temp = new Double(tempString.text()) / 100.0
                sendEvent(name: "temperature", value: temp)
            } else {
            	log.error "Expected XML response for Temperature"
            }
        	break
        case "ValvePos":
        	log.trace "handling Valve Position request response"
            if (body.contains("xml"))
            {
            	// got xml - so parse it
                def parsedXml = new XmlSlurper().parseText(body)
                def valvePosString = parsedXml.Body.GetPositionResponse.CurrentPositionStatus
                log.trace "valvePosString = "+valvePosString
                def valvePos = new Integer(valvePosString.text())
                sendEvent(name: "valvePosition", value: valvePos)
            } else {
            	log.error "Expected XML response for Valve Position"
            }
        	break
        case "setHeatingSetpoint":
        	log.trace "handling Set Heating Setpoint request response"
            pollHeatingSetpoint()
            break
            
        default:
        	log.error "Got unexpected request type: ${type}"
            break
    }
    return results
}

def requestResponse( id, body ) {
	log.trace("request Reponse ${id}, ${body}")
    
    Map reqMap = getRequestMap()
    def requestType = reqMap["${id}"]    
    if (requestType) {
    	// request response was for us - handle it        
        reqMap.remove(id)
        setRequestMap(reqMap)
        log.trace "cleared reqMap = ${getRequestMap()}"
        log.trace "requestType = ${requestType}"
        handleRequestResponse(requestType, body)
    }    
}