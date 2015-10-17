package com.cyclingengineer.upnphomeautomationbridge;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;

import com.cyclingengineer.upnphomeautomationbridge.eq3max.Eq3RoomHvacZoneThermostatDevice;
import com.cyclingengineer.upnphomeautomationbridge.examples.AcmeExampleBinaryLightDevice;
import com.cyclingengineer.upnphomeautomationbridge.examples.ExampleDeviceRunner;

public class UpnpHomeAutomationBridge implements Runnable{

    public static void main(String[] args) throws Exception {
    	Logger log = LogManager.getLogManager().getLogger("");
    	for (Handler h : log.getHandlers()) {
    	    h.setLevel(Level.FINEST);
    	}
    	// Start a user thread that runs the UPnP stack
        Thread serverThread = new Thread(new UpnpHomeAutomationBridge());
        serverThread.setDaemon(false);
        serverThread.start();
    }

    public void run() {
        try {
        	final UpnpService upnpService = new UpnpServiceImpl();
            
            AcmeExampleBinaryLightDevice basicSwitch = new AcmeExampleBinaryLightDevice();
            //Eq3RoomHvacZoneThermostatDevice thermostat = new Eq3RoomHvacZoneThermostatDevice("Room Thermostat 1", "RT1 Friendly Name", "Manuf", "Model", "A lovely room thermostat", "v1");
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    upnpService.shutdown();
                }
            });

            // Add the bound local device to the registry
            upnpService.getRegistry().addDevice(
            		basicSwitch.createDevice()
            );
            // add thermostat
            //upnpService.getRegistry().addDevice(
            //		thermostat.createDevice()
            //);

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }


}
