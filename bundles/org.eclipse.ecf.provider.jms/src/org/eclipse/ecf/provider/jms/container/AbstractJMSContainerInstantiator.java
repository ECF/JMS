/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.container;

import java.util.Map;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.ecf.provider.jms.identity.JMSNamespace;
import org.eclipse.ecf.remoteservice.provider.PeerRemoteServiceContainerInstantiator;

public abstract class AbstractJMSContainerInstantiator extends PeerRemoteServiceContainerInstantiator {

	public AbstractJMSContainerInstantiator(String peerA, String peerB) {
		super(peerA, peerB);
	}

	public static final String ID_PARAM = "id"; //$NON-NLS-1$
	public static final String KEEPALIVE_PARAM = "keepAlive"; //$NON-NLS-1$

	protected JMSID getJMSIDFromParameter(Object p, String def) {
		if (p == null && def == null)
			return null;
		if (p instanceof String) {
			return (JMSID) IDFactory.getDefault().createID(JMSNamespace.NAME, (String) p);
		} else if (p instanceof JMSID) {
			return (JMSID) p;
		} else
			return (JMSID) IDFactory.getDefault().createID(JMSNamespace.NAME, def);
	}

	protected JMSID getJMSIDFromParams(Map<String, ?> params, String def) {
		return getJMSIDFromParameter(params.get(ID_PARAM), def);
	}

	protected Integer getKeepAlive(Map<String, ?> params, Integer def) {
		return getParameterValue(params, KEEPALIVE_PARAM, Integer.class, def);
	}

}
