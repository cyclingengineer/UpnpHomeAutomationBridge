package com.cyclingengineer.upnphomeautomationbridge.upnpservices.updateservices;

public interface SetpointUpdate {
	
	public void temperatureSetpointUpdate( double value );
	
	public String getDeviceSerialNumber();
	
	public void setDeviceSerialNumber(String devSerial);

}
