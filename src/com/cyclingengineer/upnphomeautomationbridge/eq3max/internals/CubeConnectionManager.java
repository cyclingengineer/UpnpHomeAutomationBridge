package com.cyclingengineer.upnphomeautomationbridge.eq3max.internals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.openhab.binding.maxcube.internal.Utils;
import org.openhab.binding.maxcube.internal.message.Device;
import org.openhab.binding.maxcube.internal.message.Message;
import org.openhab.binding.maxcube.internal.message.MessageProcessor;
import org.openhab.binding.maxcube.internal.message.MessageType;
import org.openhab.binding.maxcube.internal.message.S_Command;
import org.openhab.binding.maxcube.internal.message.S_Message;
import org.openhab.binding.maxcube.internal.message.ThermostatModeType;

public class CubeConnectionManager {

	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private String cubeIp = "";
	private int cubePort = 62910;
	
	private Socket cubeSocket = null;
	private BufferedReader cubeReader = null;
	private OutputStreamWriter cubeWriter = null;
	
	private MessageProcessor messageProcessor = new MessageProcessor();
	
	private ArrayList<Device> deviceList = null;
	
	private ConcurrentLinkedQueue<String> cmdQueue = new ConcurrentLinkedQueue<String>();
	
	private Device findDeviceInList( String serialNum ) {
		Device dev = null;
		for (Device d : deviceList) {
			if (d.getSerialNumber().equals(serialNum)) {
				dev = d;
				break;
			}
		}
		return dev;
	}
	
	/**
	 * Processes the S message and updates Duty Cycle & Free Memory Slots
	 * @param S_Message message
	 */
	private boolean S_MessageStatusProcessing(S_Message message) {
		boolean success = false;
		int dutyCycle =  message.getDutyCycle();
		int freeMemorySlots = message.getFreeMemorySlots();
		if (message.isCommandDiscarded()) {
			logger.warning("Last Send Command discarded. Duty Cycle: "+dutyCycle+", Free Memory Slots: "+freeMemorySlots);
		} else {
			logger.info("S message. Duty Cycle: "+dutyCycle+", Free Memory Slots: " + freeMemorySlots);
			success = true;
		}
		return success;
	}
	
	public CubeConnectionManager(ArrayList<Device> deviceList, String cubeIp, int cubePort) {		
		this.deviceList = deviceList;
		this.cubeIp = cubeIp;
		this.cubePort = cubePort;
	}
	
	public void openCubeConnection() throws UnknownHostException, IOException {
		if (cubeSocket == null) {
			cubeSocket = new Socket(cubeIp, cubePort);
			cubeSocket.setSoTimeout(2000);
			logger.info("Established connection to MAX! cube at "+cubeIp+":"+cubePort);
			cubeReader = new BufferedReader(new InputStreamReader(cubeSocket.getInputStream()));
			cubeWriter = new OutputStreamWriter(cubeSocket.getOutputStream());
		}
	}	
	
	public void closeCubeConnection() {
		if (cubeSocket != null) {			
			try {
				cubeSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.info("Closed connection to MAX! cube");
			cubeReader = null;
			cubeWriter = null;
			cubeSocket = null;
		}
	}
	
	public Socket getCubeSocket() {
		return cubeSocket;
	}
	
	public BufferedReader getCubeReader() {
		return cubeReader;
	}
	
	public OutputStreamWriter getCubeWriter() {
		return cubeWriter;
	}
	
	public MessageProcessor getMessageProcessor() {
		return messageProcessor;
	}
	
	private boolean sendCommand( String commandString ) {
		boolean success = false;
		try {			
			this.openCubeConnection();

			cubeWriter.write(commandString);
			logger.fine(commandString);
			cubeWriter.flush();
			
			Message message = null;
			String raw = cubeReader.readLine();
            try {
                while (!this.messageProcessor.isMessageAvailable()) {
                	this.messageProcessor.addReceivedLine(raw);
                    raw = cubeReader.readLine();
                }

                message = this.messageProcessor.pull();
            } catch (Exception e) {
                logger.info("Error while handling response from MAX! Cube lan gateway!");
                logger.fine(Utils.getStackTrace(e));
                this.messageProcessor.reset();
            }
			
			if (message != null && message.getType() == MessageType.S) {				
				success = S_MessageStatusProcessing((S_Message)message);				
			}
			
			this.closeCubeConnection();			
		} catch (UnknownHostException e) {
			logger.info("Host error occurred while connecting to MAX! Cube lan gateway '"+cubeIp+"': "+ e.getMessage());
			closeCubeConnection();
		} catch (IOException e) {
			logger.info("IO error occurred while writing to MAX! Cube lan gateway '"+cubeIp+"': "+ e.getMessage());
			closeCubeConnection(); //reconnect on next execution
		} catch (Exception e) {
			logger.info("Error occurred while writing to MAX! Cube lan gateway '"+cubeIp+"': "+ e.getMessage());
			logger.info(Utils.getStackTrace(e));
			closeCubeConnection(); //reconnect on next execution
		}
		return success;
	}
	
	public void processQueue() {
		logger.info("Processing queue of size "+cmdQueue.size());
		int failCount = 0;
		String cmdStr = cmdQueue.peek();
		while (cmdStr != null) {			
			if (sendCommand(cmdStr)){
				cmdQueue.poll(); // remove it from the queue if successful
			}
			else {
				logger.info("Failed to send message");
				failCount++;
			}
			if (failCount > 0) {
				logger.info("Failed "+failCount+" times - breaking out");
				break;
			}
			cmdStr = cmdQueue.peek(); // get next
		} 
	}
	
	public void queueCommand( String cmdString ) {
		cmdQueue.add(cmdString);
	}
	
	/**
	 * Create and send a new setpoint command request
	 * @param newSetPoint Setpoint value to send to target
	 * @param devSerial Target device serial
	 */
	public void sendSetpointCommand( String devSerial, int newSetPoint ) {
		S_Command cmd = null;
		ThermostatModeType commandThermoType = ThermostatModeType.MANUAL;
		double setTemp = newSetPoint / 100;		
		Device d = findDeviceInList(devSerial);		
		
		logger.info("Sending setpoint update for device "+devSerial+" to value of "+setTemp);
		cmd = new S_Command(d.getRFAddress(), d.getRoomId(), commandThermoType, setTemp);
		queueCommand(cmd.getCommandString());
	}
}
