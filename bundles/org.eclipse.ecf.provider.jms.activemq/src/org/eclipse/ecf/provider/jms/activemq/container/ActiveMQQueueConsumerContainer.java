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

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.jms.container.AbstractJMSContainerInstantiator;
import org.eclipse.ecf.provider.jms.container.AbstractJMSQueueConsumerContainer;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.ecf.remoteservice.IRemoteServiceCallPolicy;

public class ActiveMQQueueConsumerContainer extends AbstractJMSQueueConsumerContainer {

	public static final String CONFIG_NAME = "ecf.jms.activemq.tcp.lb.server";

	public static class Instantiator extends AbstractJMSContainerInstantiator {

		public static final String CONTAINER_ID_PARAM = "containerId";

		public Instantiator() {
			super(ActiveMQQueueConsumerContainer.CONFIG_NAME, ActiveMQLBQueueProducerContainer.CONFIG_NAME);
		}

		@Override
		public IContainer createInstance(ContainerTypeDescription description, Object[] args)
				throws ContainerCreateException {
			try {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) getMap(args);
				if (map == null) {
					if (args != null && args.length > 1) {
						map = new HashMap<String, Object>();
						map.put(ActiveMQLBQueueProducerContainer.Instantiator.QUEUE_PARAM,
								getJMSIDFromParameter(args[0]));
						map.put(CONTAINER_ID_PARAM, getJMSIDFromParameter(args[1]));
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
			JMSID queueID = getJMSIDFromParams(parameters, ActiveMQLBQueueProducerContainer.Instantiator.QUEUE_PARAM);
			JMSID containerID = getJMSIDFromParams(parameters, CONTAINER_ID_PARAM);
			Map<String, String> props = new HashMap<String, String>();
			props.put(ActiveMQServerContainer.USERNAME_PROPERTY,
					getParameterValue(parameters, ActiveMQServerContainer.Instantiator.BROKER_USERNAME_PARAM,
							ActiveMQServerContainer.DEFAULT_USERNAME));
			props.put(ActiveMQServerContainer.PASSWORD_PROPERTY,
					getParameterValue(parameters, ActiveMQServerContainer.Instantiator.BROKER_PASSWORD_PARAM,
							ActiveMQServerContainer.DEFAULT_PASSWORD));

			ActiveMQQueueConsumerContainer server = new ActiveMQQueueConsumerContainer(containerID, queueID, props);

			try {
				server.start();
			} catch (ECFException e) {
				throw new ContainerCreateException("Could not start ActiveMQLBQueueProducerContainer", e);
			}
			return server;
		}

	}

	@SuppressWarnings("rawtypes")
	private Map properties;

	public ActiveMQQueueConsumerContainer(JMSID containerID, JMSID queueID) {
		this(containerID, queueID, null);
	}

	private String getPropertyValue(String propName, String defaultValue) {
		if (properties == null)
			return defaultValue;
		String o = (String) properties.get(propName);
		return (o == null) ? defaultValue : o;
	}

	public ActiveMQQueueConsumerContainer(JMSID containerID, JMSID queueID, @SuppressWarnings("rawtypes") Map props) {
		super(containerID, queueID);
		this.properties = props;
	}

	public ConnectionFactory getQueueConnectionFactory(String target, Object[] jmsConfiguration) {
		return new ActiveMQConnectionFactory(
				getPropertyValue(ActiveMQServerContainer.USERNAME_PROPERTY, ActiveMQServerContainer.DEFAULT_USERNAME),
				getPropertyValue(ActiveMQServerContainer.PASSWORD_PROPERTY, ActiveMQServerContainer.DEFAULT_PASSWORD),
				target);
	}

	public void start() throws ECFException {
		try {
			setupJMSQueueConsumer(getQueueID());
		} catch (JMSException e) {
			ECFException t = new ECFException("Could not connect to queueID=" + queueID, e); //$NON-NLS-1$
			t.setStackTrace(e.getStackTrace());
			throw t;
		}
	}

	public boolean setRemoteServiceCallPolicy(IRemoteServiceCallPolicy policy) {
		return false;
	}

}
