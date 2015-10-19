package com.cyclingengineer.upnphomeautomationbridge.eq3max.upnpservices;

import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import com.cyclingengineer.upnphomeautomationbridge.eq3max.internals.CubeConnectionManager;
import com.cyclingengineer.upnphomeautomationbridge.eq3max.internals.SetpointUpdate;
import com.cyclingengineer.upnphomeautomationbridge.upnpservices.TemperatureSetpointService;

@UpnpService(serviceId = @UpnpServiceId("HeatingSetpoint"), 
serviceType = @UpnpServiceType(value = "TemperatureSetpoint", 
version = 1))

public class Eq3TemperatureSetpointServiceHeatingSetpoint extends
		TemperatureSetpointService implements
		SetpointUpdate {

	private String devSerial = null;
	private CubeConnectionManager cubeConnection = null;
	
	@Override
	protected int setpointValueRequest() { 
		return this.currentSetpoint;
	}
	
	@Override
	protected void setpointUpdate(int newSetPoint) {
		cubeConnection.sendSetpointCommand(devSerial, newSetPoint);
	}	

	@Override
	public void temperatureSetpointUpdate(double value) {
		int oldSetpoint = this.currentSetpoint;
		this.currentSetpoint = (int) (value*100);
		getPropertyChangeSupport().firePropertyChange("CurrentSetpoint", oldSetpoint, this.currentSetpoint);
		
	}

	@Override
	public String getDeviceSerialNumber() {
		return this.devSerial;
	}

	@Override
	public void setDeviceSerialNumber(String devSerial) {
		this.devSerial = devSerial;
	}
	
	public void setCubeConnectionManager(CubeConnectionManager c) {
		cubeConnection = c;
	}

}
