/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.activemq.container;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.jms.channel.AbstractJMSClientChannel;
import org.eclipse.ecf.provider.jms.container.AbstractJMSClient;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
import org.eclipse.ecf.provider.jms.identity.JMSID;

public class ActiveMQJMSClientContainer extends AbstractJMSClient {

	private int keepAlive;

	/**
	 * @param keepAlive
	 * @throws Exception
	 */
	public ActiveMQJMSClientContainer(int keepAlive) throws Exception {
		super();
		this.keepAlive = keepAlive;
	}

	public ActiveMQJMSClientContainer(String name, int keepAlive)
			throws Exception {
		super(new JMSContainerConfig(name, keepAlive));
		this.keepAlive = keepAlive;
	}

	public ActiveMQJMSClientContainer(JMSContainerConfig config) {
		super(config);
		this.keepAlive = config.getKeepAlive();
	}

	class ActiveMQClientChannel extends AbstractJMSClientChannel {

		private static final long serialVersionUID = -5581778054975360068L;

		public ActiveMQClientChannel() {
			super(getReceiver(), ActiveMQJMSClientContainer.this.keepAlive);
		}

		protected Object readObject(byte[] bytes) throws IOException,
				ClassNotFoundException {
			ObjectInputStream oos = new ObjectInputStream(
					new ByteArrayInputStream(bytes));
			return oos.readObject();
		}

		protected ConnectionFactory createJMSConnectionFactory(JMSID targetID)
				throws IOException {
			JMSContainerConfig config = getJMSContainerConfig();
			return new ActiveMQConnectionFactory(config.getPropertyString(
					ActiveMQJMSServerContainer.USERNAME_PROPERTY,
					ActiveMQJMSServerContainer.DEFAULT_USERNAME),
					config.getPropertyString(
							ActiveMQJMSServerContainer.PASSWORD_PROPERTY,
							ActiveMQJMSServerContainer.DEFAULT_PASSWORD),
					targetID.getName());
		}
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