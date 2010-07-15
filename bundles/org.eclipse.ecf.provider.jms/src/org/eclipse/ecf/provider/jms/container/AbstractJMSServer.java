/****************************************************************************
 * Copyright (c) 2004 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.jms.container;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketAddress;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.ContainerConnectedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectHandlerPolicy;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.jms.*;
import org.eclipse.ecf.provider.comm.*;
import org.eclipse.ecf.provider.generic.ContainerMessage;
import org.eclipse.ecf.provider.generic.ServerSOContainer;
import org.eclipse.ecf.provider.jms.channel.*;

/**
 * Abstract JMS Server. Subclasses should be created to create concrete
 * instances of a JMS Server container.
 */
public abstract class AbstractJMSServer extends ServerSOContainer {

	public static final int DEFAULT_KEEPALIVE = 30000;

	private IConnectHandlerPolicy joinPolicy = null;

	private ISynchAsynchConnection serverChannel;

	public AbstractJMSServer(JMSContainerConfig config) {
		super(config);
	}

	/**
	 * Start this server. Subclasses must override this method to start a JMS
	 * server.
	 * 
	 * @throws ECFException
	 *             if some problem with starting the server (e.g. port already
	 *             taken)
	 */
	public abstract void start() throws ECFException;

	protected JMSContainerConfig getJMSContainerConfig() {
		return (JMSContainerConfig) getConfig();
	}

	protected void setConnection(ISynchAsynchConnection channel) {
		this.serverChannel = channel;
	}

	protected ISynchAsynchConnection getConnection() {
		return serverChannel;
	}

	protected IConnectHandlerPolicy getConnectHandlerPolicy() {
		return joinPolicy;
	}

	protected void setConnectHandlerPolicy(IConnectHandlerPolicy policy) {
		this.joinPolicy = policy;
	}

	/**
	 * @param e
	 * @return Serializable result of this synchronous processing.
	 * @throws IOException not thrown by this implementation.
	 */
	protected Serializable processSynch(SynchEvent e) throws IOException {
		final Object req = e.getData();
		if (req instanceof ConnectRequestMessage) {
			return handleConnectRequest((ConnectRequestMessage) req, (AbstractJMSServerChannel) e.getConnection());
		} else if (req instanceof DisconnectRequestMessage) {
			// disconnect them
			final DisconnectRequestMessage dcm = (DisconnectRequestMessage) req;
			final IAsynchConnection conn = getConnectionForID(dcm.getSenderID());
			if (conn != null && conn instanceof AbstractJMSServerChannel.Client) {
				final AbstractJMSServerChannel.Client client = (AbstractJMSServerChannel.Client) conn;
				client.handleDisconnect();
			}
		}
		return null;
	}

	protected void traceAndLogExceptionCatch(int code, String method, Throwable e) {
		Trace.catching(Activator.PLUGIN_ID, JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), method, e);
		Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, method, e));
	}

	protected void handleConnectException(ContainerMessage mess, AbstractJMSServerChannel serverChannel1, Exception e) {
		// no default implementation
	}

	protected Object checkJoin(SocketAddress socketAddress, ID fromID, String targetPath, Serializable data) throws Exception {
		if (joinPolicy != null)
			return joinPolicy.checkConnect(socketAddress, fromID, getID(), targetPath, data);
		return null;
	}

	protected Serializable handleConnectRequest(ConnectRequestMessage request, AbstractJMSServerChannel channel) {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "handleConnectRequest", new Object[] { //$NON-NLS-1$
				request, channel});
		try {
			final ContainerMessage containerMessage = (ContainerMessage) request.getData();
			if (containerMessage == null)
				throw new InvalidObjectException(Messages.AbstractJMSServer_CONNECT_EXCEPTION_CONTAINER_MESSAGE_NOT_NULL);
			final ID remoteID = containerMessage.getFromContainerID();
			if (remoteID == null)
				throw new InvalidObjectException(Messages.AbstractJMSServer_CONNECT_EXCEPTION_REMOTEID_NOT_NULL);
			final ContainerMessage.JoinGroupMessage jgm = (ContainerMessage.JoinGroupMessage) containerMessage.getData();
			if (jgm == null)
				throw new InvalidObjectException(Messages.AbstractJMSServer_CONNECT_EXCEPTION_JOINGROUPMESSAGE_NOT_NULL);
			ID memberIDs[] = null;
			final Serializable[] messages = new Serializable[2];
			AbstractJMSServerChannel.Client newclient = null;
			synchronized (getGroupMembershipLock()) {
				if (isClosing)
					throw new ContainerConnectException(Messages.AbstractJMSServer_CONNECT_EXCEPTION_CONTAINER_CLOSING);
				// Now check to see if this request is going to be allowed
				checkJoin(channel, remoteID, request.getTargetJMSID().getTopicOrQueueName(), jgm.getData());

				newclient = channel.new Client(remoteID);

				if (addNewRemoteMember(remoteID, newclient)) {
					// Get current membership
					memberIDs = getGroupMemberIDs();
					// Notify existing remotes about new member
					messages[1] = serialize(ContainerMessage.createViewChangeMessage(getID(), null, getNextSequenceNumber(), new ID[] {remoteID}, true, null));
				} else {
					final ConnectException e = new ConnectException(Messages.AbstractJMSServer_CONNECT_EXCEPTION_REFUSED);
					throw e;
				}
			}
			// notify listeners
			fireContainerEvent(new ContainerConnectedEvent(this.getID(), remoteID));

			messages[0] = serialize(ContainerMessage.createViewChangeMessage(getID(), remoteID, getNextSequenceNumber(), memberIDs, true, null));

			newclient.start();

			return messages;

		} catch (final Exception e) {
			traceAndLogExceptionCatch(IStatus.ERROR, "handleConnectRequest", e); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * @param from
	 * @param excluding
	 * @param data
	 * @throws IOException not thrown by this implementation.
	 */
	protected void forwardExcluding(ID from, ID excluding, ContainerMessage data) throws IOException {
		// no forwarding necessary
	}

	/**
	 * @param from
	 * @param to
	 * @param data
	 * @throws IOException not thrown by this implementation.
	 */
	protected void forwardToRemote(ID from, ID to, ContainerMessage data) throws IOException {
		// no forwarding necessary
	}

	protected void queueContainerMessage(ContainerMessage mess) throws IOException {
		serverChannel.sendAsynch(mess.getToContainerID(), serialize(mess));
	}

	protected void handleLeave(ID target, IConnection conn) {
		if (target == null)
			return;
		if (removeRemoteMember(target)) {
			try {
				queueContainerMessage(ContainerMessage.createViewChangeMessage(getID(), null, getNextSequenceNumber(), new ID[] {target}, false, null));
			} catch (final IOException e) {
				traceAndLogExceptionCatch(IStatus.ERROR, "memberLeave", e); //$NON-NLS-1$
			}
		}
		if (conn != null)
			disconnect(conn);
	}

}
