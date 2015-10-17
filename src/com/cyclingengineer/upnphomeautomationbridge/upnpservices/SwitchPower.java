package com.cyclingengineer.upnphomeautomationbridge.upnpservices;

import org.fourthline.cling.binding.annotations.*;
import java.util.logging.Logger;

@UpnpService(serviceId = @UpnpServiceId("SwitchPower"), 
	serviceType = @UpnpServiceType(value = "SwitchPower", 
	version = 1))

public abstract class SwitchPower {

	protected final Logger log = Logger.getLogger(this.getClass().getName());
		
	@UpnpStateVariable(defaultValue = "0", sendEvents = false)
	private boolean target = false;

	@UpnpStateVariable(defaultValue = "0")
	private boolean status = false;

	@UpnpAction
	public void setTarget(
		@UpnpInputArgument(name = "NewTargetValue") boolean newTargetValue) {
		target = newTargetValue;
		status = actuateSwitch( newTargetValue );		
		log.fine("Switch is: " + status);		
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "RetTargetValue"))
	public boolean getTarget() {
		return target;
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "ResultStatus"))
	public boolean getStatus() {
		// If you want to pass extra UPnP information on error:
		// throw new ActionException(ErrorCode.ACTION_NOT_AUTHORIZED);
		return status;
	}
	
	/**
	 * Complete the action required to update the physical device.
	 * @param targetValue value we are targetting
	 * @return result of the actuation
	 */
	protected abstract boolean actuateSwitch( boolean targetValue );
}
