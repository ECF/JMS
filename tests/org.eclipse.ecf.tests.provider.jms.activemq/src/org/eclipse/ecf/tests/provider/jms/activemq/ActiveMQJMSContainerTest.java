package org.eclipse.ecf.tests.provider.jms.activemq;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase;

public class ActiveMQJMSContainerTest extends JMSContainerAbstractTestCase {

	public static final String JMSBROKER_USERNAME = System.getProperty("org.eclipse.ecf.tests.provider.jms.activemq.jmsbroker.username");
	public static final String JMSBROKER_PASSWORD = System.getProperty("org.eclipse.ecf.tests.provider.jms.activemq.jmsbroker.password");
	
	protected String getClientContainerName() {
		return ActiveMQ.CLIENT_CONTAINER_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase#
	 * getServerContainerName()
	 */
	protected String getServerContainerName() {
		return ActiveMQ.SERVER_CONTAINER_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase#
	 * getServerIdentity()
	 */
	protected String getServerIdentity() {
		return ActiveMQ.TARGET_NAME;
	}

	protected IContainer createServer() throws Exception {
		Map props = new HashMap();
		props.put("id", getServerIdentity());
		if (JMSBROKER_USERNAME != null) {
			props.put("username",JMSBROKER_USERNAME);
			props.put("password",JMSBROKER_PASSWORD);
		}
		return ContainerFactory.getDefault().createContainer(
				getServerContainerName(), props );
	}

	@Override
	protected IContainer createClient(int index) throws Exception {
		Map props = new HashMap();
		if (JMSBROKER_USERNAME != null) {
			props.put("username",JMSBROKER_USERNAME);
			props.put("password",JMSBROKER_PASSWORD);
		}
		return ContainerFactory.getDefault().createContainer(
				getClientContainerName(), props );
	}
	
	public void testConnectClient() throws Exception {
		IContainer client = getClients()[0];
		ID targetID = IDFactory.getDefault().createID(
				client.getConnectNamespace(),
				new Object[] { getServerIdentity() });
		Thread.sleep(3000);
		client.connect(targetID, null);
		Thread.sleep(3000);
	}

}
