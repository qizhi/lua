/**
 * Copyright (C) 2010 Cubeia Ltd info@cubeia.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.guice.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.Context;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.guice.inject.FirebaseModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * This class should be sub-classed by Guice services. Implementors must override 
 * the {@link #getConfiguration()} help method to configure the injector context. In particular 
 * this configuration specifies the service contracts and implementation classes which are
 * needed in order to correctly create the Guicified instances.
 * 
 * <p>An example: If 'MyFirstContract' and 'MySecondContract' are implemented 
 * by 'MyService' then the subclass may look like this:
 * 
 * <pre>
 * public class MyHandler extends GuiceServiceHandle {
 *  
 *   @Override protected Configuration getConfiguration() {
 *     return new Configuration() {
 *       @Override
 *       public ContractsConfig getServiceContract() {
 *         return new ContractsConfig(MyService.class, MyFirstContract.class, MySecondContract.class);
 *       }
 *     };
 *   }
 * }
 * </pre>
 * 
 * Additional modules can be included in the injection context by overriding the 
 * {@link #preInjectorCreation(ServiceContext, List)} method.
 * 
 * <p>This method must be {@link #init(ServiceContext) initiated} before use in order
 * for the injection context to be created. This will happen automatically within Firebase
 * but should be called manually if used in testing. 
 * 
 * @author Lars J. Nilsson
 */
public abstract class GuiceServiceHandler implements InvocationHandler {

	private static final Method INIT_METHOD;
	
	static {
		try {
			INIT_METHOD = Initializable.class.getMethod("init", Context.class);
		} catch (Exception e) {
			throw new IllegalStateException("Not service init method available?!", e);
		} 
	}
	
	private Injector injector;
	private Class<?> serviceClass;
	
	private final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Initiate the handler. This creates the injection context and must be called
	 * the first thing. This happens automatically in Firebase, but should be done manually
	 * when testing. 
	 * 
	 * @param context Service context, must not be null
	 */
	public final void init(ServiceContext context) {
		injector = createInjector(context);
		Configuration config = getConfiguration();
		if(config != null) {
			serviceClass = config.getServiceContract().getService();
		}
	}

	/**
	 * This method forwards the invocation to the injection context. The actual
	 * instance that will be used as a target is looked up via {@link #findTargetForInvocation(Injector, Method, Object[])}.
	 * If no target is found for the invocation a warning is logged. This method also checks for the
	 * service "init" method, which is the first thing that happens in Firebase, and will call {@link #init(ServiceContext)}
	 * automatically. 
	 * 
	 * @param proxy Proxy object
	 * @param method Method call
	 * @param args Method arguments
	 */
	@Override
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.equals(INIT_METHOD)) {
			/*
			 * This only happens once...
			 */
			init((ServiceContext) args[0]);
		}
		Object target = findTargetForInvocation(injector, method, args);
		if(target != null) {
			return method.invoke(target, args);
		} else {
			log.warn("Found no target object for method " + method.getName());
			return null;
		}
	}
	
	/**
	 * Override this method to customize the invocation target lookup. The return from this
	 * method will be used to execute the given method. The default implementation uses any 
	 * configured service class as a lookup in the injection context.
	 * 
	 * @param inj Injection context, never null
	 * @param method Invocation method, never null
	 * @param args Method arguments, may be null
	 * @return A target for the invocation, or null if none is found
	 * @throws Throwable
	 */
	protected Object findTargetForInvocation(Injector inj, Method method, Object[] args) throws Throwable {
		Class<?> cl = findClassForMethod(method, args);
		if(cl != null) {
			return inj.getInstance(cl);
		} else {
			return null;
		}
	}

	/**
	 * Given a method for an invocation, return the class of the instance that it should
	 * be invoked on. This method is used by {@link #findTargetForInvocation(Injector, Method, Object[])}
	 * to find the class it looks up in the injection context. The default implementation returns any
	 * configured service class.
	 * 
	 * @param method Invocation method, never null
	 * @param args Method arguments, may be null
	 * @return The class to lookup target from, may return null
	 * @throws Throwable
	 */
	protected Class<?> findClassForMethod(Method method, Object[] args) throws Throwable {
		return serviceClass;
	}
	
	/**
	 * Get the basic configuration for this handler. This configuration is used to 
	 * bind the basic service contracts and instance into the Guice context. If this method
	 * returns null the subclass must override {@link #findTargetForInvocation(Injector, Method, Object[])}
	 * in order to find the service instance for each method invocation.
	 * 
	 * @return The service configuration, or null
	 */
	protected abstract Configuration getConfiguration();

	/**
	 * This method is called before the injection context is created, override to add 
	 * more modules to Guice. 
	 * 
	 * @param context The service context, never null
	 * @param modules A list of modules, never null
	 */
	protected void preInjectorCreation(ServiceContext context, List<Module> modules) { }

	
	// --- PRIVATE METHODS --- //
	
	private Injector createInjector(ServiceContext context) {
		List<Module> list = new ArrayList<Module>(5);
		addStandardModules(context, list);
		preInjectorCreation(context, list);
		return Guice.createInjector(list);
	}

	private void addStandardModules(ServiceContext context, List<Module> list) {
		list.add(new FirebaseModule(context.getParentRegistry()));
		list.add(new ServiceModule(context, getConfiguration()));
	}
}
