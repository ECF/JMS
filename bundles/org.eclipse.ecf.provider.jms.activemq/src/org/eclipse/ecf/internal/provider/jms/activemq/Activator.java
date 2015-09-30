package org.eclipse.ecf.internal.provider.jms.activemq;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.AdapterManagerTracker;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.provider.datashare.DatashareContainerAdapterFactory;
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSClientContainer;
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSQueueConsumerContainer;
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSQueueProducerContainer;
import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSServerContainer;
import org.eclipse.ecf.provider.remoteservice.generic.RemoteServiceContainerAdapterFactory;
import org.eclipse.ecf.remoteservice.provider.AdapterConfig;
import org.eclipse.ecf.remoteservice.provider.IRemoteServiceDistributionProvider;
import org.eclipse.ecf.remoteservice.provider.RemoteServiceDistributionProvider;
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
		this.context.registerService(IRemoteServiceDistributionProvider.class,
				new RemoteServiceDistributionProvider.Builder()
						.setName(ActiveMQJMSClientContainerInstantiator.JMS_CLIENT_NAME)
						.setInstantiator(new ActiveMQJMSClientContainerInstantiator())
						.setDescription("ActiveMQ Topic Client").setServer(false)
						.addAdapterConfig(new AdapterConfig(new RemoteServiceContainerAdapterFactory(),
								ActiveMQJMSClientContainer.class))
						.addAdapterConfig(new AdapterConfig(new DatashareContainerAdapterFactory(),
								ActiveMQJMSClientContainer.class))
						.build(),
				null);
		this.context.registerService(IRemoteServiceDistributionProvider.class,
				new RemoteServiceDistributionProvider.Builder()
						.setName(ActiveMQJMSServerContainerInstantiator.JMS_MANAGER_NAME)
						.setInstantiator(new ActiveMQJMSServerContainerInstantiator())
						.setDescription("ActiveMQ Topic Manager").setServer(true)
						.addAdapterConfig(new AdapterConfig(new RemoteServiceContainerAdapterFactory(),
								ActiveMQJMSServerContainer.class))
						.addAdapterConfig(new AdapterConfig(new DatashareContainerAdapterFactory(),
								ActiveMQJMSServerContainer.class))
						.build(),
				null);
		this.context.registerService(IRemoteServiceDistributionProvider.class,
				new RemoteServiceDistributionProvider.Builder()
						.setName(ActiveMQJMSQueueProducerContainerInstantiator.NAME)
						.setInstantiator(new ActiveMQJMSQueueProducerContainerInstantiator())
						.setDescription("ActiveMQ Load Balancing Service Host Container").setServer(false)
						.addAdapterConfig(new AdapterConfig(new RemoteServiceContainerAdapterFactory(),
								ActiveMQJMSQueueProducerContainer.class))
						.build(),
				null);
		this.context.registerService(IRemoteServiceDistributionProvider.class,
				new RemoteServiceDistributionProvider.Builder()
						.setName(ActiveMQJMSQueueConsumerContainerInstantiator.NAME)
						.setInstantiator(new ActiveMQJMSQueueConsumerContainerInstantiator())
						.setDescription("ActiveMQ Load Balancing Server Container").setServer(true)
						.addAdapterConfig(new AdapterConfig(new RemoteServiceContainerAdapterFactory(),
								ActiveMQJMSQueueConsumerContainer.class))
						.build(),
				null);
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
