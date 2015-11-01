package com.cyclingengineer.upnphomeautomationbridge;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;

import com.cyclingengineer.upnphomeautomationbridge.eq3max.Eq3MaxManager;
import com.cyclingengineer.upnphomeautomationbridge.examples.AcmeExampleBinaryLightDevice;

public class UpnpHomeAutomationBridge implements Runnable{
	private static UpnpService upnpService;
	
    public static void main(String[] args) throws Exception {
    	
    	// UPnP discovery is asynchronous, we need a callback
        RegistryListener listener = new RegistryListener() {

            public void remoteDeviceDiscoveryStarted(Registry registry,
                                                     RemoteDevice device) {
                System.out.println(
                        "Discovery started: " + device.getDisplayString()
                );
            }

            public void remoteDeviceDiscoveryFailed(Registry registry,
                                                    RemoteDevice device,
                                                    Exception ex) {
                System.out.println(
                        "Discovery failed: " + device.getDisplayString() + " => " + ex
                );
            }

            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
                System.out.println(
                        "Remote device available: " + device.getDisplayString()
                );
            }

            public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
                System.out.println(
                        "Remote device updated: " + device.getDisplayString()
                );
            }

            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                System.out.println(
                        "Remote device removed: " + device.getDisplayString()
                );
            }

            public void localDeviceAdded(Registry registry, LocalDevice device) {
                System.out.println(
                        "Local device added: " + device.getDisplayString()
                );
            }

            public void localDeviceRemoved(Registry registry, LocalDevice device) {
                System.out.println(
                        "Local device removed: " + device.getDisplayString()
                );
            }

            public void beforeShutdown(Registry registry) {
                System.out.println(
                        "Before shutdown, the registry has devices: "
                        + registry.getDevices().size()
                );
            }

            public void afterShutdown() {
                System.out.println("Shutdown of registry complete!");

            }
        };
        
    	// Start a user thread that runs the UPnP stack
    	upnpService = new UpnpServiceImpl(new HomeAutomationBridgeUpnpServiceConfiguration(), listener);
        //Thread serverThread = new Thread(new UpnpHomeAutomationBridge());
        //serverThread.setDaemon(false);
        //serverThread.start();    	
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
