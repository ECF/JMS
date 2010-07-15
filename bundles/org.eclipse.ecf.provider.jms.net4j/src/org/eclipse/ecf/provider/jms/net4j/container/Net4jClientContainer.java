package org.eclipse.ecf.provider.jms.net4j.container;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.internal.provider.jms.net4j.Net4jClientChannel;
import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.jms.container.AbstractJMSClient;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;

public class Net4jClientContainer extends AbstractJMSClient {

	public Net4jClientContainer(JMSContainerConfig config) {
		super(config);
	}

	protected ISynchAsynchConnection createConnection(ID remoteSpace,
			Object data) throws ConnectionCreateException {
		return new Net4jClientChannel(this.getReceiver(),((JMSContainerConfig) getConfig()).getKeepAlive());
	}

}
