/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.activemq.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.jms.container.AbstractJMSContainerInstantiator;
import org.eclipse.ecf.provider.jms.container.AbstractLBQueueProducerContainer;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
import org.eclipse.ecf.provider.jms.identity.JMSID;

public class ActiveMQLBQueueProducerContainer extends AbstractLBQueueProducerContainer {

	public static final String CONFIG_NAME = "ecf.jms.activemq.tcp.manager.lb.svchost";

	public static class Instantiator extends AbstractJMSContainerInstantiator {

		private static Map<String, List<String>> exporterImporterMap = new HashMap<String, List<String>>();

		public static final String BROKER_USERNAME_PARAM = "queueProducerBrokerUsername";
		public static final String BROKER_PASSWORD_PARAM = "queueProducerBrokerPassword";

		static {
			List<String> importers = new ArrayList<String>();
			importers.add(ActiveMQClientContainer.CONFIG_NAME);
			exporterImporterMap.put(ActiveMQLBQueueProducerContainer.CONFIG_NAME, importers);
		}

		public Instantiator() {
			super(Arrays.asList(new String[] { ActiveMQLBQueueProducerContainer.CONFIG_NAME }), exporterImporterMap);
		}

		public static final String TOPIC_PARAM = "topic";
		public static final String QUEUE_PARAM = "queue";

		@Override
		public IContainer createInstance(ContainerTypeDescription description, Object[] args)
				throws ContainerCreateException {
			try {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) getMap(args);
				if (map == null) {
					if (args != null && args.length > 1) {
						map = new HashMap<String, Object>();
						map.put(TOPIC_PARAM, getJMSIDFromParameter(args[0]));
						map.put(QUEUE_PARAM, getJMSIDFromParameter(args[1]));
						if (args.length > 2)
							map.put(KEEPALIVE_PARAM, getIntegerFromArg(args[2]));
					} else
						throw new ContainerCreateException(
								"Cannot create queue producer container. Required parameters: <String topic> <String queue> [Integer keepAlive]");
				}
				return createInstance(description, map);
			} catch (Exception e) {
				if (e instanceof ContainerCreateException)
					throw (ContainerCreateException) e;
				return throwCreateException("Exception creating ActiveMQLBQueueProducerContainer", e);
			}
		}

		@Override
		public IContainer createInstance(ContainerTypeDescription description, Map<String, ?> parameters)
				throws ContainerCreateException {
			JMSID topicID = getJMSIDFromParams(parameters, TOPIC_PARAM);
			JMSID queueID = getJMSIDFromParams(parameters, QUEUE_PARAM);
			Integer ka = getKeepAlive(parameters, ActiveMQServerContainer.DEFAULT_KEEPALIVE);
			Map<String, String> props = new HashMap<String, String>();
			props.put(ActiveMQServerContainer.USERNAME_PROPERTY,
					getParameterValue(parameters, BROKER_USERNAME_PARAM, ActiveMQServerContainer.DEFAULT_USERNAME));
			props.put(ActiveMQServerContainer.PASSWORD_PROPERTY,
					getParameterValue(parameters, BROKER_PASSWORD_PARAM, ActiveMQServerContainer.DEFAULT_PASSWORD));

			ActiveMQLBQueueProducerContainer server = new ActiveMQLBQueueProducerContainer(
					new JMSContainerConfig(topicID, ka.intValue(), props), queueID);
			try {
				server.start();
			} catch (ECFException e) {
				throw new ContainerCreateException("Could not start ActiveMQLBQueueProducerContainer", e);
			}
			return server;
		}

	}

	public ActiveMQLBQueueProducerContainer(JMSContainerConfig config, JMSID queueID) {
		super(config, queueID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.jms.container.AbstractJMSServer#start()
	 */
	public void start() throws ECFException {
		JMSContainerConfig config = getJMSContainerConfig();
		final ISynchAsynchConnection connection = new ActiveMQServerChannel(this.getReceiver(), config.getKeepAlive(),
				config.getPropertyString(ActiveMQServerContainer.USERNAME_PROPERTY,
						ActiveMQServerContainer.DEFAULT_USERNAME),
				config.getPropertyString(ActiveMQServerContainer.PASSWORD_PROPERTY,
						ActiveMQServerContainer.DEFAULT_PASSWORD));
		setConnection(connection);
		try {
			setupJMSQueueProducer(getQueueID());
		} catch (JMSException e) {
			ECFException t = new ECFException(
					"Cannot setup queueID=" + getQueueID() + " for ActiveMQLBQueueProducerContainer id=" + getID(), e);
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

	public ConnectionFactory getQueueConnectionFactory(String target, Object[] jmsConfiguration) {
		JMSContainerConfig config = getJMSContainerConfig();
		return new ActiveMQConnectionFactory(
				config.getPropertyString(ActiveMQServerContainer.USERNAME_PROPERTY,
						ActiveMQServerContainer.DEFAULT_USERNAME),
				config.getPropertyString(ActiveMQServerContainer.PASSWORD_PROPERTY,
						ActiveMQServerContainer.DEFAULT_PASSWORD),
				target);
	}

}