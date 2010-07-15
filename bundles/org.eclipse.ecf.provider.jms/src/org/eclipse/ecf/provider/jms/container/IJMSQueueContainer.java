/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.container;

import javax.jms.*;
import org.eclipse.ecf.core.identity.ID;

public interface IJMSQueueContainer {

	public ID getID();

	public Session getSession();

	public MessageProducer getMessageProducer();

	public TemporaryQueue getResponseQueue();

	public ConnectionFactory getQueueConnectionFactory(String target, Object[] jmsConfiguration);

}
