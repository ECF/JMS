/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.jms.qpid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;
import org.eclipse.ecf.provider.jms.qpid.container.QpidJMSClientContainer;
import org.eclipse.ecf.provider.jms.qpid.container.QpidJMSServerContainer;

public class QpidJMSClientContainerInstantiator extends
		GenericContainerInstantiator {

	protected static final String[] jmsIntents = { "JMS" };
	protected static final String JMS_CLIENT_NAME = "ecf.jms.qpid.tcp.client";

	public QpidJMSClientContainerInstantiator() {

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
					QpidJMSServerContainer.DEFAULT_KEEPALIVE);
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
					return new QpidJMSClientContainer(
							QpidJMSServerContainer.DEFAULT_KEEPALIVE);
				else
					return new QpidJMSClientContainer(ka.intValue());
			} else {
				if (ka == null)
					ka = new Integer(
							QpidJMSServerContainer.DEFAULT_KEEPALIVE);
				return new QpidJMSClientContainer(name, ka.intValue());
			}
		} catch (Exception e) {
			throw new ContainerCreateException(
					"Exception creating activemq client container", e);
		}
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		List results = new ArrayList();
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
		List results = new ArrayList();
		List supportedConfigs = Arrays.asList(exporterSupportedConfigs);
		if (JMS_CLIENT_NAME.equals(description.getName())) {
			if (supportedConfigs
					.contains(QpidJMSServerContainerInstantiator.JMS_MANAGER_NAME)
					|| supportedConfigs.contains(JMS_CLIENT_NAME)) {
				results.add(JMS_CLIENT_NAME);
				results
						.add(QpidJMSServerContainerInstantiator.JMS_MANAGER_NAME);
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