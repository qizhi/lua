/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cubeia.firebase.server.service;

import com.cubeia.firebase.api.service.ServiceInfo;
import com.cubeia.firebase.api.util.UnmodifiableList;
import com.cubeia.firebase.util.UnmodifiableArrayList;

/**
 * This is an extension of the service info which contains additional
 * data used by the service infrastructure.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 29
 */
public class InternalServiceInfo implements ServiceInfo {

	private final boolean isPublic;
	private final String implClass;
	private final PackageSet exported;
	private final ServiceInfo info;
	private final Dependency[] deps;
	private final boolean proxy;
	private final boolean legacyContextClassLoader;

	InternalServiceInfo(String implClass, PackageSet exported, ServiceInfo info, Dependency[] deps, boolean isPublic, boolean proxy, boolean legacyContextClassLoader) {
		this.legacyContextClassLoader = legacyContextClassLoader;
		this.implClass = implClass;
		this.isPublic = isPublic;
		this.exported = exported;
		this.info = info;
		this.deps = deps;
		this.proxy = proxy;
	}
	
	protected InternalServiceInfo(InternalServiceInfo copy) {
		this.legacyContextClassLoader = copy.legacyContextClassLoader;
		this.implClass = copy.implClass;
		this.isPublic = copy.isPublic;
		this.exported = copy.exported;
		this.info = copy.info;
		this.deps = copy.deps;
		this.proxy = copy.proxy;
	}
	
	public boolean useLegacyContextClassLoader() {
		return legacyContextClassLoader;
	}
	
	public boolean isPublic() {
		return isPublic;
	}
	
	public boolean isProxy() {
		return proxy;
	}
	
	public UnmodifiableList<Dependency> getDependencies() {
		return new UnmodifiableArrayList<Dependency>(deps);
	}
	
	public String getDescription() {
		return info.getDescription();
	}
	
	public String[] getContractClasses() {
		return info.getContractClasses();
	}

	public String getName() {
		return info.getName();
	}

	public String getPublicId() {
		return info.getPublicId();
	}

	public boolean isAutoStart() {
		return info.isAutoStart();
	}

	public String getServiceClass() {
		return implClass;
	}
	
	
	/**
	 * Get all exported packages and classes. The returned package set should
	 * always contain the contract interface.
	 * 
	 * @return The package set or exported packages, never null
	 */
	public PackageSet getExportedPackages() {
		return exported;
	}
}
