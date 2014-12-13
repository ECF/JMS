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
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSQueueConsumerContainer;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.ecf.provider.jms.identity.JMSNamespace;

public class ActiveMQJMSQueueConsumerContainerInstantiator extends
		GenericContainerInstantiator {

	protected static final String[] jmsIntents = { "JMS" };
	public static final String QUEUE_PROPERTY = "queue";
	public static final String CONTAINER_ID_PROPERTY = "containerId";

	public ActiveMQJMSQueueConsumerContainerInstantiator() {

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
			String queue = null;
			Map props = null;
			String containerId = null;
			if (args.length == 0)
				throw new ContainerCreateException(
						"No QueueID provided for creation of ActiveMQJMSQueueConsumerContainer");
			if (args[0] instanceof Map) {
				props = (Map) args[0];
				queue = (String) props.get(QUEUE_PROPERTY);
				containerId = (String) props.get(CONTAINER_ID_PROPERTY);
			} else {
				queue = (String) args[0];
				if (args.length > 1)
					containerId = (String) args[1];
			}
			if (containerId == null)
				containerId = IDFactory.getDefault().createGUID().getName();
			ActiveMQJMSQueueConsumerContainer server = new ActiveMQJMSQueueConsumerContainer(
					(JMSID) IDFactory.getDefault().createID(JMSNamespace.NAME,
							(String) containerId), (JMSID) IDFactory
							.getDefault().createID(JMSNamespace.NAME, queue),
					props);
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

}