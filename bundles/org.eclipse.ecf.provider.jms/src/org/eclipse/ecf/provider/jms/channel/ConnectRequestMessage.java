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
import org.eclipse.ecf.provider.jms.identity.JMSID;

public class ConnectRequestMessage implements Serializable, ECFMessage, SynchRequestMessage {

	private static final long serialVersionUID = -1660845225936582555L;

	String jmsTopicClientID;

	JMSID targetID;

	ID clientID;

	Serializable data;

	public ConnectRequestMessage(String jmsTopicClientID, ID clientID, JMSID targetID, Serializable data) {
		this.clientID = clientID;
		this.targetID = targetID;
		this.jmsTopicClientID = jmsTopicClientID;
		this.data = data;
	}

	public JMSID getTargetJMSID() {
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
		StringBuffer buf = new StringBuffer("ConnectRequestMessage["); //$NON-NLS-1$
		buf.append(clientID).append(";").append(targetID).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(jmsTopicClientID).append(";").append(data).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.provider.jms.channel.ECFMessage#getTargetID()
	 */
	public ID getTargetID() {
		return targetID;
	}

}
