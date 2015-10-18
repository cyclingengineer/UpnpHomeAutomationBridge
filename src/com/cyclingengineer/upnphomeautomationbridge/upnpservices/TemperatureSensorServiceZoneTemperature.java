package com.cyclingengineer.upnphomeautomationbridge.upnpservices;

import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;

@UpnpService(serviceId = @UpnpServiceId("ZoneTemperature"), 
serviceType = @UpnpServiceType(value = "TemperatureSensor", 
version = 1))

public abstract class TemperatureSensorServiceZoneTemperature extends
		TemperatureSensorService {

}
