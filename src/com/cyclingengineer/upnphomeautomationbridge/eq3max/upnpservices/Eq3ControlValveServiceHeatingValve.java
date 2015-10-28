package com.cyclingengineer.upnphomeautomationbridge.eq3max.upnpservices;

import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.model.types.UnsignedIntegerOneByte;

import com.cyclingengineer.upnphomeautomationbridge.upnpservices.ControlValveService;
import com.cyclingengineer.upnphomeautomationbridge.upnpservices.updateservices.ControlValveUpdate;

@UpnpService(serviceId = @UpnpServiceId("HeatingValve"), 
serviceType = @UpnpServiceType(value = "ControlValve", 
version = 1))

public class Eq3ControlValveServiceHeatingValve 
	extends ControlValveService
	implements ControlValveUpdate {

	private String devSerial = null;
	
	@Override
	protected UnsignedIntegerOneByte currentPositionRequest() {		
		return this.positionStatus;
	}

	@Override
	protected UnsignedIntegerOneByte currentPositionTargetRequest() {
		return this.positionTarget;
	}

	@Override
	public void controlValveCurrentPositionUpdate(UnsignedIntegerOneByte value) {
		UnsignedIntegerOneByte oldPos = this.positionStatus;
		this.positionStatus = value;
		getPropertyChangeSupport().firePropertyChange("PositionStatus", oldPos, this.positionStatus);		
	}

	@Override
	public String getDeviceSerialNumber() {
		return this.devSerial;
	}

	@Override
	public void setDeviceSerialNumber(String devSerial) {
		this.devSerial = devSerial;
	}

}
