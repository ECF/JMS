package org.eclipse.ecf.tests.provider.jms;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

public abstract class JMSContainerAbstractTestCase extends ContainerAbstractTestCase {

	protected abstract String getClientContainerName();

	protected abstract String getServerIdentity();

	protected abstract String getServerContainerName();

	protected String getJMSNamespace() {
		return "ecf.namespace.jmsid";
	}

	protected void setUp() throws Exception {
		setClientCount(1);
		createServerAndClients();
		super.setUp();
	}

	protected void tearDown() throws Exception {
		cleanUpServerAndClients();
		super.tearDown();
	}

	public void testConnectClient() throws Exception {
		final IContainer client = getClients()[0];
		final ID targetID = IDFactory.getDefault().createID(client.getConnectNamespace(), new Object[] {getServerIdentity()});
		client.connect(targetID, null);
		Thread.sleep(3000);
	}
}
