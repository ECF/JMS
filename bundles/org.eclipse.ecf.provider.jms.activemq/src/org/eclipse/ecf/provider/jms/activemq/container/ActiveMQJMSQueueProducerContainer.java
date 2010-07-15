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

	public static final String PASSWORD_PROPERTY = "password";
	public static final String USERNAME_PROPERTY = "username";
	public static final String DEFAULT_PASSWORD = "defaultPassword";
	public static final String DEFAULT_USERNAME = "defaultUsername";

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
		JMSContainerConfig config = (JMSContainerConfig) getConfig();
		final String username = (String) config.getProperties().get(
				ActiveMQJMSQueueProducerContainer.USERNAME_PROPERTY);
		final String password = (String) config.getProperties().get(
				ActiveMQJMSQueueProducerContainer.PASSWORD_PROPERTY);
		final ISynchAsynchConnection connection = new ActiveMQServerChannel(
				this.getReceiver(), config.getKeepAlive(), username, password);
		setConnection(connection);
		try {
			setupJMSQueueProducer(getQueueID());
		} catch (JMSException e) {
			throw new ECFException("Cannot setup queueID=" + getQueueID(), e);
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
		String username = null;
		String password = null;
		if (jmsConfiguration != null && jmsConfiguration.length > 0) {
			username = (String) jmsConfiguration[0];
			if (jmsConfiguration.length > 1) {
				password = (String) jmsConfiguration[1];
			}
		}
		return new ActiveMQConnectionFactory(
				(username == null) ? ActiveMQConnectionFactory.DEFAULT_USER
						: username,
				(password == null) ? ActiveMQConnectionFactory.DEFAULT_PASSWORD
						: password, target);
	}

}