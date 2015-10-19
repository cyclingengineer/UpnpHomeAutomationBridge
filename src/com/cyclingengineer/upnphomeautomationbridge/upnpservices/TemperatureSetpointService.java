package com.cyclingengineer.upnphomeautomationbridge.upnpservices;

import org.fourthline.cling.binding.annotations.*;

import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

public abstract class TemperatureSetpointService {

	protected final Logger log = Logger.getLogger(this.getClass().getName());
	
	private final PropertyChangeSupport propertyChangeSupport;

    public TemperatureSetpointService() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }
	
	@UpnpStateVariable(defaultValue = "none", sendEvents = true)
	private String application = "none";
	
	// rate limited @ 1 event per 10s, min delta of 0.2 deg or 20 units
	@UpnpStateVariable(defaultValue = "1650", sendEvents = true, eventMaximumRateMilliseconds=10000, eventMinimumDelta=20, allowedValueMaximum=3050, allowedValueMinimum=450)
	protected int currentSetpoint = 1650; // in 100ths of degrees
	
	@UpnpStateVariable(defaultValue = "", sendEvents = true )
	private String name = "";
	
	@UpnpAction
	public void setApplication(
		@UpnpInputArgument(name = "NewApplication") String setApplicationValue) {
		application = setApplicationValue;
		applicationUpdate( setApplicationValue );		
		log.fine("SetApplication request: " + setApplicationValue);		
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "CurrentApplication"))
	public String getApplication() {
		return application;
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "CurrentSetpoint"))
	public int getCurrentSetpoint() {
		currentSetpoint = setpointValueRequest();
		return currentSetpoint;
	}
	
	@UpnpAction
	public void setCurrentSetpoint(
		@UpnpInputArgument(name = "NewCurrentSetpoint") int setCurrentSetpointValue) {
		currentSetpoint = setCurrentSetpointValue;
		setpointUpdate( setCurrentSetpointValue );		
		log.fine("SetCurrentSetpoint request: " + setCurrentSetpointValue);		
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
	 * Method that will update currentSetpoint. Called on GetCurrentSetpoint.
	 * @return Updated setpoint in 100ths degrees celsius
	 */
	protected abstract int setpointValueRequest( );
	
	/**
	 * Method called on request to set new setpoint
	 * @param newSetPoint Requested setpoint
	 */
	protected abstract void setpointUpdate( int newSetPoint );
	
}
