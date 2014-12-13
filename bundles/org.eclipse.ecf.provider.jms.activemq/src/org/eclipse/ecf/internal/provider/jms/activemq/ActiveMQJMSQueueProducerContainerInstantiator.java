/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.jms.activemq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSQueueProducerContainer;
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSServerContainer;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.ecf.provider.jms.identity.JMSNamespace;

public class ActiveMQJMSQueueProducerContainerInstantiator extends
		GenericContainerInstantiator {

	public static final String TOPIC_PARAM = "topic";
	public static final String QUEUE_PARAM = "queue";
	public static final String KEEPALIVE_PARAM = "keepAlive";

	protected static final String[] jmsIntents = { "JMS" };

	protected static final String JMS_LBMANAGER_NAME = "ecf.jms.activemq.tcp.manager.lb.svchost";

	public ActiveMQJMSQueueProducerContainerInstantiator() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org
	 * .eclipse.ecf.core.ContainerTypeDescription, java.lang.Object[])
	 */
	@SuppressWarnings("rawtypes")
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException {
		try {
			Integer ka = new Integer(
					ActiveMQJMSServerContainer.DEFAULT_KEEPALIVE);
			String topic = null;
			String queue = null;
			Map props = null;
			if (args.length < 0)
				throw new ContainerCreateException(
						"Incorrect arguments provided for createContainer.  Required: <String jmsTopicID> <String jmsQueueID> [int keepAlive]");
			if (args[0] instanceof Map) {
				props = (Map) args[0];
				topic = (String) props.get(TOPIC_PARAM);
				queue = (String) props.get(QUEUE_PARAM);
				ka = (Integer) props.get(KEEPALIVE_PARAM);
			} else {
				topic = (String) args[0];
				queue = (String) args[1];
				if (args.length > 2)
					ka = getIntegerFromArg(args[1]);
			}
			ActiveMQJMSQueueProducerContainer server = new ActiveMQJMSQueueProducerContainer(
					new JMSContainerConfig((JMSID) IDFactory.getDefault()
							.createID(JMSNamespace.NAME, topic), ka.intValue(),
							props), (JMSID) IDFactory.getDefault().createID(
							JMSNamespace.NAME, queue));
			server.start();
			return server;
		} catch (Exception e) {
			ContainerCreateException t = new ContainerCreateException(
					"Exception creating activemq server container", e);
			t.setStackTrace(e.getStackTrace());
			throw t;
		}
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		List<String> results = new ArrayList<String>();
		for (int i = 0; i < genericProviderIntents.length; i++)
			results.add(genericProviderIntents[i]);
		for (int i = 0; i < jmsIntents.length; i++)
			results.add(jmsIntents[i]);
		return (String[]) results.toArray(new String[] {});
	}

	public String[] getImportedConfigs(ContainerTypeDescription description,
			String[] exporterSupportedConfigs) {
		List<String> results = new ArrayList<String>();
		List<String> supportedConfigs = Arrays.asList(exporterSupportedConfigs);
		// For a manager, if a client is exporter then we are an importer
		if (JMS_LBMANAGER_NAME.equals(description.getName())) {
			if (supportedConfigs
					.contains(ActiveMQJMSClientContainerInstantiator.JMS_CLIENT_NAME))
				results.add(JMS_LBMANAGER_NAME);
		}
		if (results.size() == 0)
			return null;
		return (String[]) results.toArray(new String[] {});
	}

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return new String[] { JMS_LBMANAGER_NAME };
	}

}