/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.provider.jms.activemq.remoteservice;


import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.osgi.services.distribution.AbstractServiceRegisterListenerTest;
import org.eclipse.ecf.tests.provider.jms.activemq.ActiveMQ;


public class ActiveMQServiceRegisterListenerTest extends AbstractServiceRegisterListenerTest {

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		setClientCount(1);
		createServerAndClients();
		connectClients();
		setupRemoteServiceAdapters();
	}

	protected String getClientContainerName() {
		return ActiveMQ.CLIENT_CONTAINER_NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.jms.remoteservice.AbstractRemoteServiceTestCase#getServerContainerName()
	 */
	protected String getServerContainerName() {
		return ActiveMQ.SERVER_CONTAINER_NAME;
	}

	protected ID getServerConnectID(int client) {
		return IDFactory.getDefault().createID("ecf.namespace.jmsid", ActiveMQ.TARGET_NAME);
	}
	
	protected IContainer createServer() throws Exception {
		return ContainerFactory.getDefault().createContainer(getServerContainerName(), new Object[] {ActiveMQ.TARGET_NAME});
	}

	protected void tearDown() throws Exception {
		cleanUpServerAndClients();
		super.tearDown();
	}

}
