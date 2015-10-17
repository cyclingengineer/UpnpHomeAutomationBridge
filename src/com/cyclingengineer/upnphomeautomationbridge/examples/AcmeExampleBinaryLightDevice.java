package com.cyclingengineer.upnphomeautomationbridge.examples;

import com.cyclingengineer.upnphomeautomationbridge.upnpdevices.BinaryLightDevice;

public class AcmeExampleBinaryLightDevice extends BinaryLightDevice {

	public AcmeExampleBinaryLightDevice(){
		super("Example BinaryLight", 
				"BinaryLight Friendly Name", 
				"ACME", "ACME light", 
				"The best light the world has seen", 
				"v1", AcmeExampleBinaryLightDevice.class);
	}
	
	
	/**
	 * Implement actuation of the switch
	 * 
	 * @param targetStatus
	 *            Status we are attempting to set
	 * @return Status that is achieved
	 */
	@Override
	protected boolean actuateSwitch(boolean targetStatus) {
		String resultString = targetStatus ? "On" : "Off";
		this.log.info("Actuating switch to " + resultString);
		return targetStatus;
	}

}
