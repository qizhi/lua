package com.cubeia.firebase.service.jta.atomikos;

import java.io.File;

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.ServiceRegistryAdapter;
import com.cubeia.firebase.transaction.ContextType;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.firebase.util.ServiceContextAdapter;

import junit.framework.TestCase;

public class TransactionManagerServiceTest extends TestCase {
	
	private TransactionManagerService manager;

	@Override
	protected void setUp() throws Exception {
		manager = new TransactionManagerService();
		final ServiceRegistryAdapter sa = new ServiceRegistryAdapter();
		sa.addImplementation(CoreTransactionManager.class, new CoreTransactionManager() {
		
			public CoreTransaction newTransaction(ContextType type) {
				return null;
			}
		
			public CoreTransaction currentTransaction() {
				return null;
			}
		});
		ServiceContextAdapter conAdapt = new ServiceContextAdapter() {
			
			@Override
			public ServiceRegistry getParentRegistry() {
				return sa;
			}
			
			@Override
			public File getServerLogDirectory() {
				return new File("logs/");
			}
		};
		manager.init(conAdapt);
		manager.start();
	}
	
	@Override
	protected void tearDown() throws Exception {
		manager.stop();
		manager.destroy();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	public void testDummy() throws Exception { }
}
