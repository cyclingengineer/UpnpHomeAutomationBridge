package com.cyclingengineer.upnphomeautomationbridge.upnpservices;

import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;

@UpnpService(serviceId = @UpnpServiceId("SystemUserMode"), 
serviceType = @UpnpServiceType(value = "HVAC_UserOperatingMode", 
version = 1))

public abstract class  HvacUserOperatingModeServiceSystemUserMode extends
		HvacUserOperatingModeService {
	
	public HvacUserOperatingModeServiceSystemUserMode() {
		super();
	}
	

}
