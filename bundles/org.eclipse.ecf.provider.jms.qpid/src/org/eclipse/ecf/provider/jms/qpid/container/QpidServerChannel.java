/**
 * 
 */
package org.eclipse.ecf.provider.jms.qpid.container;

import java.io.IOException;

import javax.jms.ConnectionFactory;

import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.url.URLSyntaxException;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.comm.ISynchAsynchEventHandler;
import org.eclipse.ecf.provider.jms.channel.AbstractJMSServerChannel;
import org.eclipse.ecf.provider.jms.identity.JMSID;

class QpidServerChannel extends AbstractJMSServerChannel {

	private static final long serialVersionUID = -2348383004973299553L;

	private final String username;
	private final String password;

	public QpidServerChannel(ISynchAsynchEventHandler handler, int keepAlive,
			String username, String pw) throws ECFException {
		super(handler, keepAlive);
		this.username = username;
		this.password = pw;
	}

	protected ConnectionFactory createJMSConnectionFactory(JMSID targetID)
			throws IOException {
		try {
			return new AMQConnectionFactory(targetID.getBroker(),
					(username == null) ? "defaultUsername" : username,
					(password == null) ? "defaultPassword" : password, targetID
							.getName(), "/" + targetID.getTopicOrQueueName());
		} catch (URLSyntaxException e) {
			throw new IOException(
					"createJMSConnectionFactory URL syntax exception: "
							+ e.getMessage());
		}
	}

}