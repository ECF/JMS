/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.activemq.container;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.jms.container.AbstractLBQueueProducerContainer;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
import org.eclipse.ecf.provider.jms.identity.JMSID;

public class ActiveMQJMSQueueProducerContainer extends
		AbstractLBQueueProducerContainer {

	public ActiveMQJMSQueueProducerContainer(JMSContainerConfig config,
			JMSID queueID) {
		super(config, queueID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.jms.container.AbstractJMSServer#start()
	 */
	public void start() throws ECFException {
		JMSContainerConfig config = getJMSContainerConfig();
		final ISynchAsynchConnection connection = new ActiveMQServerChannel(
				this.getReceiver(), config.getKeepAlive(),
				config.getPropertyString(
						ActiveMQJMSServerContainer.USERNAME_PROPERTY,
						ActiveMQJMSServerContainer.DEFAULT_USERNAME),
				config.getPropertyString(
						ActiveMQJMSServerContainer.PASSWORD_PROPERTY,
						ActiveMQJMSServerContainer.DEFAULT_PASSWORD));
		setConnection(connection);
		try {
			setupJMSQueueProducer(getQueueID());
		} catch (JMSException e) {
			ECFException t = new ECFException("Cannot setup queueID="
					+ getQueueID()
					+ " for ActiveMQJMSQueueProducerContainer id=" + getID(), e);
			t.setStackTrace(t.getStackTrace());
			throw t;
		}
		connection.start();
	}

	public void dispose() {
		super.dispose();
		getConnection().disconnect();
		setConnection(null);
	}

	public ConnectionFactory getQueueConnectionFactory(String target,
			Object[] jmsConfiguration) {
		JMSContainerConfig config = getJMSContainerConfig();
		return new ActiveMQConnectionFactory(config.getPropertyString(
				ActiveMQJMSServerContainer.USERNAME_PROPERTY,
				ActiveMQJMSServerContainer.DEFAULT_USERNAME),
				config.getPropertyString(
						ActiveMQJMSServerContainer.PASSWORD_PROPERTY,
						ActiveMQJMSServerContainer.DEFAULT_PASSWORD), target);
	}

}