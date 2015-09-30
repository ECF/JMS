/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.jms;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.provider.jms.identity.JMSNamespace;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The main plugin class to be used in the desktop. some more comment
 */
public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.jms"; //$NON-NLS-1$

	// The shared instance.
	private static Activator plugin;

	private BundleContext context = null;

	private ServiceTracker<LogService, LogService> logServiceTracker = null;

	/**
	 * The constructor.
	 */
	public Activator() {
		super();
		plugin = this;
	}

	protected LogService getLogService() {
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker<LogService, LogService>(this.context, LogService.class.getName(), null);
			logServiceTracker.open();
		}
		return logServiceTracker.getService();
	}

	public void log(IStatus status) {
		LogService logService = getLogService();
		if (logService != null)
			logService.log(LogHelper.getLogCode(status), LogHelper.getLogMessage(status), status.getException());
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(final BundleContext ctxt) throws Exception {
		this.context = ctxt;
		ctxt.registerService(Namespace.class, new JMSNamespace(), null);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext ctxt) throws Exception {
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
		}
		this.context = null;
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public synchronized static Activator getDefault() {
		if (plugin == null) {
			plugin = new Activator();
		}
		return plugin;
	}

}
