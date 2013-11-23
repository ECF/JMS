/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.channel;

import java.io.*;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.jms.Activator;
import org.eclipse.ecf.internal.provider.jms.JmsDebugOptions;
import org.eclipse.ecf.provider.comm.*;
import org.eclipse.ecf.provider.jms.identity.JMSID;

public abstract class AbstractJMSClientChannel extends AbstractJMSChannel implements ISynchAsynchConnection {
	private static final long serialVersionUID = -1381571376210849678L;

	private static final int RESPOND_TO_REQUEST_ERROR_CODE = 32001;
	private static final int DEFAULT_DISCONNECT_WAIT_TIME = 3000;

	private final int disconnectWaitTime = DEFAULT_DISCONNECT_WAIT_TIME;

	public AbstractJMSClientChannel(ISynchAsynchEventHandler handler, int keepAlive) {
		super(handler, keepAlive);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.comm.IConnection#connect(org.eclipse.ecf.core.identity.ID,
	 *      java.lang.Object, int)
	 */
	public Object connect(ID target, Object data, int timeout) throws ECFException {
		synchronized (waitResponse) {
			Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "connect", new Object[] {target, data, //$NON-NLS-1$
					new Integer(timeout)});
			if (isConnected())
				throw new ContainerConnectException("Already connected"); //$NON-NLS-1$
			if (target == null)
				throw new ContainerConnectException("target must not be null"); //$NON-NLS-1$
			if (!(target instanceof JMSID))
				throw new ContainerConnectException("target must be of type JMSID"); //$NON-NLS-1$
			if (!(data instanceof Serializable))
				throw new ContainerConnectException("data for connect to target=" + target + " is not serializable", new NotSerializableException()); //$NON-NLS-1$//$NON-NLS-2$

			Serializable result = null;
			try {
				final Serializable connectData = setupJMS((JMSID) target, data);
				Trace.trace(Activator.PLUGIN_ID, "connecting to " + target + "," //$NON-NLS-1$ //$NON-NLS-2$
						+ data + "," + timeout + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				result = sendAndWait(new ConnectRequestMessage(getConnectionID(), getLocalID(), (JMSID) target, connectData));
			} catch (final ECFException e) {
				final ECFException except = e;
				throw new ContainerConnectException(except.getStatus());
			} catch (final Exception e) {
				throw new ContainerConnectException("connect to target=" + target.getName() + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (result == null)
				throw new ContainerConnectException("connect to target=" + target.getName() + " refused"); //$NON-NLS-1$ //$NON-NLS-2$
			if (!(result instanceof ConnectResponseMessage))
				throw new ContainerConnectException("invalid response for connect to target=" + target.getName()); //$NON-NLS-1$
			final Object resultData = ((ConnectResponseMessage) result).getData();
			fireListenersConnect(new ConnectionEvent(this, resultData));
			Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "connect", resultData); //$NON-NLS-1$
			return resultData;
		}
	}

	protected void handleSynchRequest(String jmsCorrelationID, ECFMessage o) {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "respondToRequest", new Object[] {jmsCorrelationID, o}); //$NON-NLS-1$
		final boolean active = isActive();
		try {
			if (o instanceof DisconnectRequestMessage) {
				createAndSendMessage(new DisconnectResponseMessage(getConnectionID(), o.getTargetID(), o.getSenderID(), null), jmsCorrelationID);
				if (active)
					handler.handleSynchEvent(new SynchEvent(this, o.getData()));
			} else if (o instanceof Ping && active)
				createAndSendMessage(new PingResponse(o.getTargetID(), o.getSenderID()), jmsCorrelationID);
		} catch (final Exception e) {
			traceAndLogExceptionCatch(RESPOND_TO_REQUEST_ERROR_CODE, "respondToRequest", e); //$NON-NLS-1$			
		}
		Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "respondToRequest"); //$NON-NLS-1$
	}

	public Object sendSynch(ID target, byte[] data) throws IOException {
		Trace.entering(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_ENTERING, this.getClass(), "sendSynch", new Object[] {target, data}); //$NON-NLS-1$
		Object result = null;
		boolean active = true;
		synchronized (waitResponse) {
			active = isActive();
			if (active)
				isStopping = true;
		}
		if (active)
			result = sendAndWait(new DisconnectRequestMessage(getConnectionID(), getLocalID(), target, data), disconnectWaitTime);
		Trace.exiting(Activator.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING, this.getClass(), "sendSynch", result); //$NON-NLS-1$
		return result;
	}
}
