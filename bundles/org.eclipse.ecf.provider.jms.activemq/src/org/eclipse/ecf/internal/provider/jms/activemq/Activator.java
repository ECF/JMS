package org.eclipse.ecf.internal.provider.jms.activemq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.util.AdapterManagerTracker;
import org.eclipse.ecf.core.util.ExtensionRegistryRunnable;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.provider.datashare.DatashareContainerAdapterFactory;
import org.eclipse.ecf.provider.remoteservice.generic.RemoteServiceContainerAdapterFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.jms.activemq"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private BundleContext context = null;

	private ServiceTracker<LogService, LogService> logServiceTracker = null;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	protected LogService getLogService() {
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker<LogService, LogService>(
					this.context, LogService.class, null);
			logServiceTracker.open();
		}
		return (LogService) logServiceTracker.getService();
	}

	public void log(IStatus status) {
		LogService logService = getLogService();
		if (logService != null) {
			logService.log(LogHelper.getLogCode(status),
					LogHelper.getLogMessage(status), status.getException());
		}
	}

	private static IAdapterManager getAdapterManager(BundleContext ctx) {
		AdapterManagerTracker t = new AdapterManagerTracker(ctx);
		t.open();
		IAdapterManager am = t.getAdapterManager();
		t.close();
		return am;
	}

	private List<IAdapterFactory> rscAdapterFactories;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context1) throws Exception {
		plugin = this;
		this.context = context1;
		SafeRunner.run(new ExtensionRegistryRunnable(this.context) {
			protected void runWithoutRegistry() throws Exception {
				context1.registerService(ContainerTypeDescription.class, new ContainerTypeDescription(ActiveMQJMSClientContainerInstantiator.JMS_CLIENT_NAME, new ActiveMQJMSClientContainerInstantiator(), "ActiveMQ Topic Client", false, false), null); //$NON-NLS-1$
				context1.registerService(ContainerTypeDescription.class, new ContainerTypeDescription(ActiveMQJMSServerContainerInstantiator.JMS_MANAGER_NAME, new ActiveMQJMSServerContainerInstantiator(), "ActiveMQ Topic Manager", true, false), null); //$NON-NLS-1$
				context1.registerService(ContainerTypeDescription.class, new ContainerTypeDescription(ActiveMQJMSQueueProducerContainerInstantiator.NAME, new ActiveMQJMSQueueProducerContainerInstantiator(), "ActiveMQ Load Balancing Service Host Container", false, false), null); //$NON-NLS-1$
				context1.registerService(ContainerTypeDescription.class, new ContainerTypeDescription(ActiveMQJMSQueueConsumerContainerInstantiator.NAME, new ActiveMQJMSQueueConsumerContainerInstantiator(), "ActiveMQ Load Balancing Server Container", true, false), null); //$NON-NLS-1$

				IAdapterManager am = getAdapterManager(context1);
				if (am != null) {
					rscAdapterFactories = new ArrayList<IAdapterFactory>();
					IAdapterFactory af = new RemoteServiceContainerAdapterFactory();
					am.registerAdapters(af, org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSClientContainer.class);
					rscAdapterFactories.add(af);
					af = new RemoteServiceContainerAdapterFactory();
					am.registerAdapters(af, org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSServerContainer.class);
					rscAdapterFactories.add(af);
					af = new RemoteServiceContainerAdapterFactory();
					am.registerAdapters(af, org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSQueueProducerContainer.class);
					rscAdapterFactories.add(af);
					af = new DatashareContainerAdapterFactory();
					am.registerAdapters(af, org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSClientContainer.class);
					rscAdapterFactories.add(af);
					af = new DatashareContainerAdapterFactory();
					am.registerAdapters(af, org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSServerContainer.class);
					rscAdapterFactories.add(af);
				}

			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
		}
		if (rscAdapterFactories != null) {
			IAdapterManager am = getAdapterManager(this.context);
			if (am != null) {
				for (@SuppressWarnings("rawtypes")
				Iterator i = rscAdapterFactories.iterator(); i.hasNext();)
					am.unregisterAdapters((IAdapterFactory) i.next());
			}
			rscAdapterFactories = null;
		}
		plugin = null;
		this.context = null;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
