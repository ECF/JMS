/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.jms.weblogic;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.ConnectionEvent;
import org.eclipse.ecf.provider.comm.ISynchAsynchEventHandler;
import org.eclipse.ecf.provider.jms.channel.AbstractJMSClientChannel;
import org.eclipse.ecf.provider.jms.channel.JmsTopic;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.ecf.provider.jms.weblogic.container.WeblogicJMSServerContainer;

public class WeblogicJMSClientChannel extends AbstractJMSClientChannel {

	private static final long serialVersionUID = 3688761380066499761L;

	public WeblogicJMSClientChannel(ISynchAsynchEventHandler handler, int keepAlive) throws ECFException {
		super(handler, keepAlive);
	}

	private InitialContext getInitialContext(String jmsProviderURL) throws NamingException {
		final Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, WeblogicJMSServerContainer.JNDI_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, jmsProviderURL);
		return new InitialContext(env);
	}

	protected Serializable setupJMS(JMSID targetID, Object data) throws ECFException {
		try {
			final InitialContext ctx = getInitialContext(targetID.getBroker());
			final Destination topicDestination = (Destination) ctx.lookup(targetID.getTopicOrQueueName());
			final ConnectionFactory factory = (ConnectionFactory) ctx.lookup(WeblogicJMSServerContainer.JMS_CONNECTION_FACTORY);

			connection = factory.createConnection();
			connection.setClientID(getLocalID().getName());
			connection.setExceptionListener(new ExceptionListener() {
				public void onException(JMSException arg0) {
					onJMSException(arg0);
				}
			});
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			jmsTopic = new JmsTopic(session, topicDestination);
			jmsTopic.getConsumer().setMessageListener(new TopicReceiver());
			synchronized (this) {
				connected = true;
				isStopping = false;
				connection.start();
			}
			final Serializable connectData = createConnectRequestData(data);
			return connectData;
		} catch (final Exception e) {
			disconnect();
			throw new ECFException("Client JMS connect failure for " + targetID.getName(), e); //$NON-NLS-1$
		}
	}

	protected ConnectionFactory createJMSConnectionFactory(JMSID targetID) throws IOException {
		// not used due to override of setupJMS above
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.comm.IConnection#disconnect()
	 */
	public void disconnect() {
		synchronized (synch) {
			stop();
			connected = false;
			notifyAll();
		}
		fireListenersDisconnect(new ConnectionEvent(this, null));
		connectionListeners.clear();
	}
}
