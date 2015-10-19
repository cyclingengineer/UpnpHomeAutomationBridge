package com.cyclingengineer.upnphomeautomationbridge.eq3max.internals;

public interface SetpointUpdate {
	
	public void temperatureSetpointUpdate( double value );
	
	public String getDeviceSerialNumber();
	
	public void setDeviceSerialNumber(String devSerial);

}
