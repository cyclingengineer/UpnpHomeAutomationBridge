package com.cyclingengineer.upnphomeautomationbridge.upnpdevices;

import java.io.IOException;

import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import com.cyclingengineer.upnphomeautomationbridge.upnpservices.SwitchPowerService;

public abstract class BinaryLightDevice extends SwitchPowerService {

	private String deviceIdentity; 
	private String friendlyName;
	private String manufacturerName; 
	private String modelName;
	private String modelDescription; 
	private String modelNumber;
	
	public BinaryLightDevice(String deviceIdentity, String friendlyName, String manufacturerName, String modelName, String modelDescription, String modelNumber) {
		super();
		this.deviceIdentity = deviceIdentity; 
		this.friendlyName = friendlyName;
		this.manufacturerName = manufacturerName; 
		this.modelName = modelName;
		this.modelDescription = modelDescription; 
		this.modelNumber = modelNumber;
	}	

	public LocalDevice createDevice( ) 
			throws ValidationException,
			LocalServiceBindingException, 
			IOException {

		DeviceIdentity identity = new DeviceIdentity(
				UDN.uniqueSystemIdentifier(deviceIdentity));

		DeviceType type = new UDADeviceType("BinaryLight", 1);

		DeviceDetails details = new DeviceDetails(friendlyName,
				new ManufacturerDetails(manufacturerName), new ModelDetails(
						modelName, modelDescription, modelNumber));

		/*Icon icon = new Icon("image/png", 48, 48, 8, getClass().getResource(
				"icon.png"));*/

		LocalService<BinaryLightDevice> switchPowerService = new AnnotationLocalServiceBinder()
				.read(BinaryLightDevice.class);

		switchPowerService.setManager(new DefaultServiceManager<BinaryLightDevice>(
				switchPowerService, BinaryLightDevice.class));

		return new LocalDevice(identity, type, details, /*icon,*/
				switchPowerService);

		/*
		 * Several services can be bound to the same device: return new
		 * LocalDevice( identity, type, details, icon, new LocalService[]
		 * {switchPowerService, myOtherService} );
		 */

	}
}
