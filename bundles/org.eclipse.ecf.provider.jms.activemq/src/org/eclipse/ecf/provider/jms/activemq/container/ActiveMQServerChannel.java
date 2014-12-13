/**
 * 
 */
package org.eclipse.ecf.provider.jms.activemq.container;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.ISynchAsynchEventHandler;
import org.eclipse.ecf.provider.jms.channel.AbstractJMSServerChannel;
import org.eclipse.ecf.provider.jms.identity.JMSID;

public class ActiveMQServerChannel extends AbstractJMSServerChannel {

	private static final long serialVersionUID = -2348383004973299553L;

	private final String username;
	private final String password;

	public ActiveMQServerChannel(ISynchAsynchEventHandler handler,
			int keepAlive, String username, String pw) throws ECFException {
		super(handler, keepAlive);
		this.username = username;
		this.password = pw;
		setupJMS((JMSID) localContainerID, null);
	}

	protected Object readObject(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ObjectInputStream oos = new ObjectInputStream(new ByteArrayInputStream(
				bytes));
		return oos.readObject();
	}

	protected ConnectionFactory createJMSConnectionFactory(JMSID targetID)
			throws IOException {
		return new ActiveMQConnectionFactory(username, password,
				targetID.getName());
	}

}