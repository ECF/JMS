package org.eclipse.ecf.provider.jms.net4j;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase;

public class Net4jJMSContainerTest extends JMSContainerAbstractTestCase {

	protected String getClientContainerName() {
		return Net4j.CLIENT_CONTAINER_NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase#getServerContainerName()
	 */
	protected String getServerContainerName() {
		return Net4j.SERVER_CONTAINER_NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase#getServerIdentity()
	 */
	protected String getServerIdentity() {
		return Net4j.TARGET_NAME;
	}

	protected IContainer createServer() throws Exception {
		return ContainerFactory.getDefault().createContainer(
				getServerContainerName(), new Object[] { getServerIdentity() });
	}

	public void testConnectClient() throws Exception {
		IContainer client = getClients()[0];
		ID targetID = IDFactory.getDefault().createID(client.getConnectNamespace(),new Object [] { getServerIdentity() });
		client.connect(targetID, null);
		Thread.sleep(3000);
	}

}
