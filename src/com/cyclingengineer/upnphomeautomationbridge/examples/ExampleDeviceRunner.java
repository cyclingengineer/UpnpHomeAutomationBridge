package com.cyclingengineer.upnphomeautomationbridge.examples;

import org.fourthline.cling.UpnpService;

public class ExampleDeviceRunner implements Runnable {
	
	private UpnpService upnpService;
	
	public ExampleDeviceRunner(UpnpService upnpService){
		this.upnpService = upnpService;
	}
	
    public void run() {
        try {
            
            AcmeExampleBinaryLightDevice basicSwitch = new AcmeExampleBinaryLightDevice();            

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
