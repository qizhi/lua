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
package com.cubeia.firebase.server.conf;

import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.server.master.RealConfig;

public class FilteredConfig extends RealConfig {

	private final Namespace ns;

	public FilteredConfig(String id, Configuration parent, Namespace ns) {
		super(id, parent);
		this.ns = ns;
		applyFilter();
	}

	
	/// --- PROTECTED METHODS --- ///
	
	@Override
	protected boolean accept(PropertyKey key) {
		if(ns == null) return true;
		String my = ns.toString();
		String test = key.getNamespace().toString();
		return test.startsWith(my);
	}
	
	
	/// --- PRIVATE METHODS --- ///
	
	private void applyFilter() {
		int len = ns.toString().length();
		for(PropertyKey key : super.props.keySet()) {
			String val = super.props.remove(key);
			if(accept(key)) {
				String tmp = key.toString();
				int ind = tmp.lastIndexOf(':');
				String nNs = tmp.substring(len, ind);
				String nProp = tmp.substring(ind + 1);
				Namespace ns = (nNs.length() == 0 ? Namespace.NULL : new Namespace(nNs));
				PropertyKey k = new PropertyKey(ns, nProp);
				super.props.put(k, val);
			}
		}
	}
}