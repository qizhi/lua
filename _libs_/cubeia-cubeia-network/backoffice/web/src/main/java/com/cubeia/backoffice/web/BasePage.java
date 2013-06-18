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

package com.cubeia.backoffice.web;

import java.util.Date;

import com.cubeia.network.shared.web.wicket.navigation.*;
import com.cubeia.network.shared.web.wicket.navigation.MenuPanel;
import org.apache.commons.configuration.Configuration;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.cubeia.backoffice.web.util.FormatUtil;

@SuppressWarnings("serial")
public abstract class BasePage extends WebPage {
	
	@SpringBean(name="configuration")
	private Configuration configuration;
	
    public BasePage() {
		add(new MenuPanel("menuPanel",NetworkSiteMap.getPages(),this.getClass()));
        add(new Breadcrumbs("breadcrumb", NetworkSiteMap.getPages(),this.getClass()));
		// defer setting the title model object as the title may not be generated now
		add(new Label("title", new Model<String>()));
	}
	
	@Override
	protected void onBeforeRender() {
	    super.onBeforeRender();
	    get("title").setDefaultModelObject(getPageTitle());
	}
	
	/**
	 * Convenience method for date formatting.
	 * @param date date to format
	 * @return a string
	 */
	public String formatDate(Date date) {
	    return FormatUtil.formatDate(date);
	}
	
    public BackofficeAuthSession getBackofficeSession() {
        return (BackofficeAuthSession) getSession();
    }
    
    public abstract String getPageTitle();

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
    
}
