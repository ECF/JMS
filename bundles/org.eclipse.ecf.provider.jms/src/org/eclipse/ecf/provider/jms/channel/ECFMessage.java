/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.channel;

import java.io.Serializable;
import org.eclipse.ecf.core.identity.ID;

public interface ECFMessage extends Serializable {

	public ID getTargetID();

	public ID getSenderID();

	public String getSenderJMSID();

	public Serializable getData();
}
