/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.weblogic.container;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.provider.jms.weblogic.WeblogicJMSClientChannel;
import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.jms.container.AbstractJMSClient;
import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;

public class WeblogicJMSClientContainer extends AbstractJMSClient {

	public WeblogicJMSClientContainer(int keepAlive) throws Exception {
		super(keepAlive);
	}

	public WeblogicJMSClientContainer(String name, int keepAlive)
			throws Exception {
		super(new JMSContainerConfig(name, keepAlive));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.provider.generic.ClientSOContainer#createConnection(org.eclipse.ecf.core.identity.ID,
	 *      java.lang.Object)
	 */
	protected ISynchAsynchConnection createConnection(ID remoteSpace,
			Object data) throws ConnectionCreateException {
		try {
			return new WeblogicJMSClientChannel(getReceiver(),
					((JMSContainerConfig) getConfig()).getKeepAlive());
		} catch (ECFException e) {
			throw new ConnectionCreateException(e.getStatus());
		}
	}
}