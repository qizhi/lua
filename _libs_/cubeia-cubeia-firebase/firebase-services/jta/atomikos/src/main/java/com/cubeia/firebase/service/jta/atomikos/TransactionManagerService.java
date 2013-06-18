package com.cubeia.firebase.service.jta.atomikos;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.atomikos.icatch.config.TSInitInfo;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.jdbc.SimpleDataSourceBean;
import com.atomikos.jdbc.nonxa.NonXADataSourceBean;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.transaction.EventTransactionContext;
import com.cubeia.firebase.server.deployment.datasource.DatasourceDeployment;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;

/**
 * Atomikos implementation of the transaction service. This service hard wires a couple
 * of configuration properties. Future versions should possibly make them configurable.
 * 
 * <p>Atomikos has a proprietary log format, this log is by default set to level "WARN",
 * this can be changed using the system property "atomikos.logLevel". Possible values
 * are WARN, INFO or DEBUG.
 * 
 * @author Larsan
 * @date 2007 maj 23
 */
public class TransactionManagerService implements TransactionManagerProvider, Service {

	// Illegal LOCAL-TX value
	private static final String MISSING_PROP_ERR = "Missing property, a LOCAL-TX data source must have either " + DatasourceDeployment.XA_DATA_SOURCE_KEY + " or " + DatasourceDeployment.DRIVER_KEY + " property set.";

	
	// --- INSTANCE MEMBERS --- //
	
	private UserTransactionServiceImp imp;
	private List<Closable> created = new LinkedList<Closable>();
	private CoreTransactionManager coreManager;

	public void destroy() {
		closeCreated();
		imp.shutdown(true);
	}

	public void init(ServiceContext con) throws SystemException {
		coreManager = con.getParentRegistry().getServiceInstance(CoreTransactionManager.class);
		imp = new UserTransactionServiceImp();
		TSInitInfo info = imp.createTSInitInfo();
		setConfigProperties(con, info.getProperties());
		imp.init(info);
	}

	private void setConfigProperties(ServiceContext con, Properties p) {
		// standalone
		p.setProperty("com.atomikos.icatch.service", "com.atomikos.icatch.standalone.UserTransactionServiceFactory");
		// no logging aka no recovery
		p.setProperty("com.atomikos.icatch.enable_logging", "false");
		// local tx max timeout = 1 minute
		p.setProperty("com.atomikos.icatch.max_timeout", "60000");
		// propritary log level, use system property "atomikos.logLevel" to change [DEBUG|INFO|WARN]
		p.setProperty("com.atomikos.icatch.console_log_level", System.getProperty("atomikos.logLevel", "WARN"));
		// writing log to dir?
		p.setProperty("com.atomikos.icatch.output_dir", con.getServerLogDirectory().getAbsolutePath());
		p.setProperty("com.atomikos.icatch.log_base_dir", con.getServerLogDirectory().getAbsolutePath());
	}

	public void start() { }

	public void stop() { }

	public TransactionManager getTransactionManager() {
		return imp.getTransactionManager();
	}
	
	public EventTransactionContext getEventContext() {
		CoreTransaction trans = coreManager.currentTransaction();
		return (trans == null ? null : trans.getEventContext());
	}

	public UserTransaction getUserTransaction() {
		return imp.getUserTransaction();
	}
	
	public DataSource createLocalTxDataSource(String name, Properties props) throws SQLException {
		if(isEmulated(props)) return newEmulatedDriver(name, props);
		else return newXADataSource(name, props);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void closeCreated() {
		for (Closable cl : created) {
			cl.close();
		}
	}

	// is this for an emulated driver?
	private boolean isEmulated(Properties props) throws SQLException {
		if(props.containsKey(DatasourceDeployment.XA_DATA_SOURCE_KEY)) return false;
		else if(props.containsKey(DatasourceDeployment.DRIVER_KEY))return true;
		else throw new SQLException(MISSING_PROP_ERR);
	}

	private DataSource newXADataSource(String name, Properties props) {
		SimpleDataSourceBean sbean = new SimpleDataSourceBean();
		sbean.setUniqueResourceName(name);
		if(props.containsKey(DatasourceDeployment.POOL_SIZE_KEY)) {
			sbean.setConnectionPoolSize(getInt(props, DatasourceDeployment.POOL_SIZE_KEY, DatasourceDeployment.POOL_SIZE_DEF));
		}
		if(props.containsKey(DatasourceDeployment.TIMEOUT_KEY)) {
			sbean.setConnectionTimeout(getInt(props, DatasourceDeployment.TIMEOUT_KEY, DatasourceDeployment.TIMEOUT_DEF));	
		}
		sbean.setXaDataSourceClassName(props.getProperty(DatasourceDeployment.XA_DATA_SOURCE_KEY));
		sbean.setXaDataSourceProperties(getXaDsProps(props));
		created.add(new Closable(sbean));
		return sbean;
	}

	// get props in string form
	private String getXaDsProps(Properties props) {
		StringBuilder b = new StringBuilder();
		for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
			String key = (String)it.next();
			if(key.startsWith("ds.")) {
				String val = props.getProperty(key);
				String realKey = key.substring(3);
				if(b.length() > 0) {
					// separate if not first
					b.append(";");
				}
				b.append(realKey).append("=").append(val);
			}
		}
		return b.toString();
	}

	private DataSource newEmulatedDriver(String name, Properties props) {
		NonXADataSourceBean sbean = new NonXADataSourceBean();
		sbean.setUniqueResourceName(name);
		if(props.containsKey(DatasourceDeployment.POOL_SIZE_KEY)) {
			sbean.setPoolSize(getInt(props, DatasourceDeployment.POOL_SIZE_KEY, DatasourceDeployment.POOL_SIZE_DEF));
		}
		if(props.containsKey(DatasourceDeployment.TIMEOUT_KEY)) {
			sbean.setConnectionTimeout(getInt(props, DatasourceDeployment.TIMEOUT_KEY, DatasourceDeployment.TIMEOUT_DEF));	
		}
		sbean.setDriverClassName(props.getProperty(DatasourceDeployment.DRIVER_KEY));
		sbean.setUrl(props.getProperty(DatasourceDeployment.URL_KEY));
		sbean.setPassword(props.getProperty(DatasourceDeployment.PASSWORD_KEY));
		sbean.setUser(props.getProperty(DatasourceDeployment.USER_KEY));
		created.add(new Closable(sbean));
		return sbean;
	}
	
	private int getInt(Properties props, String key, int def) {
		String s = props.getProperty(key);
		return safeParse(s, def);
	}

	private int safeParse(String s, int def) {
		if(s == null) return def;
		try {
			return Integer.parseInt(s);
		} catch(NumberFormatException e) {
			return def;
		}
	}


	// --- PRIVATE CLASSES --- //
	
	/**
	 * Simple wrapper for a common close method when
	 * the service is destroyed.
	 */
	private static class Closable {
		
		private final NonXADataSourceBean nb;
		private final SimpleDataSourceBean sb;
		
		private Closable(SimpleDataSourceBean o) {
			this.sb = o;
			nb = null;
		}
		
		private Closable(NonXADataSourceBean o) {
			this.nb = o;
			sb = null;
		}
		
		private void close() {
			if(nb != null) nb.close();
			if(sb != null) {
				try {
					sb.close();
				} catch(SQLException e) {
					Logger.getLogger(getClass()).error(e);
				}
			}
		}
	}
}
