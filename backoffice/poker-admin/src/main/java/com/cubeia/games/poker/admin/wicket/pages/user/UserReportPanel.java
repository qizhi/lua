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

package com.cubeia.games.poker.admin.wicket.pages.user;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class UserReportPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private String format = "csv";
	
	public UserReportPanel(String id, final ModalWindow modal) {
		super(id);		
		
		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		form.add(new AjaxSubmitLink("reportLink", form) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String url = getRequest().getContextPath() + "reportbuilder/reports/users/?format="+format;
				target.appendJavaScript("document.location = '" + url + "'");
				modal.close(target);
			}

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                // nothing to do here...
            }
		});
		
		RadioGroup<String> formatGroup = new RadioGroup<String>("formatGroup", new PropertyModel<String>(this, "format"));
		formatGroup.add(new Radio<String>("csv", Model.of("csv")));
		formatGroup.add(new Radio<String>("xls", Model.of("xls")));
		formatGroup.add(new Radio<String>("pdf", Model.of("pdf")));
		form.add(formatGroup);
		add(form); 
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	
}
