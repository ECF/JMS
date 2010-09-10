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

import java.net.URI;
import org.eclipse.ecf.core.identity.*;

public class JMSNamespace extends Namespace {
	private static final long serialVersionUID = 3761689000414884151L;

	private static final String SCHEME = "jms"; //$NON-NLS-1$

	public static final String NAME = "ecf.namespace.jmsid"; //$NON-NLS-1$

	public JMSNamespace() {
		super(NAME, null);
	}

	public ID createInstance(Object[] args) throws IDCreateException {
		try {
			String init = getInitFromExternalForm(args);
			if (init != null)
				return new JMSID(this, init);
			if (args.length == 1) {
				if (args[0] instanceof String) {
					return new JMSID(this, (String) args[0]);
				} else if (args[0] instanceof URI) {
					return new JMSID(this, ((URI) args[0]).toString());
				}
			}
			throw new IllegalArgumentException("Parameters invalid for JMSID creation.  Must be of either String or URI type"); //$NON-NLS-1$
		} catch (Exception e) {
			throw new IDCreateException("JMSID creation failed", e); //$NON-NLS-1$
		}
	}

	public String getScheme() {
		return SCHEME;
	}

	private String getInitFromExternalForm(Object[] args) {
		if (args == null || args.length < 1 || args[0] == null)
			return null;
		if (args[0] instanceof String) {
			String arg = (String) args[0];
			if (arg.startsWith(getScheme() + Namespace.SCHEME_SEPARATOR)) {
				int index = arg.indexOf(Namespace.SCHEME_SEPARATOR);
				if (index >= arg.length())
					return null;
				return arg.substring(index + 1);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.Namespace#getSupportedParameterTypesForCreateInstance()
	 */
	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { {String.class}, {URI.class}};
	}
}
