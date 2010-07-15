package org.eclipse.ecf.provider.jms.net4j.container;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.provider.jms.net4j.Net4jServerChannel;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.jms.container.AbstractJMSServer;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;

public class Net4jServerContainer extends AbstractJMSServer {

	public Net4jServerContainer(JMSContainerConfig config) {
		super(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.jms.container.AbstractJMSServer#start()
	 */
	public void start() throws ECFException {
		ISynchAsynchConnection connection = new Net4jServerChannel(this
				.getReceiver(), ((JMSContainerConfig) getConfig())
				.getKeepAlive());
		setConnection(connection);
		connection.start();
	}

	public void dispose() {
		super.dispose();
		getConnection().disconnect();
		setConnection(null);
	}

}
