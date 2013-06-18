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

package com.cubeia.games.poker.admin.wicket.util;

import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Builder and helper for Wicket PageParameter objects.
 * @author w
 */
public class ParamBuilder {

    private PageParameters params;

    private ParamBuilder() {
        this.params = new PageParameters();
    }
    
    /**
     * Create a singleton page parameter object.
     * @param key key
     * @param value value
     * @return page parameters
     */
    public static PageParameters params(String key, Object value) {
        return start(key, value).end();
    }
    
    /**
     * Create a page parameter object with two parameters.
     * @param key key
     * @param value value
     * @return page parameters
     */
    public static PageParameters params(String key, Object value, String key2, Object value2) {
        return start(key, value).param(key2, value2).end();
    }
    
    /**
     * Create a page parameter object with three parameters.
     * @param key key
     * @param value value
     * @return page parameters
     */
    public static PageParameters params(String key, Object value, String key2, Object value2, String key3, Object value3) {
        return start(key, value).param(key2, value2).param(key3, value3).end();
    }

    /**
     * Start building a page parameter object. Add parameters with 
     * @return
     */
    public static ParamBuilder start(String key, Object value) {
        ParamBuilder pb = new ParamBuilder();
        return pb.param(key, value);
    }
    
    public ParamBuilder param(String key, Object value) {
        params.add(key, value);
        return this;
    }
    
    public PageParameters end() {
        return params;
    }
    
}
