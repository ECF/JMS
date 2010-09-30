/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.internal.provider.jms.activemq.broker;

import java.net.URI;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IIDFactory;
import org.eclipse.ecf.core.provider.BaseContainerInstantiator;
import org.eclipse.ecf.provider.jms.activemq.broker.ActiveMQJMSBrokerContainer;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.ecf.provider.jms.identity.JMSNamespace;

public class ActiveMQJMSBrokerContainerInstantiator extends
		BaseContainerInstantiator {

	public IContainer createInstance(ContainerTypeDescription description,
			Object[] parameters) throws ContainerCreateException {
		try {
			BrokerService broker = createBrokerService(parameters);
			IIDFactory idFactory = IDFactory.getDefault();
			JMSID jmsid = (JMSID) idFactory.createID(JMSNamespace.NAME,idFactory.createGUID().getName());
			return new ActiveMQJMSBrokerContainer(jmsid, broker);
		} catch (Exception e) {
			throw new ContainerCreateException("Could not create broker container", e);
		}
	}
	
	
	private BrokerService createBrokerService(Object[] parameters) throws Exception {
		if (parameters == null || parameters.length < 1) throw new IllegalArgumentException("at least one parameter must be specified");
		if (parameters[0] instanceof URI) {
			return BrokerFactory.createBroker((URI) parameters[0], false); 
		} else if (parameters[0] instanceof String) {
			BrokerService result = new BrokerService();
			result.addConnector((String) parameters[0]);
			return result;
		}
		BrokerService result = new BrokerService();
		result.addConnector("tcp://localhost:61616");
		return result;
	}
}
