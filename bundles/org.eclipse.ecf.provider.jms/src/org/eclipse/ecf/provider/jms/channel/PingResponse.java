/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.channel;

import java.io.Serializable;
import org.eclipse.ecf.core.identity.ID;

public class PingResponse implements Serializable, ECFMessage, SynchResponseMessage {

	private static final long serialVersionUID = 8216706445987157595L;
	ID targetID;
	ID clientID;

	public PingResponse(ID clientID, ID targetID) {
		this.clientID = clientID;
		this.targetID = targetID;
	}

	public ID getTargetID() {
		return targetID;
	}

	public ID getSenderID() {
		return clientID;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("PingResponse["); //$NON-NLS-1$
		buf.append(clientID).append(";").append(targetID).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

	public Serializable getData() {
		return null;
	}

	public String getSenderJMSID() {
		return null;
	}

}
