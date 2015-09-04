/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.jms.container;

import java.io.IOException;
import java.util.Map;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.sharedobject.*;
import org.eclipse.ecf.core.sharedobject.util.IQueueEnqueue;

public class LBRegistrySharedObjectConfig implements ISharedObjectConfig {

	private ID soID;
	IJMSQueueContainer container;
	private ISharedObjectContext soContext;

	public LBRegistrySharedObjectConfig(ID soID, IJMSQueueContainer container) {
		this.soID = soID;
		this.container = container;
		this.soContext = new ISharedObjectContext() {

			/**
			 * @throws ContainerConnectException  
			 */
			public void connect(ID targetID, IConnectContext connectContext) throws ContainerConnectException {
				throw new ContainerConnectException("cannot connect"); //$NON-NLS-1$
			}

			public void disconnect() {
				// nothing todo
			}

			public Namespace getConnectNamespace() {
				return null;
			}

			public ID getConnectedID() {
				return null;
			}

			public ID[] getGroupMemberIDs() {
				return new ID[] {LBRegistrySharedObjectConfig.this.container.getID()};
			}

			public ID getLocalContainerID() {
				return LBRegistrySharedObjectConfig.this.container.getID();
			}

			@SuppressWarnings({"unchecked", "rawtypes"})
			public Map getLocalContainerProperties() {
				return null;
			}

			public IQueueEnqueue getQueue() {
				return null;
			}

			public ISharedObjectManager getSharedObjectManager() {
				return null;
			}

			public boolean isActive() {
				return true;
			}

			public boolean isGroupManager() {
				return false;
			}

			/**
			 * @throws IOException  
			 */
			public void sendCreate(ID targetID, ReplicaSharedObjectDescription sd) throws IOException {
				// nothing
			}

			/**
			 * @throws IOException  
			 */
			public void sendCreateResponse(ID targetID, Throwable throwable, long identifier) throws IOException {
				// nothing
			}

			/**
			 * @throws IOException  
			 */
			public void sendDispose(ID targetID) throws IOException {
				// nothing
			}

			/**
			 * @throws IOException  
			 */
			public void sendMessage(ID targetID, Object data) throws IOException {
				// nothing
			}

			@SuppressWarnings("unchecked")
			public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
				return null;
			}
		};
	}

	public ISharedObjectContext getContext() {
		return soContext;
	}

	public ID getHomeContainerID() {
		return container.getID();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Map getProperties() {
		return null;
	}

	public ID getSharedObjectID() {
		return soID;
	}

}
