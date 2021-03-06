/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.exceptions;

/**
 * Will be thrown when there is an attempt to put a new message line into the message processor,
 * the processor detects a known message indicator, but the message could not be parsed correctly.
 * 
 * @author Christian Rockrohr <christian@rockrohr.de>
 */
public class UnprocessableMessageException extends Exception {

	private static final long serialVersionUID = 648805723501492041L;

}
