package com.cyclingengineer.upnphomeautomationbridge.upnpservices.updateservices;

public interface ControlValveUpdate {
	
	/**
	 * Provide an update to the ControlValve UPNP service
	 * @param value Value to set the CurrentPosition to
	 */
	public void controlValveCurrentPositionUpdate( org.fourthline.cling.model.types.UnsignedIntegerOneByte value );
	
	/**
	 * Get associated device serial number
	 * @return associated device serial
	 */
	public String getDeviceSerialNumber();
	
	/**
	 * Set the associated device serial number
	 * @param devSerial device serial number to associate
	 */
	public void setDeviceSerialNumber(String devSerial);

}
