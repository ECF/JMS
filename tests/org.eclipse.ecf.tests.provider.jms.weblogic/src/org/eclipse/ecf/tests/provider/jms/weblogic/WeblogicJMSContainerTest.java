package org.eclipse.ecf.tests.provider.jms.weblogic;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase;

public class WeblogicJMSContainerTest extends JMSContainerAbstractTestCase {

	protected String getClientContainerName() {
		return Weblogic.CLIENT_CONTAINER_NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase#getServerContainerName()
	 */
	protected String getServerContainerName() {
		return Weblogic.SERVER_CONTAINER_NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase#getServerIdentity()
	 */
	protected String getServerIdentity() {
		return Weblogic.TARGET_NAME;
	}

	public void testConnectClient() throws Exception {
		final IContainer client = getClients()[0];
		final ID targetID = IDFactory.getDefault().createID(client.getConnectNamespace(), new Object[] {getServerIdentity()});
		client.connect(targetID, null);
		Thread.sleep(3000);
	}

}
