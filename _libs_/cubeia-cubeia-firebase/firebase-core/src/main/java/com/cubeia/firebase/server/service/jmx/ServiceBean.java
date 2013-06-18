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
package com.cubeia.firebase.server.service.jmx;

import com.cubeia.firebase.server.service.DefaultServiceRegistry;
import com.cubeia.firebase.server.service.InternalServiceInfo;

public class ServiceBean extends InternalServiceInfo {

	private final boolean isIsolated;
	private final DefaultServiceRegistry reg;

	public ServiceBean(InternalServiceInfo info, boolean isIsolated, DefaultServiceRegistry reg) {
		super(info);
		this.isIsolated = isIsolated;
		this.reg = reg;
	}
	
	public boolean isStarted() {
		return reg.isStarted(getPublicId());
	}
	
	public boolean isIsolated() {
		return isIsolated;
	}
	
	public String getStartupStackCapture() {
		StringBuilder b = new StringBuilder("{ ");
		StackTraceElement[] arr = reg.getStartupCapture(getPublicId());
		if(arr != null) {
			for (int i = 1; i <= arr.length; i++) {
				StackTraceElement e = arr[arr.length - i];
				b.append(e.toString());
				if(i + 1 < arr.length) {
					b.append(" -> ");
				}
			}
		}
		b.append(" }");
		return b.toString();
	}
}
