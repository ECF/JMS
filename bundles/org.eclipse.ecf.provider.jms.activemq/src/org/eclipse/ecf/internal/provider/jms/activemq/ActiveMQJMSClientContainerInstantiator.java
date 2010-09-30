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

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSClientContainer;
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSServerContainer;

public class ActiveMQJMSClientContainerInstantiator extends
		GenericContainerInstantiator {

	protected static final String[] jmsIntents = { "JMS" };
	protected static final String JMS_CLIENT_NAME = "ecf.jms.activemq.tcp.client";

	public ActiveMQJMSClientContainerInstantiator() {

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
			String name = null;
			if (args != null) {
				if (args.length > 0) {
					name = (String) args[0];
					if (args.length > 1) {
						ka = getIntegerFromArg(args[1]);
					}
				}
			}
			if (name == null) {
				if (ka == null)
					return new ActiveMQJMSClientContainer(
							ActiveMQJMSServerContainer.DEFAULT_KEEPALIVE);
				else
					return new ActiveMQJMSClientContainer(ka.intValue());
			} else {
				if (ka == null)
					ka = new Integer(
							ActiveMQJMSServerContainer.DEFAULT_KEEPALIVE);
				return new ActiveMQJMSClientContainer(name, ka.intValue());
			}
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