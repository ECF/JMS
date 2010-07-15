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

public class DisconnectRequestMessage implements Serializable, ECFMessage, SynchRequestMessage {

	private static final long serialVersionUID = -6596357386291085977L;

	private String jmsTopicClientID;

	private ID targetID;

	private ID clientID;

	private Serializable data;

	public DisconnectRequestMessage(String jmsTopicClientID, ID clientID, ID targetID, Serializable data) {
		this.clientID = clientID;
		this.targetID = targetID;
		this.jmsTopicClientID = jmsTopicClientID;
		this.data = data;
	}

	public ID getTargetID() {
		return targetID;
	}

	public ID getSenderID() {
		return clientID;
	}

	public String getSenderJMSID() {
		return jmsTopicClientID;
	}

	public Serializable getData() {
		return data;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("DisconnectRequestMessage["); //$NON-NLS-1$
		buf.append(clientID).append(";").append(targetID).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(jmsTopicClientID).append(";").append(data).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

}
