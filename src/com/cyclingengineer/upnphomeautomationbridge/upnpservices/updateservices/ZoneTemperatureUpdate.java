package com.cyclingengineer.upnphomeautomationbridge.upnpservices.updateservices;

public interface ZoneTemperatureUpdate {
		
	public void zoneTemperatureSensorUpdate( double value );
	
	public String getDeviceSerialNumber();

	public void setDeviceSerialNumber(String devSerial);
}
