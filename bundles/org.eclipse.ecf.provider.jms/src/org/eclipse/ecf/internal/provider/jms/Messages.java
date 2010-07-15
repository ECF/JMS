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

package org.eclipse.ecf.internal.provider.jms;

import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.provider.jms.messages"; //$NON-NLS-1$
	public static String AbstractJMSClient_EXCEPTION_INVALID_SERVER_RESPONSE;
	public static String AbstractJMSClientChannel_CONNECT_EXCEPTION_ALREADY_CONNECTED;
	public static String AbstractJMSClientChannel_CONNECT_EXCEPTION_CONNECT_ERROR;
	public static String AbstractJMSClientChannel_CONNECT_EXCEPTION_CONNECT_FAILED;
	public static String AbstractJMSClientChannel_CONNECT_EXCEPTION_INVALID_RESPONSE;
	public static String AbstractJMSClientChannel_CONNECT_EXCEPTION_NOT_SERIALIZABLE;
	public static String AbstractJMSClientChannel_CONNECT_EXCEPTION_TARGET_NOT_JMSID;
	public static String AbstractJMSClientChannel_CONNECT_EXCEPTION_TARGET_NOT_NULL;
	public static String AbstractJMSClientChannel_CONNECT_EXCEPTION_TARGET_REFUSED_CONNECTION;
	public static String AbstractJMSServer_CONNECT_EXCEPTION_CONTAINER_CLOSING;
	public static String AbstractJMSServer_CONNECT_EXCEPTION_CONTAINER_MESSAGE_NOT_NULL;
	public static String AbstractJMSServer_CONNECT_EXCEPTION_JOINGROUPMESSAGE_NOT_NULL;
	public static String AbstractJMSServer_CONNECT_EXCEPTION_REFUSED;
	public static String AbstractJMSServer_CONNECT_EXCEPTION_REMOTEID_NOT_NULL;
	public static String AbstractJMSServerChannel_CONNECT_EXCEPTION_CONTAINER_NOT_JMSID;
	public static String AbstractJMSServerChannel_CONNECT_EXCEPTION_CONTAINER_SERVER_CANNOT_CONNECT;
	public static String JMSNamespace_EXCEPTION_IDCREATION;
	public static String JMSNamespace_EXCEPTION_XMPP_ARGS_INVALID;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		//
	}
}
