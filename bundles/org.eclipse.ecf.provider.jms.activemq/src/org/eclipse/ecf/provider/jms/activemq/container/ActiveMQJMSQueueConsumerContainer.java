/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.activemq.container;

import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.provider.jms.container.AbstractJMSQueueConsumerContainer;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.ecf.remoteservice.IRemoteServiceCallPolicy;

public class ActiveMQJMSQueueConsumerContainer extends
		AbstractJMSQueueConsumerContainer {

	@SuppressWarnings("rawtypes")
	private Map properties;

	public ActiveMQJMSQueueConsumerContainer(JMSID containerID, JMSID queueID) {
		this(containerID, queueID, null);
	}

	private String getPropertyValue(String propName, String defaultValue) {
		if (properties == null)
			return defaultValue;
		String o = (String) properties.get(propName);
		return (o == null) ? defaultValue : o;
	}

	public ActiveMQJMSQueueConsumerContainer(JMSID containerID, JMSID queueID,
			@SuppressWarnings("rawtypes") Map props) {
		super(containerID, queueID);
		this.properties = props;
	}

	public ConnectionFactory getQueueConnectionFactory(String target,
			Object[] jmsConfiguration) {
		return new ActiveMQConnectionFactory(getPropertyValue(
				ActiveMQJMSServerContainer.USERNAME_PROPERTY,
				ActiveMQJMSServerContainer.DEFAULT_USERNAME), getPropertyValue(
				ActiveMQJMSServerContainer.PASSWORD_PROPERTY,
				ActiveMQJMSServerContainer.DEFAULT_PASSWORD), target);
	}

	public void start() throws ECFException {
		try {
			setupJMSQueueConsumer(getQueueID());
		} catch (JMSException e) {
			ECFException t = new ECFException(
					"Could not connect to queueID=" + queueID, e); //$NON-NLS-1$
			t.setStackTrace(e.getStackTrace());
			throw t;
		}
	}

	public boolean setRemoteServiceCallPolicy(IRemoteServiceCallPolicy policy) {
		// TODO Auto-generated method stub
		return false;
	}

}
