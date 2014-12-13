/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
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
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSClientContainer;
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSServerContainer;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;

public class ActiveMQJMSClientContainerInstantiator extends
		GenericContainerInstantiator {

	protected static final String[] jmsIntents = { "JMS" };
	protected static final String JMS_CLIENT_NAME = "ecf.jms.activemq.tcp.client";

	public static final String ID_PARAM = "id";
	public static final String KEEPALIVE_PARAM = "keepAlive";

	@SuppressWarnings("rawtypes")
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException {
		try {
			Integer ka = null;
			String id = null;
			Map props = null;
			if (args == null) {
				ka = new Integer(ActiveMQJMSServerContainer.DEFAULT_KEEPALIVE);
				return new ActiveMQJMSClientContainer(ka.intValue());
			} else if (args[0] instanceof Map) {
				props = (Map) args[0];
				id = (String) props.get(ID_PARAM);
				Object kao = props.get(KEEPALIVE_PARAM);
				if (kao != null)
					ka = getIntegerFromArg(kao);
			} else {
				id = (String) args[0];
				if (args.length > 1)
					ka = getIntegerFromArg(args[1]);
			}
			if (ka == null)
				ka = new Integer(ActiveMQJMSServerContainer.DEFAULT_KEEPALIVE);
			if (id == null)
				id = IDFactory.getDefault().createGUID().getName();
			return new ActiveMQJMSClientContainer(new JMSContainerConfig(id,
					ka.intValue(), props));
		} catch (Exception e) {
			throw new ContainerCreateException(
					"Exception creating activemq client container", e);
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

	public String[] getImportedConfigs(ContainerTypeDescription description,
			String[] exporterSupportedConfigs) {
		List<String> results = new ArrayList<String>();
		List<String> supportedConfigs = Arrays.asList(exporterSupportedConfigs);
		if (JMS_CLIENT_NAME.equals(description.getName())) {
			if (
			// If it's a normal manager
			supportedConfigs
					.contains(ActiveMQJMSServerContainerInstantiator.JMS_MANAGER_NAME)
					// Or the service exporter is a client
					|| supportedConfigs.contains(JMS_CLIENT_NAME)
					// Or it's a load balancing service host
					|| supportedConfigs
							.contains(ActiveMQJMSQueueProducerContainerInstantiator.JMS_LBMANAGER_NAME)) {
				results.add(JMS_CLIENT_NAME);
			}
		}
		if (results.size() == 0)
			return null;
		return (String[]) results.toArray(new String[] {});
	}

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return new String[] { JMS_CLIENT_NAME };
	}

}