/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. and others. All rights reserved. This
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.jms.channel.AbstractJMSClientChannel;
import org.eclipse.ecf.provider.jms.container.AbstractJMSClient;
import org.eclipse.ecf.provider.jms.container.AbstractJMSContainerInstantiator;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
import org.eclipse.ecf.provider.jms.identity.JMSID;

public class ActiveMQClientContainer extends AbstractJMSClient {

	public static final String CONFIG_NAME = "ecf.jms.activemq.tcp.client";

	private int keepAlive;

	public static class Instantiator extends AbstractJMSContainerInstantiator {

		private static Map<String, List<String>> exporterImporterMap = new HashMap<String, List<String>>();

		static {
			List<String> importers = new ArrayList<String>();
			importers.add(ActiveMQClientContainer.CONFIG_NAME);
			exporterImporterMap.put(ActiveMQServerContainer.CONFIG_NAME, importers);
			exporterImporterMap.put(ActiveMQClientContainer.CONFIG_NAME, importers);
			exporterImporterMap.put(ActiveMQLBQueueProducerContainer.CONFIG_NAME, importers);
		}

		public Instantiator() {
			super(Arrays.asList(new String[] { ActiveMQClientContainer.CONFIG_NAME }), exporterImporterMap);
		}

		@Override
		public IContainer createInstance(ContainerTypeDescription description, Object[] args)
				throws ContainerCreateException {
			try {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) getMap(args);
				if (map == null) {
					JMSID clientID = (args != null && args.length > 0) ? getJMSIDFromParameter(args[0])
							: getJMSIDFromParameter(IDFactory.getDefault().createGUID().getName());
					Integer ka = (args != null && args.length > 1) ? getIntegerFromArg(args[1])
							: new Integer(ActiveMQServerContainer.DEFAULT_KEEPALIVE);
					map = new HashMap<String, Object>();
					map.put(ID_PARAM, clientID);
					map.put(KEEPALIVE_PARAM, ka);
				}
				return createInstance(description, map);
			} catch (Exception e) {
				if (e instanceof ContainerCreateException)
					throw (ContainerCreateException) e;
				return throwCreateException("Exception creating ActiveMQClientContainer", e);
			}
		}

		@Override
		public IContainer createInstance(ContainerTypeDescription description, Map<String, ?> parameters)
				throws ContainerCreateException {
			JMSID id = getJMSIDFromParams(parameters, IDFactory.getDefault().createGUID().getName());
			Integer ka = getKeepAlive(parameters, ActiveMQServerContainer.DEFAULT_KEEPALIVE);
			Map<String, String> props = new HashMap<String, String>();
			props.put(ActiveMQServerContainer.USERNAME_PROPERTY,
					getParameterValue(parameters, ActiveMQServerContainer.Instantiator.BROKER_USERNAME_PARAM,
							ActiveMQServerContainer.DEFAULT_USERNAME));
			props.put(ActiveMQServerContainer.PASSWORD_PROPERTY,
					getParameterValue(parameters, ActiveMQServerContainer.Instantiator.BROKER_PASSWORD_PARAM,
							ActiveMQServerContainer.DEFAULT_PASSWORD));
			return new ActiveMQClientContainer(new JMSContainerConfig(id, ka, props));
		}

	}

	/**
	 * @param keepAlive
	 * @throws Exception
	 */
	public ActiveMQClientContainer(int keepAlive) throws Exception {
		super();
		this.keepAlive = keepAlive;
	}

	public ActiveMQClientContainer(String name, int keepAlive) throws Exception {
		super(new JMSContainerConfig(name, keepAlive));
		this.keepAlive = keepAlive;
	}

	public ActiveMQClientContainer(JMSContainerConfig config) {
		super(config);
		this.keepAlive = config.getKeepAlive();
	}

	class ActiveMQClientChannel extends AbstractJMSClientChannel {

		private static final long serialVersionUID = -5581778054975360068L;

		public ActiveMQClientChannel() {
			super(getReceiver(), ActiveMQClientContainer.this.keepAlive);
		}

		protected Object readObject(byte[] bytes) throws IOException, ClassNotFoundException {
			return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
		}

		protected ConnectionFactory createJMSConnectionFactory(JMSID targetID) throws IOException {
			JMSContainerConfig config = getJMSContainerConfig();
			return new ActiveMQConnectionFactory(
					config.getPropertyString(ActiveMQServerContainer.USERNAME_PROPERTY,
							ActiveMQServerContainer.DEFAULT_USERNAME),
					config.getPropertyString(ActiveMQServerContainer.PASSWORD_PROPERTY,
							ActiveMQServerContainer.DEFAULT_PASSWORD),
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
	protected ISynchAsynchConnection createConnection(ID remoteSpace, Object data) throws ConnectionCreateException {
		return new ActiveMQClientChannel();
	}
}