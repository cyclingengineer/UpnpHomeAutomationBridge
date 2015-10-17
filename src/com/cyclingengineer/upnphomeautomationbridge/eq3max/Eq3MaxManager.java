package com.cyclingengineer.upnphomeautomationbridge.eq3max;

import org.fourthline.cling.UpnpService;

/**
 * This is the top level class for the EQ-3 MAX! UPNP bridge
 * @author Paul Hampson (cyclingengineer)
 */
public class Eq3MaxManager implements Runnable {
	
	private UpnpService upnpService;
	
	public Eq3MaxManager(UpnpService upnpService)
	{
		this.upnpService = upnpService;
	}
	
	public void run() {
		try {
			Eq3RoomHvacZoneThermostatDevice roomTherm = new Eq3RoomHvacZoneThermostatDevice("Room Thermostat 1", "RT1 Friendly Name", "Manuf", "Model", "A lovely room thermostat", "v1");

            // Add the bound local device to the registry
            upnpService.getRegistry().addDevice(
            		roomTherm.createDevice()
            );

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
	}

}
