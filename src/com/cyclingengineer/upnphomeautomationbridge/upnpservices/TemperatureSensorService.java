package com.cyclingengineer.upnphomeautomationbridge.upnpservices;

import org.fourthline.cling.binding.annotations.*;

import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

public abstract class TemperatureSensorService {

	protected final Logger log = Logger.getLogger(this.getClass().getName());
	
	private final PropertyChangeSupport propertyChangeSupport;

    public TemperatureSensorService() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }
	
	@UpnpStateVariable(defaultValue = "none", sendEvents = true)
	private String application = "none";
	
	// rate limited @ 1 event per 10s, min delta of 0.2 deg or 20 units
	@UpnpStateVariable(defaultValue = "0", sendEvents = true, eventMaximumRateMilliseconds=10000, eventMinimumDelta=20)
	protected int currentTemperature = 0; // in 100ths of degrees
	
	@UpnpStateVariable(defaultValue = "", sendEvents = true )
	private String name = "";
	
	@UpnpAction
	public void setApplication(
		@UpnpInputArgument(name = "NewApplication") String setApplicationValue) {
		application = setApplicationValue;
		applicationUpdate( setApplicationValue );		
		log.fine("setApplication request: " + setApplicationValue);		
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "CurrentApplication"))
	public String getApplication() {
		return application;
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "CurrentTemperature"))
	public int getCurrentTemperature() {
		currentTemperature = temperatureValueRequest();
		return currentTemperature;
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "CurrentName"))
	public String getName() {
		return name;
	}
	
	@UpnpAction
	public void setName(
		@UpnpInputArgument(name = "NewName") String setNameValue) {
		name = setNameValue;
		nameUpdate( setNameValue );
	}
	
	
	
	/**
	 * Application property update event. Called on SetApplication request
	 * @param setApplicationValue Application value that is requesting to be set
	 */
	protected void applicationUpdate( String setApplicationValue ) {
		// do nothing by default
	}
	
	/**
	 * Name property update event. Called on SetName request
	 * @param setNameValue Name value that is requesting to be set
	 */
	protected void nameUpdate( String setNameValue ) {
		// do nothing by default
	}
	
	/**
	 * Method that will update currentTemperature. Called on GetCurrentTemperature.
	 * @return Updated temperature in 100ths degrees celsius
	 */
	protected abstract int temperatureValueRequest( );
	
}
