package org.eclipse.ecf.internal.provider.jms.net4j;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.provider.BaseContainerInstantiator;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
import org.eclipse.ecf.provider.jms.identity.JMSNamespace;
import org.eclipse.ecf.provider.jms.net4j.container.Net4jServerContainer;

public class Net4jServerContainerInstantiator extends BaseContainerInstantiator
		implements IContainerInstantiator {

	public IContainer createInstance(ContainerTypeDescription description,
			Object[] parameters) throws ContainerCreateException {
		try {
			if (parameters == null || parameters.length < 1
					|| !(parameters[0] instanceof String))
				throw new IDCreateException(
						"parameters invalid for creating net4j server container");
			JMSContainerConfig containerConfig = new JMSContainerConfig(
					IDFactory.getDefault().createID(
							IDFactory.getDefault().getNamespaceByName(
									JMSNamespace.NAME), (String) parameters[0]));
			Net4jServerContainer server = new Net4jServerContainer(containerConfig);
			server.start();
			return server;
		} catch (Exception e) {
			throw new ContainerCreateException(e);
		}
	}
}
