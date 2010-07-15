package org.eclipse.ecf.internal.provider.jms.net4j;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.provider.BaseContainerInstantiator;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
import org.eclipse.ecf.provider.jms.net4j.container.Net4jClientContainer;

public class Net4jClientContainerInstantiator extends BaseContainerInstantiator
		implements IContainerInstantiator {

	public IContainer createInstance(ContainerTypeDescription description,
			Object[] parameters) throws ContainerCreateException {
		try {
			JMSContainerConfig containerConfig = new JMSContainerConfig(IDFactory.getDefault().createGUID());
			return new Net4jClientContainer(containerConfig);
		} catch (IDCreateException e) {
			throw new ContainerCreateException(e);
		}
	}
}
