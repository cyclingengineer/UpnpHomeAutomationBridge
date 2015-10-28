package com.cyclingengineer.upnphomeautomationbridge.eq3max.upnpservices;

import com.cyclingengineer.upnphomeautomationbridge.upnpservices.TemperatureSensorServiceZoneTemperature;
import com.cyclingengineer.upnphomeautomationbridge.upnpservices.updateservices.ZoneTemperatureUpdate;

public class Eq3TemperatureSensorServiceZoneTemperature 
	extends	TemperatureSensorServiceZoneTemperature 
	implements ZoneTemperatureUpdate {

	private String devSerial = null;
	
	@Override
	protected int temperatureValueRequest() {
		// TODO trigger a request to the cube?
		return this.currentTemperature;
	}

	@Override
	public void zoneTemperatureSensorUpdate(double value) {
		int oldTemperature = this.currentTemperature;
		this.currentTemperature = (int) (value*100);
		getPropertyChangeSupport().firePropertyChange("CurrentTemperature", oldTemperature, this.currentTemperature);
	}
	
	public String getDeviceSerialNumber(){
		return devSerial;
	}
	
	public void setDeviceSerialNumber(String devSerial){
		this.devSerial = devSerial;
	}
	

}
