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

package com.cubeia.backoffice.accounting.api;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class TransactionDTOTest {

    @Test
    public void getAttributesAsStrings() {
        TransactionDTO txd = new TransactionDTO();
        
        assertThat(txd.getAttributes(), notNullValue());
        assertThat(txd.getAttributes().isEmpty(), is(true));
        assertThat(txd.getAttributesAsStrings(), notNullValue());
        assertThat(txd.getAttributesAsStrings().isEmpty(), is(true));
        
        TransactionAttributeDTO txad = new TransactionAttributeDTO();
        txad.setKey("a");
        txad.setValue("A");
        txd.getAttributes().put("a", txad);
        
        assertThat(txd.getAttributesAsStrings().get("a"), is("A"));
    }
    
}
