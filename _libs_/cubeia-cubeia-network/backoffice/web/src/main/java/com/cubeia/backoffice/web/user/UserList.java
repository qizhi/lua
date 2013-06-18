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

package com.cubeia.backoffice.web.user;

import com.cubeia.backoffice.report.ReportServlet;
import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserQueryResult;
import com.cubeia.backoffice.users.client.UserServiceClient;
import com.cubeia.backoffice.web.BasePage;
import com.cubeia.backoffice.web.util.LabelLinkPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.cubeia.backoffice.web.util.ParamBuilder.params;

@AuthorizeInstantiation({"SUPER_USER", "USER_ADMIN"})
public class UserList extends BasePage {
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(UserList.class);

    @SpringBean(name="client.user-service")
    private UserServiceClient userService;

    private Long userId;
    private String name;
    
	private final class UsersDataProvider extends SortableDataProvider<User,String> {
        private static final long serialVersionUID = 1L;
        
        public UsersDataProvider() {
            setSort(UserOrder.ID.name(), SortOrder.ASCENDING);
        }

        @Override
        public Iterator<User> iterator(long first, long count) {
            SortParam<String> sort = getSort();
            return getUserList(getUserId(), getName(), (int)first, (int)count, sort.getProperty(), sort.isAscending()).getUsers().iterator();
        }

        @Override
        public IModel<User> model(User u) {
            return Model.of(u);
        }

        @Override
        public long size() {
            return getUserList(getUserId(), getName(), 0, 0, null, true).getTotalQueryResultSize();
        }
    }

    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public UserList(final PageParameters parameters) {
        final Form<?> searchForm = new Form<UserList>("searchForm", new CompoundPropertyModel<UserList>(this));
        final TextField<String> idField = new TextField<String>("userId");
        searchForm.add(idField);
        final TextField<String> userNameField = new TextField<String>("name");
        searchForm.add(userNameField);
        searchForm.add(new Button("clearForm"){
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				idField.clearInput();
				userNameField.clearInput();
			}        	
        });
        add(searchForm);
        add(new FeedbackPanel("feedback"));
        
        final UsersDataProvider dataProvider = new UsersDataProvider();
    	List<IColumn<User,String>> columns = new ArrayList<IColumn<User,String>>();
        
    	columns.add(new AbstractColumn<User,String>(Model.of("User Id")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<User>> item, String componentId, IModel<User> model) {
                Long userId = model.getObject().getUserId();
                LabelLinkPanel panel = new LabelLinkPanel(
                    componentId, "" + userId, 
                    UserSummary.class,  
                    params(UserSummary.PARAM_USER_ID, userId));
                item.add(panel);
            }
            
            @Override
            public boolean isSortable() {
                return true;
            }
            
            @Override
            public String getSortProperty() {
                return UserOrder.ID.name();
            }
    	});
        columns.add(new PropertyColumn<User,String>(Model.of("XId"), "externalUserId"));
        columns.add(new PropertyColumn<User,String>(Model.of("User name"), UserOrder.USER_NAME.name(), "userName"));
        columns.add(new PropertyColumn<User,String>(Model.of("Status"), UserOrder.STATUS.name(), "status"));
//        columns.add(new PropertyColumn<User,String>(Model.of("First name"), "userInformation.firstName"));
//        columns.add(new PropertyColumn<User,String>(Model.of("Last name"), "userInformation.lastName"));
//        columns.add(new PropertyColumn<User,String>(Model.of("Country"), UserOrder.COUNTRY.name(), "userInformation.country"));
        columns.add(new PropertyColumn<User,String>(Model.of("Ext. Username"), "attributes.externalUsername"));
        columns.add(new PropertyColumn<User,String>(Model.of("Screename"), "attributes.screenname"));
        
    	AjaxFallbackDefaultDataTable<User,String> userTable = new AjaxFallbackDefaultDataTable<User,String>("userTable", columns, dataProvider , 20);
    	add(userTable);
    	
    	final ModalWindow modal = new ModalWindow("modal");
    	modal.setContent(new UserReportPanel(modal.getContentId(), modal));
        modal.setTitle("Generate report");
        modal.setCookieName("modal");
        modal.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        modal.setInitialWidth(265);
        modal.setInitialHeight(200);

    	add(modal);
    	add(new AjaxLink<Void>("showReportPanel") {
    		private static final long serialVersionUID = 1L;

			@Override
            public void onClick(AjaxRequestTarget target) {
				List<User> users = getUserList(getUserId(), getName(), 0, Integer.MAX_VALUE, dataProvider.getSort().getProperty(), dataProvider.getSort().isAscending()).getUsers();				
				HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
				request.getSession().setAttribute(ReportServlet.REPORTS_COLLECTION_DATA_SOURCE, users);
				modal.show(target);
            }
        });
    }

	private UserQueryResult getUserList(
	    Long userId, String userName, int offset, int limit, String sortProperty, boolean ascending) {
	    
	    UserOrder sortOrder = convertPropertyToSortOrder(sortProperty);
        UserQueryResult result = userService.findUsers(getUserId(), getName(), offset, limit, sortOrder, ascending);
        return result;
	}

    private UserOrder convertPropertyToSortOrder(String sortProperty) {
        return sortProperty == null ? null : UserOrder.valueOf(sortProperty);
    }

    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
	
    @Override
    public String getPageTitle() {
        return "User list";
    }
}
