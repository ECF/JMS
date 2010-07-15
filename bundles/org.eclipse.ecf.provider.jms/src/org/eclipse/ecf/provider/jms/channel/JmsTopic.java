/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.jms.channel;

import javax.jms.*;

/**
 * 
 */
public class JmsTopic {

	private Destination destination;
	private MessageConsumer consumer;
	private MessageProducer producer;

	public JmsTopic(Session session, String topic) throws JMSException {
		destination = session.createTopic(topic);
		consumer = session.createConsumer(destination);
		producer = session.createProducer(destination);
	}

	public JmsTopic(Session session, Destination destination) throws JMSException {
		this.destination = destination;
		this.consumer = session.createConsumer(destination);
		this.producer = session.createProducer(destination);
	}

	public MessageConsumer getConsumer() {
		return consumer;
	}

	public MessageProducer getProducer() {
		return producer;
	}
}
