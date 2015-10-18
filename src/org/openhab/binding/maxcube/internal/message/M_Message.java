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

import org.apache.commons.codec.binary.Base64;
import org.openhab.binding.maxcube.internal.MaxCubeBinding;
import org.openhab.binding.maxcube.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	Logger logger = LoggerFactory.getLogger(MaxCubeBinding.class);
	

	public M_Message(String raw) {
		super(raw);
		hasConfiguration = false;

		String[] tokens = this.getPayload().split(Message.DELIMETER);

		if (tokens.length > 1) try {
			byte[] bytes = Base64.decodeBase64(tokens[2].getBytes());
			
			hasConfiguration = true;
			logger.trace("*** M_Message trace**** ");
			logger.trace ("\tMagic? (expect 86) : {}", (int) bytes[0]);
			logger.trace ("\tVersion? (expect 2): {}", (int) bytes[1]);
			logger.trace ("\t#defined rooms in M: {}", (int) bytes[2]);
			
			
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
			logger.debug(Utils.getStackTrace(e));
			logger.debug("\tRAW : {}", this.getPayload());
		}
		else {
			logger.info("No rooms defined. Configure your Max!Cube");
			hasConfiguration = false;
		} 
	}

	@Override
	public void debug(Logger logger) {
		logger.debug("=== M_Message === ");
		if (hasConfiguration) {
			logger.trace("\tRAW : {}", this.getPayload());
			for(RoomInformation room: rooms){
				logger.debug("\t=== Rooms ===");
				logger.debug("\tRoom Pos   : {}", room.getPosition());
				logger.debug("\tRoom Name  : {}", room.getName());
				logger.debug("\tRoom RF Adr: {}",  room.getRFAddress());
				for(DeviceInformation device: devices){
					if (room.getPosition() == device.getRoomId()) {
						logger.debug("\t=== Devices ===");
						logger.debug("\tDevice Type    : {}", device.getDeviceType());
						logger.debug("\tDevice Name    : {}", device.getName());
						logger.debug("\tDevice Serialnr: {}", device.getSerialNumber());
						logger.debug("\tDevice RF Adr  : {}", device.getRFAddress());
						logger.debug("\tRoom Id        : {}", device.getRoomId());
					}
				}

			}
		} 
		else {
			logger.debug("M-Message empty. No Configuration");
		}
	}

	@Override
	public MessageType getType() {
		return MessageType.M;
	}
}
