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
package com.cubeia.backoffice.operator.service.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cubeia.backoffice.operator.service.entity.OperatorConfigParameter;

public class OperatorDefaultParamsTest {

	OperatorDefaultParams operatorDefaults = new OperatorDefaultParams();
	
	@Test
	public void testInsertDefault() {
		Map<OperatorConfigParameter, String> config = new HashMap<OperatorConfigParameter, String>();
		operatorDefaults.addDefaultConfiguration(config);
		
		assertThat(config.get(OperatorConfigParameter.CLIENT_HELP_URL), is("www.hjalp.nu"));
	}

}
