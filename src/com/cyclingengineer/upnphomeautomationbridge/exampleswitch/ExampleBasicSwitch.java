package com.cyclingengineer.upnphomeautomationbridge.exampleswitch;

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

import com.cyclingengineer.upnphomeautomationbridge.upnpservices.SwitchPower;

public class ExampleBasicSwitch extends SwitchPower {

	public ExampleBasicSwitch() {
		super();
	}

	/**
	 * Implement actuation of the switch
	 * 
	 * @param targetStatus
	 *            Status we are attempting to set
	 * @return Status that is achieved
	 */
	protected boolean actuateSwitch(boolean targetStatus) {
		String resultString = targetStatus ? "On" : "Off";
		this.log.info("Actuating switch to " + resultString);
		return targetStatus;
	}

	public LocalDevice createDevice() throws ValidationException,
			LocalServiceBindingException, IOException {

		DeviceIdentity identity = new DeviceIdentity(
				UDN.uniqueSystemIdentifier("Example Binary Light"));

		DeviceType type = new UDADeviceType("BinaryLight", 1);

		DeviceDetails details = new DeviceDetails("Friendly Example Binary Light",
				new ManufacturerDetails("ACME"), new ModelDetails(
						"BinLight2000", "A demo light with on/off switch.",
						"v1"));

		/*Icon icon = new Icon("image/png", 48, 48, 8, getClass().getResource(
				"icon.png"));*/

		LocalService<ExampleBasicSwitch> switchPowerService = new AnnotationLocalServiceBinder()
				.read(ExampleBasicSwitch.class);

		switchPowerService.setManager(new DefaultServiceManager<ExampleBasicSwitch>(
				switchPowerService, ExampleBasicSwitch.class));

		return new LocalDevice(identity, type, details, /*icon,*/
				switchPowerService);

		/*
		 * Several services can be bound to the same device: return new
		 * LocalDevice( identity, type, details, icon, new LocalService[]
		 * {switchPowerService, myOtherService} );
		 */

	}
}
