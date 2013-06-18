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

import com.cubeia.backoffice.users.api.dto.Gender;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserStatus;
import com.cubeia.backoffice.users.client.UserServiceClient;
import com.cubeia.games.poker.admin.wicket.BasePage;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.cubeia.backoffice.users.api.dto.UserStatus.BLOCKED;
import static com.cubeia.backoffice.users.api.dto.UserStatus.ENABLED;
import static com.cubeia.games.poker.admin.wicket.util.ParamBuilder.params;
/**
 */
@AuthorizeInstantiation({"SUPER_USER", "USER_ADMIN"})
public class EditUser extends BasePage {
    public static final String PARAM_USER_ID = "userId";

    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(EditUser.class);

    @SpringBean(name="userClient")
    private UserServiceClient userService;
    
    private User user;
    
    private String password1;
    private String password2;
    
    private String newAttributeKey;
    private String newAttributeValue;
    
    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public EditUser(PageParameters parameters) {
        super(parameters);
    	if (!assertValidUserid(parameters)) {
    		return;
    	}
    	
        final Long userId = parameters.get(PARAM_USER_ID).toLongObject();
        loadFormData(userId);

        if (getUser() == null  ||  getUser().getStatus() == UserStatus.REMOVED) {
            log.debug("user is removed, id = " + userId);
            setInvalidUserResponsePage(userId);
            return;
        }
        
        add(createBlockActionLink(userId));
        
        add(new Link<Void>("removeActionLink") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
                userService.setUserStatus(userId, UserStatus.REMOVED);
                setInvalidUserResponsePage(userId);
            }
        });
        
        Form<?> userForm = new Form<Void>("userForm") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit() {
                userService.updateUser(user);
                info("User updated, id = " + user.getUserId());
                loadFormData(userId);
            }
        };
        
        CompoundPropertyModel<?> cpm = new CompoundPropertyModel<EditUser>(this);
        
        userForm.add(new Label(PARAM_USER_ID, cpm.bind("user.userId")));
        userForm.add(new Label("status", cpm.bind("user.status")));
        userForm.add(new TextField<Long>("operatorId", cpm.<Long>bind("user.operatorId")).setEnabled(false));
        userForm.add(new TextField<String>("externalUserId", cpm.<String>bind("user.externalUserId")));
        userForm.add(new TextField<String>("userName", cpm.<String>bind("user.userName")));
        userForm.add(new TextField<String>("firstName", cpm.<String>bind("user.userInformation.firstName")));
        userForm.add(new TextField<String>("lastName", cpm.<String>bind("user.userInformation.lastName")));
        userForm.add(new TextField<String>("email", cpm.<String>bind("user.userInformation.email")).
            add(EmailAddressValidator.getInstance()));
        userForm.add(new TextField<String>("title", cpm.<String>bind("user.userInformation.title")));
        userForm.add(new TextField<String>("city", cpm.<String>bind("user.userInformation.city")));
        userForm.add(new TextField<String>("billingAddress", cpm.<String>bind("user.userInformation.billingAddress")));
        userForm.add(new TextField<String>("fax", cpm.<String>bind("user.userInformation.fax")));
        userForm.add(new TextField<String>("cellphone", cpm.<String>bind("user.userInformation.cellphone")));
        userForm.add(new DropDownChoice<String>(
            "country",
            cpm.<String>bind("user.userInformation.country"),
            Arrays.asList(Locale.getISOCountries()),
            new IChoiceRenderer<String>() {
                private static final long serialVersionUID = 1L;

                @Override
                public String getDisplayValue(String isoCountry) {
                    return new Locale(
                        Locale.ENGLISH.getLanguage(), 
                        (String) isoCountry).getDisplayCountry(Locale.ENGLISH);
                }

                @Override
                public String getIdValue(String isoCountry, int id) {
                    return "" + id;
                }
            }));
        
        userForm.add(new TextField<String>("zipcode", cpm.<String>bind("user.userInformation.zipcode")));
        userForm.add(new TextField<String>("state", cpm.<String>bind("user.userInformation.state")));
        userForm.add(new TextField<String>("phone", cpm.<String>bind("user.userInformation.phone")));
        userForm.add(new TextField<String>("workphone", cpm.<String>bind("user.userInformation.workphone")));
        userForm.add(new DropDownChoice<Gender>(
            "gender", 
            cpm.<Gender>bind("user.userInformation.gender"), 
            Arrays.asList(Gender.values())));
        userForm.add(createAttributesListView());
        add(userForm);
        
        
        Form<?> addAttributeForm = new Form<Void>("addAttrForm") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
            	if(user.getAttributes() == null) {
            		user.setAttributes(new HashMap<String, String>());
            	}
            	if(getNewAttributeKey() != null) {
            		user.getAttributes().put(""+getNewAttributeKey(), ""+getNewAttributeValue());
            	}
                setNewAttributeKey(null);
                setNewAttributeValue(null);
            }
        };
        addAttributeForm.add(new SubmitLink("addAttrLink").add(new Label("addAttrLabel", "Add attribute")));
        addAttributeForm.add(new TextField<String>("newAttrKey", cpm.<String>bind("newAttributeKey")));
        addAttributeForm.add(new TextField<String>("newAttrValue", cpm.<String>bind("newAttributeValue")));
        userForm.add(addAttributeForm);
        
        Form<?> pwdForm = new Form<Void>("changePasswordForm") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                userService.updatePassword(user.getUserId(), getPassword1());
                setPassword1(null);
                setPassword2(null);
            }
        };
        PasswordTextField pwd1 = new PasswordTextField("password1", cpm.<String>bind("password1"));
        PasswordTextField pwd2 = new PasswordTextField("password2", cpm.<String>bind("password2"));
        pwdForm.add(new EqualPasswordInputValidator(pwd1, pwd2));
        pwdForm.add(pwd1);
        pwdForm.add(pwd2);
        add(pwdForm);
        
        
        
        add(new FeedbackPanel("feedback"));
    }

    private boolean assertValidUserid(PageParameters params) {
    	try {
    		params.get(PARAM_USER_ID).toLongObject();
    		return true;
    	} catch (Exception e) {
    		setResponsePage(InvalidUser.class, params(InvalidUser.PARAM_USER_ID, params.get(PARAM_USER_ID).toString()));
    		return false;
    	}
	}

	private void setInvalidUserResponsePage(final Long userId) {
        setResponsePage(InvalidUser.class, params(InvalidUser.PARAM_USER_ID, userId));
    }

    private Link<?> createBlockActionLink(final Long userId) {
        Link<?> blockActionLink = new Link<Void>("blockActionLink") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
                if ((user.getStatus() == ENABLED)) {
                    userService.setUserStatus(userId, BLOCKED);
                } else if ((user.getStatus() == BLOCKED)) {
                    userService.setUserStatus(userId, ENABLED);
                }
                
                loadFormData(userId);
            }
        };
        
        blockActionLink.add(new Label("blockActionLabel", new Model<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public String getObject() {
                UserStatus status = getUser().getStatus();
                if ((status == ENABLED)) {
                    return "Block user";
                } else if ((status == BLOCKED)) {
                    return "Unblock user";
                } 
                throw new IllegalStateException("user should not be visible here! Id = " + userId);
            }
        }));
        return blockActionLink;
    }

    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    private void loadFormData(Long userId) {
        user = userService.getUserById(userId);
    }
    
    @Override
    public String getPageTitle() {
        return "Edit user: " + user.getUserName() + " (" + user.getUserId() + ")";
    }
    
    private ListView<String[]> createAttributesListView() {
        Model<ArrayList<String[]>> attributeModel = new Model<ArrayList<String[]>>() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public ArrayList<String[]> getObject() {
                ArrayList<String[]> keyValueList = new ArrayList<String[]>();
                
                if(user.getAttributes() != null){                
	                ArrayList<String> keysSorted = new ArrayList<String>(user.getAttributes().keySet());
	                Collections.sort(keysSorted);
	                for (String key : keysSorted) {
	                	if(key != null) {
	                		keyValueList.add(new String[] {key, user.getAttributes().get(key)});
	                	}
	                }
                }
                return keyValueList;
            }
        };
        
        return new ListView<String[]>("attributes", attributeModel) {
            private static final long serialVersionUID = 1L;

			@Override 
			protected void populateItem(ListItem<String[]> item) {
				String[] keyValue = item.getModelObject();
				if(keyValue[0] != null){
					item.add(new Label("key", keyValue[0]));
                	item.add(new TextField<String>("value", new PropertyModel<String>(EditUser.this, "user.attributes["+ keyValue[0] + "]")));
				}
			}	
        };
    }

	public String getNewAttributeKey() {
		return newAttributeKey;
	}

	public void setNewAttributeKey(String newAttributeKey) {
		this.newAttributeKey = newAttributeKey;
	}

	public String getNewAttributeValue() {
		return newAttributeValue;
	}

	public void setNewAttributeValue(String newAttributeValue) {
		this.newAttributeValue = newAttributeValue;
	}
    
    
}
