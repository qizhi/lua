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
package com.cubeia.firebase.api.server;

/**
 * Initializable component. It is guaranteed by its context to
 * be non-reentrant and that its pair of methods will properly mark
 * the component life-cycle. The generic parameter is used to make 
 * the initialization context type safe for context subclasses.
 * 
 * @author lars.j.nilsson
 * @see Context
 */
public interface Initializable<C extends Context> {

    /**
     * Initialize component. Normally, this method will swallow
     * all transient errors. If the component fails hard, and cannot
     * continue it should throw a system exception, for top level
     * components. If this method throws an exception, the component
     * is guaranteed to never be started.
     * 
     * @param con Component context, never null
     * @throws SystemException If the component fails to initialize
     */
    public void init(C con) throws SystemException;
    
    
    /**
     * Called when the component will no longer be used.
     */
    public void destroy();
    
}
