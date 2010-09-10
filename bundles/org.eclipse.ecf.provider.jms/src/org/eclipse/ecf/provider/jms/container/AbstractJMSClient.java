/*******************************************************************************
 * Copyright (c) 2004 2007 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.container;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.Map;
import org.eclipse.ecf.core.events.ContainerDisconnectedEvent;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.sharedobject.util.IQueueEnqueue;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.comm.SynchEvent;
import org.eclipse.ecf.provider.generic.*;
import org.eclipse.ecf.provider.jms.channel.DisconnectRequestMessage;
import org.eclipse.ecf.provider.jms.identity.JMSNamespace;

/**
 * Abstract JMS Client. Subclasses should be created to create concrete
 * instances of a JMS Client container.
 */
public abstract class AbstractJMSClient extends ClientSOContainer {

	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(JMSNamespace.NAME);
	}

	public AbstractJMSClient() throws Exception {
		this(new JMSContainerConfig());
	}

	public AbstractJMSClient(int keepAlive) {
		this(new JMSContainerConfig(keepAlive));
	}

	public AbstractJMSClient(JMSContainerConfig config) {
		super(config);
	}

	protected void handleContainerMessage(ContainerMessage mess) throws IOException {
		if (mess == null) {
			debug("got null container message...ignoring"); //$NON-NLS-1$
			return;
		}
		Object data = mess.getData();
		if (data instanceof ContainerMessage.CreateMessage) {
			handleCreateMessage(mess);
		} else if (data instanceof ContainerMessage.CreateResponseMessage) {
			handleCreateResponseMessage(mess);
		} else if (data instanceof ContainerMessage.SharedObjectMessage) {
			handleSharedObjectMessage(mess);
		} else if (data instanceof ContainerMessage.SharedObjectDisposeMessage) {
			handleSharedObjectDisposeMessage(mess);
		} else {
			debug("got unrecognized container message...ignoring: " + mess); //$NON-NLS-1$
		}
	}

	class JMSContainerContext extends SOContext {

		public JMSContainerContext(ID objID, ID homeID, SOContainer cont, Map props, IQueueEnqueue queue) {
			super(objID, homeID, cont, props, queue);
		}
	}

	protected SOContext createSharedObjectContext(SOConfig soconfig, IQueueEnqueue queue) {
		return new JMSContainerContext(soconfig.getSharedObjectID(), soconfig.getHomeContainerID(), this, soconfig.getProperties(), queue);
	}

	/**
	 * @param e
	 * @return Serializable result of the synchronous processing.
	 * @throws IOException not thrown by this implementation.
	 */
	protected Serializable processSynch(SynchEvent e) throws IOException {
		Object req = e.getData();
		if (req instanceof DisconnectRequestMessage) {
			handleDisconnectRequest((DisconnectRequestMessage) req);
		}
		return null;
	}

	protected void handleDisconnectRequest(DisconnectRequestMessage request) {
		ID fromID = request.getSenderID();
		if (fromID == null)
			return;
		ISynchAsynchConnection conn = getConnection();
		handleLeave(fromID, conn);
		// Notify listeners
		fireContainerEvent(new ContainerDisconnectedEvent(getID(), fromID));
	}

	protected ID handleConnectResponse(ID originalTarget, Object serverData) throws Exception {
		Object cr = null;
		if (serverData instanceof byte[]) {
			cr = deserializeContainerMessage((byte[]) serverData);
		} else if (serverData instanceof ContainerMessage) {
			cr = serverData;
		} else {
			throw new ConnectException("Invalid server response to connect request"); //$NON-NLS-1$
		}
		return super.handleConnectResponse(originalTarget, cr);
	}
}