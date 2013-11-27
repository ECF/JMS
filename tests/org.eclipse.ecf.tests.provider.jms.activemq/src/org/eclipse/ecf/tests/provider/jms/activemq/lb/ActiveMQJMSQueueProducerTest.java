package org.eclipse.ecf.tests.provider.jms.activemq.lb;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

public abstract class ActiveMQJMSQueueProducerTest extends
		ContainerAbstractTestCase {

	public void testConnectConsumer() throws Exception {
		IContainer container = ContainerFactory.getDefault().createContainer(
				ActiveMQLB.QUEUE_PRODUCER_CONTAINER_NAME,
				new Object[] { ActiveMQLB.TOPIC_NAME, ActiveMQLB.QUEUE_NAME });
		Assert.isNotNull(container);
		container.dispose();
	}

}
