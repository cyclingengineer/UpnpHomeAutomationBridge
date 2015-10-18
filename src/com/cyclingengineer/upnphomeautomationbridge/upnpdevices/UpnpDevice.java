package com.cyclingengineer.upnphomeautomationbridge.upnpdevices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;

public abstract class UpnpDevice {

	protected List<LocalService<?>> serviceList = new ArrayList<LocalService<?>>();
	protected List<UpnpDevice> embeddedDevicesList = new ArrayList<UpnpDevice>();
	
	public LocalService<?> addServiceToDevice( Class<?> serviceClass ) {
		LocalService newLocalService = 
				new AnnotationLocalServiceBinder().read(serviceClass);

		newLocalService.setManager(
				new DefaultServiceManager(
						newLocalService, serviceClass
				)
		);
		serviceList.add(newLocalService);
		return newLocalService;
	}
	
	public void addChildDevice( UpnpDevice d ) {
		embeddedDevicesList.add(d);
	}
	
	
	public abstract LocalDevice createDevice( ) 
			throws ValidationException,
			LocalServiceBindingException, 
			IOException;
}
