package org.eclipse.ecf.tests.provider.jms;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

public abstract class JMSContainerAbstractTestCase extends
		ContainerAbstractTestCase {

	public static final boolean useBroker = new Boolean(System.getProperty(
			"org.eclipse.ecf.provider.jms.useBroker", "true")).booleanValue();

	protected abstract String getClientContainerName();

	protected abstract String getServerIdentity();

	protected abstract String getServerContainerName();

	protected String getJMSNamespace() {
		return "ecf.namespace.jmsid";
	}

	protected void setUp() throws Exception {
		setupBroker();
		setClientCount(1);
		createServerAndClients();
		super.setUp();
	}

	protected void setupBroker() throws Exception {
		if (useBroker) 
			broker = new BrokerUtil(getContainerManager());
	}

	private BrokerUtil broker;

	protected void tearDownBroker() throws Exception {
		if (broker != null) {
			broker.dispose();
			broker = null;
		}
	}

	protected void tearDown() throws Exception {
		cleanUpServerAndClients();
		tearDownBroker();
		super.tearDown();
	}

	public void testConnectClient() throws Exception {
		final IContainer client = getClients()[0];
		final ID targetID = IDFactory.getDefault().createID(
				client.getConnectNamespace(),
				new Object[] { getServerIdentity() });
		client.connect(targetID, null);
		Thread.sleep(3000);
	}
}
