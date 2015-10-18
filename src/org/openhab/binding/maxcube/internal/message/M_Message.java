/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.message;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.openhab.binding.maxcube.internal.Utils;

/**
 * The M message contains metadata about the MAX!Cube setup. 
 * 
 * @author Andreas Heil (info@aheil.de)
 * @since 1.4.0
 */
public final class M_Message extends Message {

	public ArrayList<RoomInformation> rooms;
	public ArrayList<DeviceInformation> devices;
	private Boolean hasConfiguration ;
	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	

	public M_Message(String raw) {
		super(raw);
		hasConfiguration = false;

		String[] tokens = this.getPayload().split(Message.DELIMETER);

		if (tokens.length > 1) try {
			byte[] bytes = Base64.decodeBase64(tokens[2].getBytes());
			
			hasConfiguration = true;
			logger.finer("*** M_Message trace**** ");
			logger.finer ("\tMagic? (expect 86) : "+ (int) bytes[0]);
			logger.finer ("\tVersion? (expect 2): "+ (int) bytes[1]);
			logger.finer ("\t#defined rooms in M: "+ (int) bytes[2]);
			
			
			rooms = new ArrayList<RoomInformation>();
			devices = new ArrayList<DeviceInformation>();

			int roomCount = bytes[2];

			int byteOffset = 3; // start of rooms

			/* process room */

			for (int i = 0; i < roomCount; i++) {

				int position = bytes[byteOffset++];
				
				int nameLength = (int) bytes[byteOffset++] & 0xff;
				byte[] data = new byte[nameLength];
				System.arraycopy(bytes, byteOffset, data, 0, nameLength);
				byteOffset += nameLength;
				String name = new String(data, "UTF-8");

				String rfAddress = Utils.toHex(((int)bytes[byteOffset] & 0xff), ((int)bytes[byteOffset+1] & 0xff), ((int)bytes[byteOffset + 2] & 0xff));
				byteOffset += 3;

				rooms.add(new RoomInformation(position, name, rfAddress));
			}

			/* process devices */

			int deviceCount = bytes[byteOffset++];

			for (int deviceId = 0; deviceId < deviceCount; deviceId++) {
				DeviceType deviceType = DeviceType.create(bytes[byteOffset++]);

				String rfAddress = Utils.toHex(((int)bytes[byteOffset]&0xff), ((int)bytes[byteOffset+1]&0xff), ((int)bytes[byteOffset+2]&0xff));
				byteOffset += 3;

				String serialNumber = "";

				for (int i = 0; i < 10; i++) {
					serialNumber += (char) bytes[byteOffset++];
				}

				int nameLength = (int)bytes[byteOffset++] & 0xff;
				byte[] data = new byte[nameLength];
				System.arraycopy(bytes, byteOffset, data, 0, nameLength);
				byteOffset += nameLength;
				String deviceName = new String(data, "UTF-8");

				int roomId = (int)bytes[byteOffset++] & 0xff;
				devices.add(new DeviceInformation(deviceType, serialNumber, rfAddress, deviceName, roomId));	
			}
		}  catch (Exception e) {
			logger.info("Unknown error parsing the M Message");
			logger.info(e.getMessage());
			logger.finer(Utils.getStackTrace(e));
			logger.fine("\tRAW : "+ this.getPayload());
		}
		else {
			logger.info("No rooms defined. Configure your Max!Cube");
			hasConfiguration = false;
		} 
	}

	@Override
	public void debug(Logger logger) {
		logger.fine("=== M_Message === ");
		if (hasConfiguration) {
			logger.finer("\tRAW : "+ this.getPayload());
			for(RoomInformation room: rooms){
				logger.fine("\t=== Rooms ===");
				logger.fine("\tRoom Pos   : {}"+ room.getPosition());
				logger.fine("\tRoom Name  : {}"+ room.getName());
				logger.fine("\tRoom RF Adr: {}"+  room.getRFAddress());
				for(DeviceInformation device: devices){
					if (room.getPosition() == device.getRoomId()) {
						logger.fine("\t=== Devices ===");
						logger.fine("\tDevice Type    : "+ device.getDeviceType());
						logger.fine("\tDevice Name    : "+ device.getName());
						logger.fine("\tDevice Serialnr: "+ device.getSerialNumber());
						logger.fine("\tDevice RF Adr  : "+ device.getRFAddress());
						logger.fine("\tRoom Id        : "+ device.getRoomId());
					}
				}

			}
		} 
		else {
			logger.fine("M-Message empty. No Configuration");
		}
	}

	@Override
	public MessageType getType() {
		return MessageType.M;
	}
}
