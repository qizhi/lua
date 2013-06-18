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
package com.cubeia.backoffice.operator.service.entity;

import javax.persistence.*;

import com.cubeia.backoffice.operator.api.OperatorAccountStatus;

import java.util.HashMap;
import java.util.Map;

@Entity
public class Operator {

    private Long id;
    private String name;
    private boolean enabled;
    private OperatorAccountStatus accountStatus;
    private Map<OperatorConfigParameter,String> config;

    public Operator() {
        this.config = new HashMap<OperatorConfigParameter, String>();
    }

    public Operator(long id, String name, boolean enabled) {
        this.id = id;
        this.name = name;
        this.enabled = enabled;
        config = new HashMap<OperatorConfigParameter, String>();
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Enumerated(EnumType.STRING)
    public OperatorAccountStatus getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(OperatorAccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}

    public void addConfig(OperatorConfigParameter parameter, String value) {
        getConfig().put(parameter,value);
    }
    public String getConfig(OperatorConfigParameter parameter) {
        return getConfig().get(parameter);
    }

    @MapKeyColumn(name="param")
    @MapKeyEnumerated(value = EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name="val")
    @CollectionTable(name="OperatorConfig", joinColumns=@JoinColumn(name="operatorId"))
    public Map<OperatorConfigParameter, String> getConfig() {
        return config;
    }

    public void setConfig(Map<OperatorConfigParameter, String> config) {
        this.config = config;
    }
}