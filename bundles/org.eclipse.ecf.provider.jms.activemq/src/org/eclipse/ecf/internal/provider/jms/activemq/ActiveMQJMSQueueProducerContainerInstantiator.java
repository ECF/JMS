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
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException {
		try {
			Integer ka = new Integer(
					ActiveMQJMSServerContainer.DEFAULT_KEEPALIVE);
			String topic = null;
			String queue = null;
			if (args.length < 0)
				throw new ContainerCreateException(
						"Incorrect arguments provided for construction.  Required: <String jmsTopicID> <String jmsQueueID> [keepAlive]");
			topic = (String) args[0];
			queue = (String) args[1];
			JMSID topicID = (JMSID) IDFactory.getDefault().createID(
					JMSNamespace.NAME, topic);
			JMSID queueID = (JMSID) IDFactory.getDefault().createID(
					JMSNamespace.NAME, queue);
			if (args.length > 2)
				ka = getIntegerFromArg(args[1]);
			if (ka == null)
				ka = new Integer(ActiveMQJMSServerContainer.DEFAULT_KEEPALIVE);
			ActiveMQJMSQueueProducerContainer server = new ActiveMQJMSQueueProducerContainer(
					new JMSContainerConfig(topicID, ka.intValue(), null),
					queueID);
			server.start();
			return server;
		} catch (Exception e) {
			throw new ContainerCreateException(
					"Exception creating activemq server container", e);
		}
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		List<String> results = new ArrayList<String>();
		for (int i = 0; i < genericProviderIntents.length; i++) {
			results.add(genericProviderIntents[i]);
		}
		for (int i = 0; i < jmsIntents.length; i++) {
			results.add(jmsIntents[i]);
		}
		return (String[]) results.toArray(new String[] {});
	}

	public String[] getImportedConfigs(ContainerTypeDescription description, String[] exporterSupportedConfigs) {
		List<String> results = new ArrayList<String>();
		List<String> supportedConfigs = Arrays.asList(exporterSupportedConfigs);
		// For a manager, if a client is exporter then we are an importer
		if (JMS_LBMANAGER_NAME.equals(description.getName())) {
			if (supportedConfigs.contains(ActiveMQJMSClientContainerInstantiator.JMS_CLIENT_NAME))
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