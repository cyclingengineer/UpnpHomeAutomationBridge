package com.cyclingengineer.upnphomeautomationbridge.eq3max;

import org.fourthline.cling.UpnpService;
import com.cyclingengineer.upnphomeautomationbridge.eq3max.upnpdevices.Eq3RoomHvacZoneThermostatDevice;

/**
 * This is the top level class for the EQ-3 MAX! UPNP bridge
 * @author Paul Hampson (cyclingengineer)
 */
public class Eq3MaxManager implements Runnable {
	
	private UpnpService upnpService;
	
	public Eq3MaxManager(UpnpService upnpService) {
		this.upnpService = upnpService;
		
		try {			
            
            Eq3RoomHvacZoneThermostatDevice thermostat = new Eq3RoomHvacZoneThermostatDevice("Room Thermostat 1", "RT1 Friendly Name", "Manuf", "Model", "A lovely room thermostat", "v1");

            // add thermostat
            upnpService.getRegistry().addDevice(
            		thermostat.createDevice()
            );

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
	}
	
	public void run() {
		// TODO run eq3 stuff here
	}

}
