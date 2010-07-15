/****************************************************************************
 * Copyright (c) 2004 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.jms.identity;

import org.eclipse.ecf.core.identity.*;

public class JMSID extends StringID {

	private static final long serialVersionUID = 3979266962767753264L;

	private static final String PROTOCOLSEPARATOR = "://"; //$NON-NLS-1$

	/**
	 * @param n
	 * @param s
	 */
	protected JMSID(Namespace n, String s) {
		super(n, s);
	}

	protected boolean namespaceEquals(BaseID obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof JMSID))
			return false;
		JMSID o = (JMSID) obj;
		return value.equals(o.getName());
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("JMSID["); //$NON-NLS-1$
		sb.append(getName()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	public String getTopicOrQueueName() {
		String server = getBroker();
		if (server.length() > 0) {
			String postServer = getName().substring(server.length());
			int indexOfPathStart = postServer.indexOf("/"); //$NON-NLS-1$
			if (indexOfPathStart != -1) {
				String path = postServer.substring(indexOfPathStart + 1);
				while (path.charAt(0) == '/')
					path = path.substring(1, path.length());
				return path;
			}
		}
		return ""; //$NON-NLS-1$
	}

	public String getBroker() {
		int indexOfSlashes = getName().indexOf(PROTOCOLSEPARATOR);
		if (indexOfSlashes != -1) {
			String postProtocol = getName().substring(indexOfSlashes + PROTOCOLSEPARATOR.length());
			int indexOfPathStart = postProtocol.indexOf('/');
			if (indexOfPathStart != -1) {
				return getName().substring(0, indexOfSlashes + PROTOCOLSEPARATOR.length() + indexOfPathStart);
			}
		}
		return ""; //$NON-NLS-1$
	}
}
