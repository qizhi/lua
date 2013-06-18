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

package com.cubeia.backoffice.wallet.api.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>Cubeia Wallet Protocol Unit implementation.</p>
 * 
 * <p>This bean is mapped to the XML specification by using JAXB
 * annotation framework.</p> 
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
@SuppressWarnings("serial")
@XmlRootElement(name="Licensee")
public class Licensee implements Serializable {
	
	private int id = 0;
	
	private int count = 0;
	
	/** 
	 * @return String, a readable representation
	 */
	public String toString() {
		return "id["+id+"] amount["+count+"]";
	}

	@XmlAttribute(name="id")
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	@XmlAttribute(name="count")
	public int getCount() {
		return count;
	}

	public void setCount(int amount) {
		this.count = amount;
	}
	
}
