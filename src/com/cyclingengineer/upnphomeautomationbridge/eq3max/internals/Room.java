package com.cyclingengineer.upnphomeautomationbridge.eq3max.internals;

import java.util.ArrayList;

public class Room {

	private String name = "";
	private int roomId = -1;
	private ArrayList<String> roomDeviceSerialList = new ArrayList<String>();
	
	public Room(String name, int id) {
		this.name = name;
		this.roomId = id;
	}
	
	public void addDevice( String deviceSerial )
	{
		roomDeviceSerialList.add(deviceSerial);
	}
	
	public ArrayList<String> getRoomDeviceList() {
		return roomDeviceSerialList;
	}

	public int getRoomId() {
		return roomId;
	}
	
	public String getRoomName() {
		return name;
	}
}
