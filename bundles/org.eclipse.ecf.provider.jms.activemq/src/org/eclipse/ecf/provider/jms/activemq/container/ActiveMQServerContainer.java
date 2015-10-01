/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.activemq.container;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.jms.container.AbstractJMSContainerInstantiator;
import org.eclipse.ecf.provider.jms.container.AbstractJMSServer;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
import org.eclipse.ecf.provider.jms.identity.JMSID;

public class ActiveMQServerContainer extends AbstractJMSServer {

	public static final String CONFIG_NAME = "ecf.jms.activemq.tcp.manager";

	public static final String PASSWORD_PROPERTY = "password";
	public static final String USERNAME_PROPERTY = "username";
	public static final String DEFAULT_PASSWORD = "password";
	public static final String DEFAULT_USERNAME = "guest";

	public static final String DEFAULT_SERVER_ID = "tcp://localhost:61616/exampleTopic";

	public static class Instantiator extends AbstractJMSContainerInstantiator {

		public static final String BROKER_USERNAME_PARAM = "brokerUsername";
		public static final String BROKER_PASSWORD_PARAM = "brokerPassword";

		public Instantiator() {
			super(ActiveMQServerContainer.CONFIG_NAME, ActiveMQClientContainer.CONFIG_NAME);
		}

		@Override
		public IContainer createInstance(ContainerTypeDescription description, Object[] args)
				throws ContainerCreateException {
			try {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) getMap(args);
				if (map == null) {
					JMSID serverID = (args != null && args.length > 0) ? getJMSIDFromParameter(args[0])
							: getJMSIDFromParameter(ActiveMQServerContainer.DEFAULT_SERVER_ID);
					Integer ka = (args != null && args.length > 1) ? getIntegerFromArg(args[1])
							: new Integer(ActiveMQServerContainer.DEFAULT_KEEPALIVE);
					map = new HashMap<String, Object>();
					map.put(ID_PARAM, serverID);
					map.put(KEEPALIVE_PARAM, ka);
				}
				return createInstance(description, map);
			} catch (Exception e) {
				if (e instanceof ContainerCreateException)
					throw (ContainerCreateException) e;
				return throwCreateException("Exception creating ActiveMQServerContainer", e);
			}

		}

		@Override
		public IContainer createInstance(ContainerTypeDescription description, Map<String, ?> parameters)
				throws ContainerCreateException {
			JMSID id = getJMSIDFromParams(parameters, ActiveMQServerContainer.DEFAULT_SERVER_ID);
			Integer ka = getKeepAlive(parameters, ActiveMQServerContainer.DEFAULT_KEEPALIVE);
			Map<String, String> props = new HashMap<String, String>();
			props.put(ActiveMQServerContainer.USERNAME_PROPERTY,
					getParameterValue(parameters, BROKER_USERNAME_PARAM, ActiveMQServerContainer.DEFAULT_USERNAME));
			props.put(ActiveMQServerContainer.PASSWORD_PROPERTY,
					getParameterValue(parameters, BROKER_PASSWORD_PARAM, ActiveMQServerContainer.DEFAULT_PASSWORD));
			ActiveMQServerContainer server = new ActiveMQServerContainer(new JMSContainerConfig(id, ka, props));
			try {
				server.start();
			} catch (ECFException e) {
				throw new ContainerCreateException("Could not start ActiveMQServerContainer", e);
			}
			return server;
		}

	}

	public ActiveMQServerContainer(JMSContainerConfig config) {
		super(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.jms.container.AbstractJMSServer#start()
	 */
	public void start() throws ECFException {
		JMSContainerConfig config = getJMSContainerConfig();
		final ISynchAsynchConnection connection = new ActiveMQServerChannel(getReceiver(), config.getKeepAlive(),
				config.getPropertyString(USERNAME_PROPERTY, DEFAULT_USERNAME),
				config.getPropertyString(PASSWORD_PROPERTY, DEFAULT_PASSWORD));
		setConnection(connection);
		connection.start();
	}

	public void dispose() {
		super.dispose();
		ISynchAsynchConnection connection = getConnection();
		if (connection != null)
			connection.disconnect();
	}

}