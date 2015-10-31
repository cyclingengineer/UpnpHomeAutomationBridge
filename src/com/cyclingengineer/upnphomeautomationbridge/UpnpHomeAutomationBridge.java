package com.cyclingengineer.upnphomeautomationbridge;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;

import com.cyclingengineer.upnphomeautomationbridge.eq3max.Eq3MaxManager;
import com.cyclingengineer.upnphomeautomationbridge.examples.AcmeExampleBinaryLightDevice;

public class UpnpHomeAutomationBridge implements Runnable{
	private static UpnpService upnpService;
	
    public static void main(String[] args) throws Exception {
    	// Start a user thread that runs the UPnP stack
    	upnpService = new UpnpServiceImpl();
        //Thread serverThread = new Thread(new UpnpHomeAutomationBridge());
        //serverThread.setDaemon(false);
        //serverThread.start();
    	Logger.getGlobal().setLevel(Level.ALL);
        Thread eq3Thread = new Thread(new Eq3MaxManager(upnpService));
        eq3Thread.setDaemon(false);
        eq3Thread.start();
    }

    public void run() {
        try {
        	            
            AcmeExampleBinaryLightDevice basicSwitch = new AcmeExampleBinaryLightDevice();            
            
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

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }


}
