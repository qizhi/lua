/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.admin.wicket.report;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class RequestBean {
	
	public static RequestBean parse(HttpServletRequest req) {
		/*
		 * Trim the path (which becomes the report name)
		 */
		String path = trim(req.getPathInfo());
		/*
		 * check the format and do default if needed
		 */
		String form = req.getParameter("format");
		if(form == null) {
			form = Format.CSV.name();
		}
		/*
		 * Convert parameter map to a simple string-string map
		 */
		@SuppressWarnings("rawtypes")
        Map params = req.getParameterMap();
		Map<String, String> tmp = new HashMap<String, String>(params.size());
		for (Object key : params.keySet()) {
			tmp.put(key.toString(), req.getParameter(key.toString()));
		}
		/*
		 * Continue parsing
		 */
		return parse(path, tmp, form);
	}

	@SuppressWarnings("rawtypes")
    public static RequestBean parse(String name, Map params, String format) {
		Map<String, String> map = checkParameters(params);
		Format f = Format.valueOf(format.toUpperCase());
		if(f == null) throw new IllegalArgumentException("Unknown format: " + format);
		return new RequestBean(name, map, f);
	}

	@SuppressWarnings("rawtypes")
    private static Map<String, String> checkParameters(Map p) {
		Map<String, String>  m = new HashMap<String, String>(p.size());
		for (Object o : p.keySet()) {
			String key = o.toString();
			if(key.startsWith("param:")) {
				key = key.substring(6);
				Object v = p.get(o);
				m.put(key, v.toString());
			}
		}
		return m;
	}

	static String trim(String info) {
		if(info.startsWith("/")) {
			info = info.substring(1, info.length());
		}
		if(info.endsWith("/")) {
			info = info.substring(0, info.length() - 1);
		}
		return info;
	}

	private String name;
	private Map<String, String> params;
	private Format format;

	RequestBean(String name, Map<String, String> params, Format format) {
		this.name = name;
		this.params = params;
		this.format = format;		
	}
	
	public void setFormat(Format format) {
		this.format = format;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public Format getFormat() {
		return format;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
