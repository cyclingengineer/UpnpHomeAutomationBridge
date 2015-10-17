package com.cyclingengineer.upnphomeautomationbridge;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;

import com.cyclingengineer.upnphomeautomationbridge.exampleswitch.ExampleBasicSwitch;

public class UpnpHomeAutomationBridge implements Runnable {

    public static void main(String[] args) throws Exception {
        // Start a user thread that runs the UPnP stack
        Thread serverThread = new Thread(new UpnpHomeAutomationBridge());
        serverThread.setDaemon(false);
        serverThread.start();
    }

    public void run() {
        try {

            final UpnpService upnpService = new UpnpServiceImpl();
            ExampleBasicSwitch basicSwitch = new ExampleBasicSwitch();

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
