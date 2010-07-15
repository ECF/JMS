package org.eclipse.ecf.internal.provider.jms.net4j;

import java.io.IOException;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.eclipse.ecf.provider.comm.ConnectionCreateException;
import org.eclipse.ecf.provider.comm.ISynchAsynchEventHandler;
import org.eclipse.ecf.provider.jms.channel.AbstractJMSClientChannel;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.net4j.IConnector;
import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.internal.tcp.TCPAcceptorFactory;
import org.eclipse.net4j.internal.tcp.TCPConnectorFactory;
import org.eclipse.net4j.internal.util.container.ManagedContainer;
import org.eclipse.net4j.internal.util.om.log.PrintLogHandler;
import org.eclipse.net4j.internal.util.om.trace.PrintTraceHandler;
import org.eclipse.net4j.jms.JMSInitialContext;
import org.eclipse.net4j.jms.JMSUtil;
import org.eclipse.net4j.jms.admin.IJMSAdmin;
import org.eclipse.net4j.jms.admin.JMSAdminUtil;
import org.eclipse.net4j.jms.server.JMSServerUtil;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.om.OMPlatform;

public class Net4jServerChannel extends AbstractJMSClientChannel {

	private static final String NET4J_JMS_CONNECTION_FACTORY = "net4j.jms.ConnectionFactory";
	
	public Net4jServerChannel(ISynchAsynchEventHandler handler, int keepAlive) throws ConnectionCreateException {
		super(handler, keepAlive);
	}

	private static Context init(JMSID id) throws ConnectionCreateException
	  {
	    OMPlatform.INSTANCE.addLogHandler(PrintLogHandler.CONSOLE);
	    OMPlatform.INSTANCE.addTraceHandler(PrintTraceHandler.CONSOLE);
	    OMPlatform.INSTANCE.setDebugging(true);
	    
	    /*
	    IDBAdapter.REGISTRY.put("derby", new DerbyAdapter());
	    IStore store = JDBCUtil.getStore();
	    Server.INSTANCE.setStore(store);
	    Server.INSTANCE.activate();
	    */
	    
	    IManagedContainer container = new ManagedContainer();
	    Net4jUtil.prepareContainer(container);
	    TCPUtil.prepareContainer(container);
	    JMSUtil.prepareContainer(container);
	    JMSServerUtil.prepareContainer(container);
	    JMSAdminUtil.prepareContainer(container);

	    TCPAcceptorFactory.get(container, null);
	    IConnector connector = TCPConnectorFactory.get(container, "localhost");

	    IJMSAdmin admin = JMSAdminUtil.createAdmin(connector);
	    admin.createTopic(id.getTopic());
	    
	    try {
			return new JMSInitialContext(container);
		} catch (NamingException e) {
			throw new ConnectionCreateException(e);
		}
	}


	protected ConnectionFactory createJMSConnectionFactory(JMSID targetID)
			throws IOException {
		try {
			Context context = init(targetID);
			return (ConnectionFactory) context.lookup(NET4J_JMS_CONNECTION_FACTORY);
		} catch (Exception e) {
			IOException e1 = new IOException(e.getLocalizedMessage());
			e1.setStackTrace(e.getStackTrace());
			throw e1;
		}
	}

}
