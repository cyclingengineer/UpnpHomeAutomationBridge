/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.message;

import java.util.logging.Logger;



/**
 * The battery of a MAX!Cube {@link Device}.
 * 
 * @author Dominic Lerbs
 * @since 1.7.0
 */
public class Battery {
	
	protected final Logger log = Logger.getLogger(this.getClass().getName());
	private Charge charge = Charge.UNKNOWN;
	private boolean chargeUpdated;

	public void setCharge(Charge charge) {
		chargeUpdated = (this.charge != charge);
		if (chargeUpdated){
			log.info("Battery charge changed from " + this.charge + " to " + charge);
			this.charge = charge;
		}
	}

	public String getCharge() {
		return charge.getText();
	}

	public boolean isChargeUpdated() {
		return chargeUpdated;
	}
	
	
	/** Charging state of the battery. */
	public enum Charge {
		UNKNOWN("n/a"), OK("ok"), LOW("low");

		private final String text;

		private Charge(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}
}
