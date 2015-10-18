/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.message;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.openhab.binding.maxcube.internal.Utils;

/**
 * The C message contains configuration about a MAX! device.
 * 
 * @author Andreas Heil (info@aheil.de)
 * @since 1.4.0
 */
public final class C_Message extends Message {

	protected final Logger logger = Logger.getLogger(this.getClass().getName());

	private String rfAddress = null;
	private int length = 0;
	private DeviceType deviceType = null;
	private String serialNumber = null;
	private String tempComfort= null;
	private String tempEco = null;
	private String tempSetpointMax= null;
	private String tempSetpointMin= null;
	private String tempOffset = null;
	private String tempOpenWindow = null;
	private String durationOpenWindow = null;
	private String decalcification = null;
	private String valveMaximum = null;
	private String valveOffset = null;
	private String programData = null;
	private String boostDuration = null;
	private String boostValve = null;


	public C_Message(String raw) {
		super(raw);
		logger.fine(" *** C-Message ***");
		String[] tokens = this.getPayload().split(Message.DELIMETER);

		rfAddress = tokens[0];

		byte[] bytes = Base64.decodeBase64(tokens[1].getBytes());

		int[] data = new int[bytes.length];

		for (int i = 0; i < bytes.length; i++) {
			data[i] = bytes[i] & 0xFF;
		}

		length = data[0];
		if (length != data.length - 1) {
			logger.fine("C_Message malformed: wrong data length. Expected bytes "+length+", actual bytes "+ (data.length - 1));
		}

		String rfAddress2 = Utils.toHex(data[1], data[2], data[3]);
		if (!rfAddress.toUpperCase().equals(rfAddress2.toUpperCase())) {
			logger.fine("C_Message malformed: wrong RF address. Expected address "+rfAddress.toUpperCase()+", actual address "+ rfAddress2.toUpperCase());
		}

		deviceType = DeviceType.create(data[4]);

		serialNumber = getSerialNumber(bytes);
		if (deviceType == DeviceType.HeatingThermostatPlus || deviceType == DeviceType.HeatingThermostat || deviceType == DeviceType.WallMountedThermostat)  parseHeatingThermostatData (bytes);
		if (deviceType == DeviceType.EcoSwitch || deviceType == DeviceType.ShutterContact)  logger.finer("Device "+rfAddress+" type "+deviceType.toString()+" Data:"+ parseData (bytes));
	}

	private String getSerialNumber(byte[] bytes) {
		byte[] sn = new byte[10];

		for (int i = 0; i < 10; i++) {
			sn[i] = (byte) bytes[i + 8];
		}

		try {
			return new String(sn, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.fine("Cannot encode serial number from C message due to encoding issues.");
		}

		return "";
	}

	private String parseData(byte[] bytes) {
		if (bytes.length <= 18) return "";
		try{
			int DataStart = 18;
			byte[] sn = new byte[bytes.length - DataStart];

			for (int i = 0; i < sn.length; i++) {
				sn[i] = (byte) bytes[i + DataStart];
			}
			logger.finer("DataBytes: " + Utils.getHex(sn));
			try {
				return new String(sn, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.fine("Cannot encode device string from C message due to encoding issues.");
			}

		}  catch (Exception e) {
			logger.fine(e.getMessage());
			logger.fine(Utils.getStackTrace(e));
		}

		return "";
	}

	private void parseHeatingThermostatData(byte[] bytes) {
		try{

			int plusDataStart = 18;
			int programDataStart = 11;
			tempComfort= Float.toString( bytes[plusDataStart ]/2);
			tempEco = Float.toString( bytes[plusDataStart + 1]/2);
			tempSetpointMax=  Float.toString( bytes[plusDataStart + 2]/2);
			tempSetpointMin=  Float.toString( bytes[plusDataStart + 3]/2);

			logger.fine("DeviceType:             "+ deviceType.toString());
			logger.fine("RFAddress:              "+ rfAddress);
			logger.fine("Temp Comfort:           "+ tempComfort);
			logger.fine("TempEco:                "+ tempEco);
			logger.fine("Temp Setpoint Max:      "+ tempSetpointMax);
			logger.fine("Temp Setpoint Min:      "+ tempSetpointMin);			

			if (bytes.length < 211) {
				// Device is a WallMountedThermostat
				programDataStart = 4;
				logger.finer("WallThermostat byte "+(bytes.length -3)+": "+ Float.toString( bytes[bytes.length -3]&0xFF));
				logger.finer("WallThermostat byte "+(bytes.length -2)+": "+ Float.toString( bytes[bytes.length -2]&0xFF));
				logger.finer("WallThermostat byte "+(bytes.length -1)+": "+ Float.toString( bytes[bytes.length -1]&0xFF));
			} else
			{
				// Device is a HeatingThermostat(+)
				tempOffset =  Double.toString( (bytes[plusDataStart +4 ]/2) - 3.5);
				tempOpenWindow =  Float.toString( bytes[plusDataStart + 5]/2);
				durationOpenWindow =  Float.toString( bytes[plusDataStart + 6]);
				boostDuration =  Float.toString( bytes[plusDataStart + 7]&0xFF >> 5 );
				boostValve =  Float.toString( (bytes[plusDataStart + 7]&0x1F)*5);
				decalcification =  Float.toString( bytes[plusDataStart + 8]);
				valveMaximum = Float.toString( bytes[plusDataStart + 9]&0xFF * 100 / 255);
				valveOffset = Float.toString( bytes[plusDataStart+ 10]&0xFF * 100 / 255 );
				logger.fine("Temp Offset:            "+ tempOffset);
				logger.fine("Temp Open Window:       "+ tempOpenWindow );
				logger.fine("Duration Open Window:   "+ durationOpenWindow);
				logger.fine("Duration Boost:         "+ boostDuration);
				logger.fine("Boost Valve Pos:        "+ boostValve);
				logger.fine("Decalcification:        "+ decalcification);
				logger.fine("ValveMaximum:           "+ valveMaximum);
				logger.fine("ValveOffset:            "+ valveOffset);
			}
			programData = "";
			int ln = 13 * 6; //first day = Sat 
			String startTime = "00:00h";
			for (int char_idx = plusDataStart + programDataStart; char_idx < (plusDataStart + programDataStart +  26*7); char_idx++) {
				if (ln % 13 == 0 ) { programData += "\r\n Day " + Integer.toString((ln / 13) % 7 ) + ": "; startTime = "00:00h"; }
				int progTime =  (bytes[char_idx+1]&0xFF ) * 5 + (bytes[char_idx]&0x01 ) * 1280 ;
				int progMinutes = progTime % 60;
				int progHours = (progTime - progMinutes ) / 60;
				String endTime = Integer.toString(progHours) + ":" + String.format("%02d", progMinutes) + "h";
				programData += startTime + "-" + endTime + " " + Double.toString(bytes[char_idx] /4) + "C  ";
				startTime = endTime;
				char_idx++;
				ln++;
			}
			logger.fine("ProgramData:          "+ programData);

		}  catch (Exception e) {
			logger.fine(e.getMessage());
			logger.fine(Utils.getStackTrace(e));
		}
		return ;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	@Override
	public MessageType getType() {
		return MessageType.C;
	}

	public String getRFAddress() {
		return rfAddress;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	@Override
	public void debug(Logger logger) {
		logger.fine("=== C_Message === ");
		logger.finer("\tRAW:        "+ this.getPayload());
		logger.fine("DeviceType:    "+ deviceType.toString());
		logger.fine("SerialNumber:  "+ serialNumber);
		logger.fine("RFAddress:     "+ rfAddress);
	}
}
