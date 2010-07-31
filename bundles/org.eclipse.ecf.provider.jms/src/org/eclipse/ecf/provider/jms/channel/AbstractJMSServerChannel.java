/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.channel;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.jms.*;
import org.eclipse.ecf.provider.comm.*;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.osgi.util.NLS;

/**
 * Abstract JMS server channel.
 */
public abstract class AbstractJMSServerChannel extends AbstractJMSChannel implements ISynchAsynchConnection {
	private static final long serialVersionUID = -4762123821387039176L;

	private static final int RESPOND_TO_REQUEST_ERROR_CODE = 33001;

	public AbstractJMSServerChannel(ISynchAsynchEventHandler handler, int keepAlive) throws ECFException {
		super(handler, keepAlive);
		if (localContainerID instanceof JMSID) {
			setupJMS((JMSID) localContainerID, null);
		} else
			throw new ECFException(Messages.AbstractJMSServerChannel_CONNECT_EXCEPTION_CONTAINER_NOT_JMSID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.jms.channel.AbstractJMSChannel#connect(org.eclipse.ecf.core.identity.ID,
	 *      java.lang.Object, int)
	 */
	public Object connect(ID remote, Object data, int timeout) throws ECFException {
		throw new ECFException(Messages.AbstractJMSServerChannel_CONNECT_EXCEPTION_CONTAINER_SERVER_CANNOT_CONNECT);
	}

	public class Client implements ISynchAsynchConnection {

		public static final int DEFAULT_PING_WAITTIME = 3000;

		private final Map properties;
		private final ID clientID;
		boolean isStarted = false;
		private final Object disconnectLock = new Object();
		boolean disconnectHandled = false;

		private Thread pingThread = null;
		private final int pingWaitTime = DEFAULT_PING_WAITTIME;

		private long lastSendTime = -1;

		protected long getLastSendTime() {
			return lastSendTime;
		}

		protected void setLastSendTime() {
			lastSendTime = System.currentTimeMillis();
		}

		public Client(ID clientID) {
			this.clientID = clientID;
			this.properties = new HashMap();
		}

		public void sendAsynch(ID receiver, byte[] data) throws IOException {
			setLastSendTime();
			AbstractJMSServerChannel.this.sendAsynch(receiver, data);
		}

		public void addListener(IConnectionListener listener) {
			// XXX not implemented
		}

		public Object connect(ID remote, Object data, int timeout) throws ECFException {
			throw new ECFException(Messages.AbstractJMSServerChannel_CONNECT_EXCEPTION_CONTAINER_SERVER_CANNOT_CONNECT);
		}

		public void disconnect() {
			synchronized (synch) {
				stop();
			}
		}

		public ID getLocalID() {
			return clientID;
		}

		public Map getProperties() {
			return properties;
		}

		public boolean isConnected() {
			return true;
		}

		public boolean isStarted() {
			return isStarted;
		}

		public void removeListener(IConnectionListener listener) {
			// XXX not implemented
		}

		public void start() {
			if (!isStarted) {
				isStarted = true;
				pingThread = setupPing();
				pingThread.setDaemon(true);
				pingThread.start();
			}
		}

		public void stop() {
			if (isStarted) {
				isStarted = false;
				if (pingThread != null) {
					pingThread.interrupt();
					pingThread = null;
				}
			}
		}

		public Object getAdapter(Class adapter) {
			return null;
		}

		public Object sendSynch(ID receiver, byte[] data) throws IOException {
			return AbstractJMSServerChannel.this.sendSynch(receiver, data);
		}

		private Thread setupPing() {
			final int pingStartWait = (new Random()).nextInt(keepAlive / 2);
			return new Thread(new Runnable() {
				public void run() {
					final Thread me = Thread.currentThread();
					// Sleep a random interval to start
					try {
						Thread.sleep(pingStartWait);
					} catch (final InterruptedException e) {
						return;
					}
					// Setup ping frequency as keepAlive /2
					final int frequency = keepAlive / 2;
					while (isStarted) {
						try {
							// We give up if thread interrupted or disconnect
							// has
							// occurred
							if (me.isInterrupted() || disconnectHandled)
								break;
							// Sleep for timeout interval divided by two
							Thread.sleep(frequency);
							// We give up if thread interrupted or disconnect
							// has
							// occurred
							if (me.isInterrupted() || disconnectHandled)
								break;
							final long lastSendTime1 = getLastSendTime();
							// Only send ping if the current time is greater
							// than the lastSendTime + keepAlive/2
							if (System.currentTimeMillis() > (lastSendTime1 + frequency)) {
								setLastSendTime();
								sendAndWait(new Ping(AbstractJMSServerChannel.this.getLocalID(), Client.this.getLocalID()), pingWaitTime);
							}
						} catch (final Exception e) {
							handleException(e);
							break;
						}
					}
					handleException(null);
				}
			}, getLocalID() + ":ping:" + AbstractJMSServerChannel.this.getLocalID()); //$NON-NLS-1$
		}

		public void handleDisconnect() {
			synchronized (disconnectLock) {
				if (!disconnectHandled) {
					disconnectHandled = true;
					handler.handleDisconnectEvent(new DisconnectEvent(Client.this, null, null));
				}
			}
			synchronized (Client.this) {
				Client.this.notifyAll();
			}
		}

		void handleException(Throwable e) {
			synchronized (disconnectLock) {
				if (!disconnectHandled) {
					disconnectHandled = true;
					if (e != null)
						handler.handleDisconnectEvent(new DisconnectEvent(Client.this, e, null));
				}
			}
			synchronized (Client.this) {
				Client.this.notifyAll();
			}
		}

		public void sendConnectResponse(String jmsCorrelationID, ID targetID, ID senderID, Serializable[] messages) throws JMSException {
			final ObjectMessage first = createObjectMessage(new ConnectResponseMessage(getConnectionID(), targetID, senderID, (messages == null) ? null : messages[0]));
			first.setJMSCorrelationID(jmsCorrelationID);
			jmsTopic.getProducer().send(first);
			final ObjectMessage second = createObjectMessage(new JMSMessage(getConnectionID(), getLocalID(), null, (messages == null) ? null : messages[1]));
			jmsTopic.getProducer().send(second);
		}
	}

	public Client createClient(ID remoteID) {
		Client newclient = new Client(remoteID);
		newclient.start();
		return newclient;
	}

	protected void handleSynchRequest(ObjectMessage omsg, ECFMessage o) {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "respondToRequest", new Object[] {omsg, o}); //$NON-NLS-1$
		try {
			final Serializable[] resp = (Serializable[]) handler.handleSynchEvent(new SynchEvent(this, new Object[] {omsg, o}));
			// this resp is an Serializable[] with two messages, one for the
			// connect response and the other for everyone else
			if (o instanceof ConnectRequestMessage) {
				final ObjectMessage first = createObjectMessage(new ConnectResponseMessage(getConnectionID(), o.getTargetID(), o.getSenderID(), (resp == null) ? null : resp[0]));
				first.setJMSCorrelationID(omsg.getJMSCorrelationID());
				jmsTopic.getProducer().send(first);
				final ObjectMessage second = createObjectMessage(new JMSMessage(getConnectionID(), getLocalID(), null, (resp == null) ? null : resp[1]));
				jmsTopic.getProducer().send(second);
			} else if (o instanceof DisconnectRequestMessage) {
				final ObjectMessage msg = createObjectMessage(new DisconnectResponseMessage(getConnectionID(), o.getTargetID(), o.getSenderID(), null));
				msg.setJMSCorrelationID(omsg.getJMSCorrelationID());
				jmsTopic.getProducer().send(msg);
			}
		} catch (final Exception e) {
			traceAndLogExceptionCatch(RESPOND_TO_REQUEST_ERROR_CODE, "respondToRequest", e); //$NON-NLS-1$
		}
		Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "respondToRequest"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.jms.channel.AbstractJMSChannel#sendSynch(org.eclipse.ecf.core.identity.ID,
	 *      byte[])
	 */
	public Object sendSynch(ID target, byte[] data) throws IOException {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "sendSynch", new Object[] {target, data}); //$NON-NLS-1$
		Object result = null;
		if (isActive()) {
			result = sendAndWait(new DisconnectRequestMessage(getConnectionID(), getLocalID(), target, data), keepAlive);
		} else
			Trace.trace(Activator.PLUGIN_ID, NLS.bind("sendSynch: channel not active...ignoring sendSynch", target, data)); //$NON-NLS-1$
		Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "sendSynch", result); //$NON-NLS-1$
		return result;
	}
}
