package com.cyclingengineer.upnphomeautomationbridge.eq3max;

import com.cyclingengineer.upnphomeautomationbridge.upnpdevices.HvacZoneThermostatDevice;
import com.cyclingengineer.upnphomeautomationbridge.upnpservices.HvacUserOperatingModeService;

public class Eq3RoomHvacZoneThermostatDevice extends HvacZoneThermostatDevice {

	public Eq3RoomHvacZoneThermostatDevice(String deviceIdentity,
			String friendlyName, String manufacturerName, String modelName,
			String modelDescription, String modelNumber) {
		super(deviceIdentity, friendlyName, manufacturerName, modelName,
				modelDescription, modelNumber, Eq3MaxHvacUserOperatingModeServiceZoneUserMode.class);
		// TODO add other relevant services to the list?
	}

}
