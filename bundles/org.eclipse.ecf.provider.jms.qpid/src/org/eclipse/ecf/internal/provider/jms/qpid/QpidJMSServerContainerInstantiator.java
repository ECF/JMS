/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
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
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.ecf.provider.jms.identity.JMSNamespace;
import org.eclipse.ecf.provider.jms.qpid.container.QpidJMSServerContainer;

public class QpidJMSServerContainerInstantiator extends
		GenericContainerInstantiator {

	protected static final String[] jmsIntents = { "JMS" };

	protected static final String JMS_MANAGER_NAME = "ecf.jms.qpid.tcp.manager";
	
	public QpidJMSServerContainerInstantiator() {

	}

	private JMSID getJMSIDFromParameter(Object p) {
		if (p instanceof String) {
			return (JMSID) IDFactory.getDefault().createID(
					JMSNamespace.NAME, (String) p);
		} else if (p instanceof JMSID) { 
			return (JMSID) p;
		} else return (JMSID) IDFactory.getDefault().createID(
				JMSNamespace.NAME, QpidJMSServerContainer.DEFAULT_SERVER_ID);
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
			Integer ka = null;
			JMSID serverID = (args == null || args.length < 1)?getJMSIDFromParameter((String) QpidJMSServerContainer.DEFAULT_SERVER_ID):getJMSIDFromParameter(args[0]);
			if (args != null && args.length > 1)
				ka = getIntegerFromArg(args[1]);
			if (ka == null)
				ka = new Integer(QpidJMSServerContainer.DEFAULT_KEEPALIVE);
			QpidJMSServerContainer server = new QpidJMSServerContainer(
					new JMSContainerConfig(serverID, ka.intValue(), null));
			server.start();
			return server;
		} catch (Exception e) {
			throw new ContainerCreateException(
					"Exception creating activemq server container", e);
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

	public String[] getImportedConfigs(ContainerTypeDescription description, String[] exporterSupportedConfigs) {
		List results = new ArrayList();
		List supportedConfigs = Arrays.asList(exporterSupportedConfigs);
		// For a manager, if a client is exporter then we are an importer
		if (JMS_MANAGER_NAME.equals(description.getName())) {
			if (supportedConfigs.contains(QpidJMSClientContainerInstantiator.JMS_CLIENT_NAME))
				results.add(JMS_MANAGER_NAME);
		}
		if (results.size() == 0)
			return null;
		return (String[]) results.toArray(new String[] {});
	}

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return new String[] { JMS_MANAGER_NAME };
	}


}