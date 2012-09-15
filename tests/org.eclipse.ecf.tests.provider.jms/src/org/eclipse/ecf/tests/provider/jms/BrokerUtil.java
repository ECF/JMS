package org.eclipse.ecf.tests.provider.jms;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;

public class BrokerUtil {

	private IContainerManager containerManager;
	private IContainer brokerContainer;
	
	public BrokerUtil(IContainerManager containerManager) throws ContainerCreateException, ContainerConnectException {
		this.containerManager = containerManager;
		brokerContainer = containerManager.getContainerFactory().createContainer("ecf.jms.activemq.broker");
		brokerContainer.connect(null,null);
	}
	
	public void dispose() {
		if (this.brokerContainer != null) {
			brokerContainer.disconnect();
			brokerContainer.dispose();
			containerManager.removeContainer(brokerContainer);
			brokerContainer = null;
		}
	}
}
