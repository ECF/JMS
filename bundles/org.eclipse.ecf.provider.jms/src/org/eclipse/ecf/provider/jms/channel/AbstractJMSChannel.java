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
import java.net.ConnectException;
import java.net.SocketAddress;
import java.util.*;
import javax.jms.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.jms.Activator;
import org.eclipse.ecf.internal.provider.jms.JmsDebugOptions;
import org.eclipse.ecf.provider.comm.*;
import org.eclipse.ecf.provider.jms.identity.JMSID;

/**
 * Abstract JMSChannel implementation. This class is superclass to
 * AbstractJMSServerChannel and AbstractJMSClient channel.
 */
public abstract class AbstractJMSChannel extends SocketAddress implements ISynchAsynchConnection {

	private static final long serialVersionUID = 4516462369458730752L;

	private static long correlationID = 0;
	protected Connection connection = null;
	protected Session session = null;
	protected JmsTopic jmsTopic = null;
	protected ID localContainerID;
	protected boolean connected = false;
	private boolean started = false;
	protected ISynchAsynchEventHandler handler;
	protected int keepAlive = -1;
	private Map properties = new HashMap();
	protected List connectionListeners = new ArrayList();
	protected boolean isStopping = false;

	public AbstractJMSChannel(ISynchAsynchEventHandler hand, int keepAlive, Map properties) {
		this.handler = hand;
		Assert.isNotNull(this.handler);
		this.localContainerID = hand.getEventHandlerID();
		Assert.isNotNull(localContainerID);
		this.keepAlive = keepAlive;
		if (properties != null)
			this.properties = properties;
	}

	public AbstractJMSChannel(ISynchAsynchEventHandler hand, int keepAlive) {
		this(hand, keepAlive, null);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.comm.IConnection#connect(org.eclipse.ecf.core.identity.ID,
	 *      java.lang.Object, int)
	 */
	public abstract Object connect(ID remote, Object data, int timeout) throws ECFException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.comm.ISynchConnection#sendSynch(org.eclipse.ecf.core.identity.ID,
	 *      byte[])
	 */
	public abstract Object sendSynch(ID target, byte[] data) throws IOException;

	/**
	 * Create a JMS ConnectionFactory instance for a given targetID with given
	 * data. Implementers of this method must return a non-<code>null</code>
	 * ConnectionFactory instance or throw an IOException. They cannot return
	 * <code>null</code>.
	 * 
	 * @param targetID
	 *            the JMSID for the target host.
	 * @return ConnectionFactory instance. Must not be <code>null</code>.
	 * @throws IOException
	 *             if the connection factory cannot be made for the given
	 *             target.
	 */
	protected abstract ConnectionFactory createJMSConnectionFactory(JMSID targetID) throws IOException;

	/**
	 * Handle synchronous request messages
	 * (ConnectRequestMessage/DisconnectRequestMessage)
	 * 
	 * @param omsg
	 *            ObjectMessage that is the source of the request
	 * @param o
	 *            the ECFMessage that is the request message
	 */
	protected abstract void handleSynchRequest(ObjectMessage omsg, ECFMessage o);

	protected void fireListenersConnect(ConnectionEvent event) {
		List toNotify = null;
		synchronized (connectionListeners) {
			toNotify = new ArrayList(connectionListeners);
		}
		for (Iterator i = toNotify.iterator(); i.hasNext();) {
			IConnectionListener l = (IConnectionListener) i.next();
			l.handleConnectEvent(event);
		}
	}

	protected void fireListenersDisconnect(ConnectionEvent event) {
		List toNotify = null;
		synchronized (connectionListeners) {
			toNotify = new ArrayList(connectionListeners);
		}
		for (Iterator i = toNotify.iterator(); i.hasNext();) {
			IConnectionListener l = (IConnectionListener) i.next();
			l.handleConnectEvent(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.comm.IConnection#getLocalID()
	 */
	public ID getLocalID() {
		return localContainerID;
	}

	private static long getNextCorrelationID() {
		return correlationID++;
	}

	protected boolean isActive() {
		return isConnected() && isStarted() && !isStopping();
	}

	protected void onJMSException(JMSException except) {
		if (isActive())
			handler.handleDisconnectEvent(new DisconnectEvent(this, except, null));
	}

	protected boolean isStopping() {
		return isStopping;
	}

	protected Serializable createConnectRequestData(Object data) {
		if (data instanceof Serializable)
			return (Serializable) data;
		return null;
	}

	protected Serializable setupJMS(JMSID targetID, Object data) throws ECFException {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "setupJMS"); //$NON-NLS-1$
		try {
			ConnectionFactory factory = createJMSConnectionFactory(targetID);
			connection = factory.createConnection();
			connection.setClientID(getLocalID().getName());
			connection.setExceptionListener(new ExceptionListener() {
				public void onException(JMSException arg0) {
					onJMSException(arg0);
				}
			});
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			jmsTopic = new JmsTopic(session, targetID.getTopicOrQueueName());
			jmsTopic.getConsumer().setMessageListener(new TopicReceiver());
			connected = true;
			isStopping = false;
			connection.start();
			Serializable connectData = createConnectRequestData(data);
			Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "setup", connectData); //$NON-NLS-1$
			return connectData;
		} catch (Exception e) {
			disconnect();
			Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.EXCEPTIONS_THROWING, this.getClass(), "setupJMS"); //$NON-NLS-1$
			throw new ECFException("JMS Connect or Setup Exception", e); //$NON-NLS-1$
		}
	}

	public void sendAsynch(ID recipient, Object obj) throws IOException {
		sendAsync(recipient, (Serializable) obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.comm.IAsynchConnection#sendAsynch(org.eclipse.ecf.core.identity.ID,
	 *      byte[])
	 */
	public void sendAsynch(ID recipient, byte[] obj) throws IOException {
		sendAsync(recipient, obj);
	}

	private void sendAsync(ID recipient, Serializable obj) throws IOException {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "sendAsynch", new Object[] {recipient, obj}); //$NON-NLS-1$
		if (!isActive())
			throw new ConnectException("Not connected"); //$NON-NLS-1$
		try {
			jmsTopic.getProducer().send(createObjectMessage(new JMSMessage(getConnectionID(), getLocalID(), recipient, obj)));
		} catch (JMSException e) {
			throwIOException("sendAsynch", "Exception in sendAsynch", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "sendAsynch"); //$NON-NLS-1$
	}

	protected void onTopicException(JMSException except) {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "onTopicException", new Object[] {except}); //$NON-NLS-1$
		if (isActive())
			handler.handleDisconnectEvent(new DisconnectEvent(this, except, null));
		Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "onTopicException"); //$NON-NLS-1$
	}

	protected void throwIOException(String method, String msg, Throwable t) throws IOException {
		Trace.throwing(Activator.PLUGIN_ID, JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), method, t);
		throw new IOException(msg + ": " + t.getMessage()); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.comm.IConnection#isConnected()
	 */
	public boolean isConnected() {
		return connected;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.comm.IConnection#isStarted()
	 */
	public boolean isStarted() {
		return started;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.comm.IConnection#getProperties()
	 */
	public Map getProperties() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.comm.IConnection#addCommEventListener(org.eclipse.ecf.core.comm.IConnectionListener)
	 */
	public void addListener(IConnectionListener listener) {
		connectionListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.comm.IConnection#removeCommEventListener(org.eclipse.ecf.core.comm.IConnectionListener)
	 */
	public void removeListener(IConnectionListener listener) {
		connectionListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.comm.IConnection#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class clazz) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.comm.IConnection#disconnect()
	 */
	public void disconnect() {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "disconnect"); //$NON-NLS-1$
		synchronized (synch) {
			stop();
			connected = false;
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "connection close", e)); //$NON-NLS-1$
				}
				connection = null;
			}
			synch.notifyAll();
		}
		fireListenersDisconnect(new ConnectionEvent(this, null));
		connectionListeners.clear();
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "disconnect"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.comm.IConnection#stop()
	 */
	public void stop() {
		started = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.comm.IConnection#start()
	 */
	public void start() {
		started = true;
	}

	protected void handleTopicMessage(Message msg, JMSMessage jmsmsg) {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "handleTopicMessage", new Object[] {msg, //$NON-NLS-1$
				jmsmsg});
		if (isActive()) {
			try {
				handler.handleAsynchEvent(new AsynchEvent(this, jmsmsg.getData()));
			} catch (IOException e) {
				Trace.catching(Activator.PLUGIN_ID, JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "handleTopicMessage", e); //$NON-NLS-1$
				Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Exception on handleTopicMessage", e)); //$NON-NLS-1$
			}
		} else
			Trace.trace(Activator.PLUGIN_ID, "handleTopicMessage: channel not active...ignoring message " + msg + " with JMSMessage " + jmsmsg); //$NON-NLS-1$ //$NON-NLS-2$
		Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "handleTopicMessage"); //$NON-NLS-1$
	}

	protected Object synch = new Object();

	private String correlation = null;

	private Serializable reply = null;

	private boolean waitDone;

	protected Serializable sendAndWait(Serializable obj) throws IOException {
		return sendAndWait(obj, keepAlive);
	}

	protected ObjectMessage createObjectMessage(Serializable obj) throws JMSException {
		return session.createObjectMessage(obj);
	}

	protected Serializable sendAndWait(Serializable obj, int waitDuration) throws IOException {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "sendAndWait", new Object[] {obj, //$NON-NLS-1$
				new Integer(waitDuration)});
		synchronized (synch) {
			try {
				ObjectMessage msg = createObjectMessage(obj);
				correlation = String.valueOf(getNextCorrelationID());
				msg.setJMSCorrelationID(correlation);
				waitDone = false;
				long waittimeout = System.currentTimeMillis() + waitDuration;
				reply = null;
				jmsTopic.getProducer().send(msg);
				while (!waitDone && (waittimeout - System.currentTimeMillis() > 0)) {
					synch.wait(waitDuration / 10);
				}
				waitDone = true;
				if (reply == null)
					throw new IOException("timeout waiting for response"); //$NON-NLS-1$
			} catch (JMSException e) {
				Trace.catching(Activator.PLUGIN_ID, JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), "sendAndWait", e); //$NON-NLS-1$
				throwIOException("sendAndWait", "JMSException in sendAndWait", //$NON-NLS-1$ //$NON-NLS-2$
						e);
			} catch (InterruptedException e) {
				traceAndLogExceptionCatch(IStatus.ERROR, "handleTopicMessage", e); //$NON-NLS-1$
			}
			Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "sendAndWait", reply); //$NON-NLS-1$
			return reply;
		}
	}

	protected void handleSynchResponse(ObjectMessage msg, ECFMessage ecfmsg) {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "handleSynchMessage", new Object[] {msg, //$NON-NLS-1$
				ecfmsg});
		synchronized (synch) {
			if (correlation == null)
				return;
			try {
				if (correlation.equals(msg.getJMSCorrelationID())) {
					reply = msg.getObject();
					waitDone = true;
					correlation = null;
					synch.notify();
				}
			} catch (JMSException e) {
				traceAndLogExceptionCatch(IStatus.ERROR, "handleSynchMessage", e); //$NON-NLS-1$
			}
		}
		Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "handleSynchMessage"); //$NON-NLS-1$
	}

	protected void traceAndLogExceptionCatch(int code, String method, Throwable e) {
		Trace.catching(Activator.PLUGIN_ID, JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), method, e);
		Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, method, e));
	}

	protected String getConnectionID() {
		String res = null;
		try {
			res = connection.getClientID();
			if (res == null)
				res = getLocalID().getName();
			return res;
		} catch (Exception e) {
			traceAndLogExceptionCatch(IStatus.ERROR, "getConnectionID", e); //$NON-NLS-1$
			return null;
		}
	}

	protected final class TopicReceiver implements MessageListener {

		public TopicReceiver() {
			super();
		}

		public void onMessage(Message msg) {
			try {
				// All messages should be ObjectMessages
				if (msg instanceof ObjectMessage) {
					ObjectMessage omg = (ObjectMessage) msg;
					Object o = omg.getObject();
					// All messages should also be ECFMessages
					if (o instanceof ECFMessage) {
						ECFMessage ecfmsg = (ECFMessage) o;
						ID fromID = ecfmsg.getSenderID();
						if (fromID == null) {
							Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "onMessage: fromID=null...ignoring ECFMessage " + ecfmsg); //$NON-NLS-1$
							return;
						}
						if (fromID.equals(getLocalID())) {
							Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "onMessage:  fromID=localID...ignoring ECFMessage " + ecfmsg); //$NON-NLS-1$
							return;
						}
						// Get targetID...it's either null, and the message is intended for everyone, or it's 
						// non-null and it equals our ID and is meant for us.  Anything else and it's not meant for us
						ID targetID = ecfmsg.getTargetID();
						if (targetID == null) {
							if (ecfmsg instanceof JMSMessage)
								handleTopicMessage(msg, (JMSMessage) ecfmsg);
							else
								Trace.trace(Activator.PLUGIN_ID, "onMessage.received invalid message to group"); //$NON-NLS-1$
						} else {
							if (targetID.equals(getLocalID())) {
								if (ecfmsg instanceof JMSMessage)
									handleTopicMessage(msg, (JMSMessage) ecfmsg);
								else if (ecfmsg instanceof SynchRequestMessage)
									handleSynchRequest(omg, ecfmsg);
								else if (ecfmsg instanceof SynchResponseMessage)
									handleSynchResponse(omg, ecfmsg);
								else
									Trace.trace(Activator.PLUGIN_ID, "onMessage.msg invalid message to " + targetID); //$NON-NLS-1$
							} else
								Trace.trace(Activator.PLUGIN_ID, "onMessage.msg ECFMessage " + ecfmsg + " not intended for " + targetID); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else
						// received bogus message...ignore
						Trace.trace(Activator.PLUGIN_ID, "onMessage: received non-ECFMessage...ignoring " + o); //$NON-NLS-1$
				} else
					Trace.trace(Activator.PLUGIN_ID, "onMessage: received non-object message...ignoring " + msg); //$NON-NLS-1$
			} catch (Exception e) {
				traceAndLogExceptionCatch(IStatus.ERROR, "onMessage: Unexpected Exception", e); //$NON-NLS-1$
			}
		}
	}
}
