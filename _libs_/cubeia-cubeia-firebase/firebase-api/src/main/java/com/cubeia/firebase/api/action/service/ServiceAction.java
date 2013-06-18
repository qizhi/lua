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
package com.cubeia.firebase.api.action.service;

import java.util.List;

import com.cubeia.firebase.api.action.Attribute;
import com.cubeia.firebase.api.action.PlayerAction;

/**
 * Interface for actions that an addressable service. It can be noted that
 * actions for services are always delivered locally within the current
 * virtual machine. As such, service actions does not have o be 
 * serializable.
 * 
 * @author Fredrik
 */
public interface ServiceAction extends PlayerAction {
	
    public byte[] getData();
    
    public List<Attribute> getAttributes();
    
    public int getSeq();
    
}
