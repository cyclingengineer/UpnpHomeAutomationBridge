package com.cyclingengineer.upnphomeautomationbridge.upnpservices;

import java.util.logging.Logger;

import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;

@UpnpService(serviceId = @UpnpServiceId("ZoneUserMode"), 
serviceType = @UpnpServiceType(value = "HVAC_UserOperatingMode", 
version = 1))

public abstract class HvacUserOperatingModeService {
	
	protected final Logger log = Logger.getLogger(this.getClass().getName());
	
	@UpnpStateVariable(defaultValue = "", sendEvents = true )
	protected String name = "";
	
	@UpnpStateVariable(defaultValue = "AutoChangeOver", sendEvents = true )
	protected String modeTarget = "";
	
	@UpnpStateVariable(defaultValue = "", sendEvents = true )
	protected String modeStatus = "";
	
	@UpnpAction
	public void setModeTarget(
		@UpnpInputArgument(name = "NewModeTarget") String setModeTargetValue) {
		modeTarget = setModeTargetValue;
		modeTargetUpdate(setModeTargetValue);		
	}	
	
	@UpnpAction(out = @UpnpOutputArgument(name = "CurrentModeTarget"))
	public String getModeTarget() {
		return modeTarget;
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "CurrentModeStatus"))
	public String getModeStatus() {
		modeStatus = modeStatusRequest();
		return modeStatus;
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "CurrentName"))
	public String getName() {
		return name;
	}
	
	@UpnpAction
	public void setName(
		@UpnpInputArgument(name = "NewName") String setNameValue) {
		name = setNameValue;
		nameUpdate( setNameValue );
	}
	
	
	/**
	 * Name property update event. Called on SetName request
	 * @param setNameValue Name value that is requesting to be set
	 */
	protected abstract void nameUpdate( String setNameValue );
	
	/**
	 * Mode target property update event. Called on SetModeTarget request
	 * @param setModeTargetValue Name value that is requesting to be set
	 */
	protected abstract void modeTargetUpdate( String setModeTargetValue );
	
	/**
	 * Mode status request event. Called on GetModeStatus request
	 * @return mode status that we are currently in
	 */
	protected abstract String modeStatusRequest( );

}
