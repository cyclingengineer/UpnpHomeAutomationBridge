package com.cyclingengineer.upnphomeautomationbridge.upnpservices;

import org.fourthline.cling.binding.annotations.*;
import org.fourthline.cling.model.types.UnsignedIntegerOneByte;

import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

public abstract class ControlValveService {

	protected final Logger log = Logger.getLogger(this.getClass().getName());
	
	private final PropertyChangeSupport propertyChangeSupport;

    public ControlValveService() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }
	
	@UpnpStateVariable(defaultValue = "AUTO", sendEvents = true)
	private String controlMode = "AUTO";
	
	@UpnpStateVariable(defaultValue = "0", sendEvents = false, allowedValueMaximum=100, allowedValueMinimum=0, allowedValueStep=1, datatype="ui1")
	protected UnsignedIntegerOneByte positionTarget = new UnsignedIntegerOneByte(0);
	
	@UpnpStateVariable(defaultValue = "0", sendEvents = true, allowedValueMaximum=100, allowedValueMinimum=0, allowedValueStep=1, eventMaximumRateMilliseconds=30000, eventMinimumDelta=10, datatype="ui1")
	protected UnsignedIntegerOneByte positionStatus = new UnsignedIntegerOneByte(0);
	
	@UpnpAction(name="SetMode")
	public void setControlMode(
		@UpnpInputArgument(name = "NewControlMode") String setModeValue) {
		controlMode = setModeValue;
		controlModeUpdate( setModeValue );		
		log.fine("setMode request: " + setModeValue);		
	}
	
	@UpnpAction(name="GetMode", out = @UpnpOutputArgument(name = "CurrentControlMode"))
	public String getControlMode() {
		return controlMode;
	}
	
	@UpnpAction(name="GetPosition", out = @UpnpOutputArgument(name = "CurrentPositionStatus"))
	public UnsignedIntegerOneByte getPositionStatus() {
		positionStatus = currentPositionRequest();
		return positionStatus;
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "CurrentPositionTarget"))
	public UnsignedIntegerOneByte getPositionTarget() {
		positionTarget = currentPositionTargetRequest();
		return positionTarget;
	}
	
	@UpnpAction(name="SetPosition")
	public void setPositionTarget(
		@UpnpInputArgument(name = "NewPositionTarget") UnsignedIntegerOneByte setPositionTarget) {
		positionTarget = setPositionTarget;
		positionTargetUpdate( setPositionTarget );
	}
	
	
	
	/**
	 * Application property update event. Called on SetMode request
	 * @param setModeValue Control Mode value that is requesting to be set
	 */
	protected void controlModeUpdate( String setModeValue ) {
		// do nothing by default
	}
	
	/**
	 * Position target update event. Called on SetPosition request
	 * @param setPositionTarget Target value that is requesting to be set in %
	 */
	protected void positionTargetUpdate( UnsignedIntegerOneByte setPositionTarget ) {
		// do nothing by default
	}
	
	/**
	 * Method that will update currentPosition. Called on GetPosition.
	 * @return Updated position in percentage of valve travel
	 */
	protected abstract UnsignedIntegerOneByte currentPositionRequest( );
	
	/**
	 * Method that will update currentTargetPosition. Called on GetPositionTarget.
	 * @return Updated target position in percentage of valve travel
	 */
	protected abstract UnsignedIntegerOneByte currentPositionTargetRequest( );
}
