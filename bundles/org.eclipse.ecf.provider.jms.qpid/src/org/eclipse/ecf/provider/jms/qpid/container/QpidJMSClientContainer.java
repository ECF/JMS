/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.qpid.container;

import java.io.IOException;

import javax.jms.ConnectionFactory;

import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.url.URLSyntaxException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.jms.channel.AbstractJMSClientChannel;
import org.eclipse.ecf.provider.jms.container.AbstractJMSClient;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
import org.eclipse.ecf.provider.jms.identity.JMSID;

public class QpidJMSClientContainer extends AbstractJMSClient {

	private int keepAlive;

	private int getKeepAlive() {
		return keepAlive;
	}

	class ActiveMQClientChannel extends AbstractJMSClientChannel {

		private static final long serialVersionUID = -5581778054975360068L;

		public ActiveMQClientChannel() {
			super(getReceiver(), getKeepAlive());
		}

		protected ConnectionFactory createJMSConnectionFactory(JMSID targetID)
				throws IOException {
			try {
				return new AMQConnectionFactory(targetID.getBroker(),
						getUsername(targetID), getPassword(targetID), getID()
								.getName(), "/" + targetID.getTopicOrQueueName());
			} catch (URLSyntaxException e) {
				throw new IOException(
						"createJMSConnectionFactory URL syntax exception: "
								+ e.getMessage());
			}
		}

		private String getPassword(JMSID targetID) {
			String pw = (String) getConfig().getProperties().get(
					QpidJMSServerContainer.PASSWORD_PROPERTY);
			return (pw == null) ? QpidJMSServerContainer.DEFAULT_PASSWORD : pw;
		}

		private String getUsername(JMSID targetID) {
			String username = (String) getConfig().getProperties().get(
					QpidJMSServerContainer.USERNAME_PROPERTY);
			return (username == null) ? QpidJMSServerContainer.DEFAULT_USERNAME
					: username;
		}

	}

	/**
	 * @param keepAlive
	 * @throws Exception
	 */
	public QpidJMSClientContainer(int keepAlive) throws Exception {
		super();
		this.keepAlive = keepAlive;
	}

	public QpidJMSClientContainer(String name, int keepAlive) throws Exception {
		super(new JMSContainerConfig(name, keepAlive));
		this.keepAlive = keepAlive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.provider.generic.ClientSOContainer#createConnection(org
	 * .eclipse.ecf.core.identity.ID, java.lang.Object)
	 */
	protected ISynchAsynchConnection createConnection(ID remoteSpace,
			Object data) throws ConnectionCreateException {
		return new ActiveMQClientChannel();
	}
}