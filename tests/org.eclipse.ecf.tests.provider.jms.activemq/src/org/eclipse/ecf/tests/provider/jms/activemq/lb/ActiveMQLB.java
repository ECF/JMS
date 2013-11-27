/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.provider.jms.activemq.lb;

/**
 *
 */
public interface ActiveMQLB {

	public static final String QUEUE_PRODUCER_CONTAINER_NAME = "ecf.jms.activemq.tcp.manager.lb.svchost";
	public static final String QUEUE_CONSUMER_CONTAINER_NAME = "ecf.jms.activemq.tcp.lb.server";
	public static final String QUEUE_NAME = "tcp://localhost:61616/junit.testQueue";
	public static final String TOPIC_NAME = "tcp://localhost:61616/junit.testTopic";
}
