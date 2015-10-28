package com.cyclingengineer.upnphomeautomationbridge.eq3max;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.LocalService;
import org.openhab.binding.maxcube.internal.MaxCubeDiscover;
import org.openhab.binding.maxcube.internal.Utils;
import org.openhab.binding.maxcube.internal.exceptions.IncompleteMessageException;
import org.openhab.binding.maxcube.internal.exceptions.IncorrectMultilineIndexException;
import org.openhab.binding.maxcube.internal.exceptions.MessageIsWaitingException;
import org.openhab.binding.maxcube.internal.exceptions.NoMessageAvailableException;
import org.openhab.binding.maxcube.internal.exceptions.UnprocessableMessageException;
import org.openhab.binding.maxcube.internal.exceptions.UnsupportedMessageTypeException;
import org.openhab.binding.maxcube.internal.message.C_Message;
import org.openhab.binding.maxcube.internal.message.Configuration;
import org.openhab.binding.maxcube.internal.message.Device;
import org.openhab.binding.maxcube.internal.message.DeviceInformation;
import org.openhab.binding.maxcube.internal.message.HeatingThermostat;
import org.openhab.binding.maxcube.internal.message.L_Message;
import org.openhab.binding.maxcube.internal.message.M_Message;
import org.openhab.binding.maxcube.internal.message.Message;
import org.openhab.binding.maxcube.internal.message.RoomInformation;

import com.cyclingengineer.upnphomeautomationbridge.eq3max.internals.CubeConnectionManager;
import com.cyclingengineer.upnphomeautomationbridge.eq3max.internals.Room;
import com.cyclingengineer.upnphomeautomationbridge.eq3max.internals.SetpointUpdate;
import com.cyclingengineer.upnphomeautomationbridge.eq3max.internals.ZoneTemperatureUpdate;
import com.cyclingengineer.upnphomeautomationbridge.eq3max.upnpservices.Eq3MaxHvacUserOperatingModeServiceSystemUserMode;
import com.cyclingengineer.upnphomeautomationbridge.eq3max.upnpservices.Eq3MaxHvacUserOperatingModeServiceZoneUserMode;
import com.cyclingengineer.upnphomeautomationbridge.eq3max.upnpservices.Eq3TemperatureSensorServiceZoneTemperature;
import com.cyclingengineer.upnphomeautomationbridge.eq3max.upnpservices.Eq3TemperatureSetpointServiceHeatingSetpoint;
import com.cyclingengineer.upnphomeautomationbridge.upnpdevices.HvacSystemDevice;
import com.cyclingengineer.upnphomeautomationbridge.upnpdevices.HvacZoneThermostatDevice;
import com.cyclingengineer.upnphomeautomationbridge.upnpdevices.UpnpDevice;

/**
 * This is the top level class for the EQ-3 MAX! UPNP bridge
 * @author Paul Hampson (cyclingengineer)
 */
public class Eq3MaxManager implements Runnable {
	
	private final static int REFRESH_INTERVAL = 10000;
	private final static String SERVICE_VERSION = "v1";
	private UpnpService upnpService;
	
	String cubeIp = "";
	int cubePort = 62910;
	CubeConnectionManager cubeConManager = null;
	
	private ArrayList<Configuration> configurations = new ArrayList<Configuration>();
	private ArrayList<Device> devices = new ArrayList<Device>();
	private ArrayList<Room> rooms = new ArrayList<Room>();
	
	private ArrayList<UpnpDevice> upnpDeviceList = new ArrayList<UpnpDevice>();
	
	private ArrayList<ZoneTemperatureUpdate> tempUpdateRegister = new ArrayList<ZoneTemperatureUpdate>();
	private ArrayList<SetpointUpdate> setpointUpdateRegister = new ArrayList<SetpointUpdate>();
	
	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	
	public Eq3MaxManager(UpnpService upnpService) {
		this.upnpService = upnpService;	
		
	}
	
	public void run() {
		// MAX! cube discovery
		if (cubeIp.isEmpty()) {
			cubeIp = MaxCubeDiscover.discoverIp();
		}
		if (cubeIp == null)
		{
			logger.severe("Exiting as unable to find MAX! cube IP");
			return;
		} 
		
		cubeConManager = new CubeConnectionManager(devices, cubeIp, cubePort);
		
		logger.info("Found cube at "+cubeIp);		
		try {
			cubeConManager.openCubeConnection();
		} catch (UnknownHostException e) {
			logger.severe("Unable to connect to cube (UnknownHostException): "+e.getMessage());
			return;
		} catch (IOException e) { 
			logger.severe("Unable to connect to cube (IOException): "+e.getMessage());
			return;
		}
		
		logger.info("Successfully connected to cube at "+cubeIp);	
		
		// Find all available devices
		boolean processMessages = true;
		while (processMessages) {
			String rawLine = null;
			try {
				rawLine = cubeConManager.getCubeReader().readLine();
			} catch (Exception e) {
				logger.severe("Error reading data from cube: "+e.getMessage());
				processMessages = false;
				continue;
			}
			
			logger.info("Read: "+rawLine);
			
			if (rawLine == null) {
				// run out of data
				processMessages = false;
				continue;
			}
			
			Message message = null;
			try {
				cubeConManager.getMessageProcessor().addReceivedLine(rawLine);
				if (cubeConManager.getMessageProcessor().isMessageAvailable()) {
					message = cubeConManager.getMessageProcessor().pull();
					logger.info("New MAX! message available");
				} else {
					continue;
				}
				
				if (message != null) {
					logger.getParent().setLevel(Level.FINEST);
					message.debug(logger);

					// process messages
					switch (message.getType())
					{
						case M: // device and room configuration
						{
							M_Message msg = (M_Message) message;
							// process rooms
							for (RoomInformation ri : msg.rooms) {
								Room existingRoom = null;
								for (Room r : rooms) {
									if (r.getRoomId() == ri.getPosition()) {
										existingRoom = r;
										break;
									}
								}
								// no existing room found - add one
								if (existingRoom == null) {
									logger.info("Adding room "+ri.getName());
									rooms.add(new Room(ri.getName(), ri.getPosition()));
								}									
							}
							
							// process devices
							for (DeviceInformation di : msg.devices) {
								
								// generate device to room mapping							
								Room parentRoom = null;
								for (Room r : rooms) {
									if (r.getRoomId() == di.getRoomId())
										parentRoom = r;
								}
								if (parentRoom == null) {
									// this would be odd... TODO handle orphaned device?
									logger.warning("Found orphaned device "+di.getName());
									continue;
								} else {
									logger.info("Adding "+di.getName()+" to "+parentRoom.getRoomName());
									parentRoom.addDevice(di.getSerialNumber());
								}
								
								// setup configurations
								Configuration c = null;
								for (Configuration conf : configurations) {
									if (conf.getSerialNumber().equalsIgnoreCase(di.getSerialNumber())) {
										c = conf;
										break;
									}
								}

								if (c != null) {
									configurations.remove(c);
								}

								c = Configuration.create(di);
								configurations.add(c);

								c.setRoomId(di.getRoomId());
							}							
							break;
						}
						
						case C: // device state information
						{
							Configuration c = null;
							for (Configuration conf : configurations) {
								if (conf.getSerialNumber().equalsIgnoreCase(((C_Message) message).getSerialNumber())) {
									c = conf;
									break;
								}
							}

							if (c == null) {
								configurations.add(Configuration.create(message));
							} else {
								c.setValues((C_Message) message);
							}
							break;
						}
						// TODO other message types...
						case L: 
						{
							((L_Message) message).updateDevices(devices, configurations);
							
							logger.info(""+devices.size()+" devices found." );
							
							// the L message is the last one, while the reader
							// would hang trying to read a new line and
							// eventually the
							// cube will fail to establish
							// new connections for some time
							processMessages = false;
							break;
						}
						
						default:
							// do nothing
							break;
					}
				}
			}  catch (IncorrectMultilineIndexException ex) {
				logger.info("Incorrect MAX!Cube multiline message detected. Stopping processing and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (NoMessageAvailableException ex) {
				logger.info("Could not process MAX!Cube message. Stopping processing and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (IncompleteMessageException ex) {
				logger.info("Error while parsing MAX!Cube multiline message. Stopping processing, and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (UnprocessableMessageException ex) {
				logger.info("Error while parsing MAX!Cube message. Stopping processing, and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (UnsupportedMessageTypeException ex) {
				logger.info("Unsupported MAX!Cube message detected. Ignoring and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (MessageIsWaitingException ex) {
				logger.info("There was and unhandled message waiting. Ignoring and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (Exception e) {
				logger.info("Failed to process message received by MAX! protocol.");
				logger.fine(Utils.getStackTrace(e));
				cubeConManager.getMessageProcessor().reset();
			}
		}
				
		cubeConManager.closeCubeConnection();
		
		// build Upnp device(s) based on data received from MAX! cube 
		// setup top level system device
		HvacSystemDevice topLevelHvacSystemDevice = new HvacSystemDevice("MAX! Cube at "+cubeIp, 
				"MAX! Cube at "+cubeIp, 
				"CyclingEngineer", 
				"Upnp EQ3 Bridge", 
				"EQ3 MAX! LAN cube bridge service", 
				SERVICE_VERSION, 
				Eq3MaxHvacUserOperatingModeServiceSystemUserMode.class);
		upnpDeviceList.add(topLevelHvacSystemDevice);
		
		// generate a HVAC_ZoneThermostat for each room
		for (Room r : rooms){
			HvacZoneThermostatDevice newZone = new HvacZoneThermostatDevice(
					Integer.toString(r.getRoomId()), 
					r.getRoomName(),
					"CyclingEngineer",
					"EQ3 MAX! Room",
					"Representation of a EQ3 cube room",
					SERVICE_VERSION,
					Eq3MaxHvacUserOperatingModeServiceZoneUserMode.class);			
			// add room services as required	
			boolean roomTempSenseIsWallTherm = false;
			Device roomTempSenseDev = null;			
			for (String devSerial : r.getRoomDeviceList()) {
				// find device with the serial number
				Device matchedDevice = null;
				for (Device d : devices) {					
					if (d.getSerialNumber().equals(devSerial))
					{
						matchedDevice = d;
						break;
					}
				}
				
				switch (matchedDevice.getType()) {
					case HeatingThermostat:
					case HeatingThermostatPlus:
						// temperature
						if (roomTempSenseIsWallTherm == false) {
							roomTempSenseDev = matchedDevice;
						}						
						// set point
						// valve
						// schedule?
						break;
						
					case WallMountedThermostat:												
						// temperature
						if (roomTempSenseIsWallTherm == false){
							roomTempSenseDev = matchedDevice;
							roomTempSenseIsWallTherm = true; // favour wall thermostat
						}
						// set point
						// schedule?
						break;
						
					default:
						// we don't care about other devices
						break;
				}
			}
			// register for room temperature updates
			if (roomTempSenseDev != null) {
				// temperature sensor service
				LocalService<?> tempSenseService = newZone.addServiceToDevice(Eq3TemperatureSensorServiceZoneTemperature.class);
				Eq3TemperatureSensorServiceZoneTemperature roomTempSenseServiceImp = (Eq3TemperatureSensorServiceZoneTemperature) tempSenseService.getManager().getImplementation();
				roomTempSenseServiceImp.setDeviceSerialNumber(roomTempSenseDev.getSerialNumber());
				roomTempSenseServiceImp.setApplication("Room");
				roomTempSenseServiceImp.setName(r.getRoomName() +" Sensor ("+roomTempSenseDev.getType()+")");
				tempUpdateRegister.add(roomTempSenseServiceImp);
				
				// temperature setpoint service
				LocalService<?> tempSetpointService = newZone.addServiceToDevice(Eq3TemperatureSetpointServiceHeatingSetpoint.class);
				Eq3TemperatureSetpointServiceHeatingSetpoint roomTempSetpointServiceImp = (Eq3TemperatureSetpointServiceHeatingSetpoint) tempSetpointService.getManager().getImplementation();
				roomTempSetpointServiceImp.setDeviceSerialNumber(roomTempSenseDev.getSerialNumber());
				roomTempSetpointServiceImp.setApplication("Room");
				roomTempSetpointServiceImp.setName(r.getRoomName() +" Setpoint ("+roomTempSenseDev.getType()+")");
				roomTempSetpointServiceImp.setCubeConnectionManager(cubeConManager);
				setpointUpdateRegister.add(roomTempSetpointServiceImp);	
			}
			
			// add room zone to HVAC system
			topLevelHvacSystemDevice.addChildDevice(newZone);
		}
		
		// setup Upnp service
		try {			
			this.upnpService.getRegistry().addDevice(
					topLevelHvacSystemDevice.createDevice()
			);			

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
		
		boolean continuePolling = true;
		while (continuePolling) {
			cubeConManager.processQueue(); // process all commands
			continuePolling = cubePoller();
			try {
				Thread.sleep(REFRESH_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
	boolean cubePoller() {
		boolean continuePolling = true;
		
		try {
			cubeConManager.openCubeConnection();
		} catch (UnknownHostException e) {
			logger.severe("Unable to connect to cube (UnknownHostException): "+e.getMessage());
			return true;
		} catch (IOException e) { 
			logger.severe("Unable to connect to cube (IOException): "+e.getMessage());
			return true;
		}
		
		logger.info("Successfully connected to cube at "+cubeIp);	
		
		// Find all available devices
		boolean processMessages = true;
		while (processMessages) {
			String rawLine = null;
			try {
				rawLine = cubeConManager.getCubeReader().readLine();
			} catch (Exception e) {
				logger.severe("Error reading data from cube: "+e.getMessage());
				processMessages = false;
				continue;
			}			
			
			if (rawLine == null) {
				// run out of data
				processMessages = false;
				continue;
			}
			
			Message message = null;
			try {
				cubeConManager.getMessageProcessor().addReceivedLine(rawLine);
				if (cubeConManager.getMessageProcessor().isMessageAvailable()) {
					message = cubeConManager.getMessageProcessor().pull();					
				} else {
					continue;
				}
				
				if (message != null) {
					message.debug(logger);

					// process messages
					switch (message.getType())
					{
					case L:
						logger.info("Doing update from Cube");
						((L_Message) message).updateDevices(devices, configurations);
						processMessages = false; // this is the end of stuff from the cube
						break;
					default:
						// TODO - configuration updates?
						break;
					}
				}
			} catch (IncorrectMultilineIndexException ex) {
				logger.info("Incorrect MAX!Cube multiline message detected. Stopping processing and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (NoMessageAvailableException ex) {
				logger.info("Could not process MAX!Cube message. Stopping processing and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (IncompleteMessageException ex) {
				logger.info("Error while parsing MAX!Cube multiline message. Stopping processing, and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (UnprocessableMessageException ex) {
				logger.info("Error while parsing MAX!Cube message. Stopping processing, and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (UnsupportedMessageTypeException ex) {
				logger.info("Unsupported MAX!Cube message detected. Ignoring and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (MessageIsWaitingException ex) {
				logger.info("There was and unhandled message waiting. Ignoring and continue with next Line.");
				cubeConManager.getMessageProcessor().reset();
			} catch (Exception e) {
				logger.info("Failed to process message received by MAX! protocol.");
				logger.fine(Utils.getStackTrace(e));
				cubeConManager.getMessageProcessor().reset();
			}
		}
		
		
		cubeConManager.closeCubeConnection();
		

		// TODO process the updates
		for (Device d : devices){
			// check for device in temperature sensor update registry
			ZoneTemperatureUpdate updateTempSensorTarget = null;			
			for (ZoneTemperatureUpdate tempUpdate : tempUpdateRegister) {
				if (tempUpdate.getDeviceSerialNumber().equals(d.getSerialNumber())) {
					updateTempSensorTarget = tempUpdate;
					break;
				}
			}
			// check for device in temperature setpoint update registry			
			SetpointUpdate updateSetpointTarget = null;
			for (SetpointUpdate spUpdate : setpointUpdateRegister) {
				if (spUpdate.getDeviceSerialNumber().equals(d.getSerialNumber())){
					updateSetpointTarget = spUpdate;
					break;
				}
			}
			switch (d.getType()) {
			case HeatingThermostatPlus:
			case HeatingThermostat:
				if (updateTempSensorTarget != null && ((HeatingThermostat) d).isTemperatureActualUpdated()){
					updateTempSensorTarget.zoneTemperatureSensorUpdate(((HeatingThermostat) d).getTemperatureActual());
				}
				if (updateSetpointTarget != null && ((HeatingThermostat) d).isTemperatureSetpointUpdated()){
					updateSetpointTarget.temperatureSetpointUpdate(((HeatingThermostat) d).getTemperatureSetpoint());
				}
				break;
				
			case WallMountedThermostat:
				if (updateTempSensorTarget != null && ((HeatingThermostat) d).isTemperatureActualUpdated()){
					updateTempSensorTarget.zoneTemperatureSensorUpdate(((HeatingThermostat) d).getTemperatureActual());
				}
				if (updateSetpointTarget != null && ((HeatingThermostat) d).isTemperatureSetpointUpdated()){
					updateSetpointTarget.temperatureSetpointUpdate(((HeatingThermostat) d).getTemperatureSetpoint());
				}
				break;
				
			case ShutterContact:
				break;
				
			default:
				break;
			}
		}
		return continuePolling;
	}

}
