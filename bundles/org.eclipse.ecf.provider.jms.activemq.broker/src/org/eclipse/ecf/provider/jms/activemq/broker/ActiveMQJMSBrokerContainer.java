/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.provider.jms.activemq.broker;

import org.apache.activemq.broker.BrokerService;
import org.eclipse.ecf.core.AbstractContainer;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.ContainerConnectedEvent;
import org.eclipse.ecf.core.events.ContainerConnectingEvent;
import org.eclipse.ecf.core.events.ContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.ContainerDisconnectingEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.ecf.provider.jms.identity.JMSNamespace;

public class ActiveMQJMSBrokerContainer extends AbstractContainer {

	public static final String NAME = "ecf.jms.activemq.broker";
	
	private JMSID id;
	protected BrokerService brokerService;
	
	public ActiveMQJMSBrokerContainer(JMSID jmsid, BrokerService broker) throws Exception {
		this.id = jmsid;
		this.brokerService = broker;
		configureBroker();
	}
	
	protected void configureBroker() throws Exception {
		// by default do nothing, subclasses may override
	}
	
	public void connect(ID targetID, IConnectContext connectContext)
			throws ContainerConnectException {
		ID localID = getID();
		fireContainerEvent(new ContainerConnectingEvent(localID,localID));
		try {
			brokerService.start();
			brokerService.waitUntilStarted();
			fireContainerEvent(new ContainerConnectedEvent(localID,localID));
		} catch (Exception e) {
			throw new ContainerConnectException("Could not start broker service", e);
		}
	}

	public ID getConnectedID() {
		return getID();
	}

	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(JMSNamespace.NAME);
	}

	public void disconnect() {
		ID localID = getID();
		fireContainerEvent(new ContainerDisconnectingEvent(localID,localID));
		try {
			if (brokerService.isStarted()) {
				brokerService.stop();
				brokerService.waitUntilStopped();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireContainerEvent(new ContainerDisconnectedEvent(localID,localID));
	}

	public ID getID() {
		return id;
	}

}
