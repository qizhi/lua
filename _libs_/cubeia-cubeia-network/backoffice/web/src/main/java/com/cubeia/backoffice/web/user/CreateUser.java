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

import static com.cubeia.backoffice.users.api.dto.CreationStatus.OK;
import static com.cubeia.backoffice.web.util.ParamBuilder.params;

import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backoffice.users.api.dto.CreateUserRequest;
import com.cubeia.backoffice.users.api.dto.CreateUserResponse;
import com.cubeia.backoffice.users.api.dto.Gender;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserInformation;
import com.cubeia.backoffice.users.client.UserServiceClient;
import com.cubeia.backoffice.web.BasePage;

@AuthorizeInstantiation({"SUPER_USER", "USER_ADMIN"})
public class CreateUser extends BasePage {
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(CreateUser.class);

    @SpringBean(name="client.user-service")
    private UserServiceClient userService;
    
    private User user;
    private String password;
    private String passwordConfirm;
    
    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public CreateUser(final PageParameters parameters) {
        resetFormData();
        
        Form<?> userForm = new Form<Void>("userForm") {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit() {
                CreateUserRequest createUserData = new CreateUserRequest(user, password);
                CreateUserResponse createResponse = userService.createUser(createUserData);
                
                if (createResponse.getStatus() != OK) {
                	error("Error creating user: " + createResponse.getStatus().name());
                	return;
                }
                
                User createdUser = createResponse.getUser();
                log.debug("created user id = " + createdUser.getUserId());
                setResponsePage(EditUser.class, params(EditUser.PARAM_USER_ID, createdUser.getUserId()));
            }
        };
        
        CompoundPropertyModel<CreateUser> cpm = new CompoundPropertyModel<CreateUser>(this);
        
        userForm.add(new RequiredTextField<String>("userName", cpm.<String>bind("user.userName")));
        userForm.add(new TextField<String>("externalUserId", cpm.<String>bind("user.externalUserId")));
        userForm.add(new RequiredTextField<Long>("operatorId", cpm.<Long>bind("user.operatorId")));
        
        final RequiredTextField<String> passwordField = 
            new RequiredTextField<String>("password", cpm.<String>bind("password"));
        final RequiredTextField<String> passwordConfirmField = 
            new RequiredTextField<String>("passwordConfirm", cpm.<String>bind("passwordConfirm"));
        userForm.add(passwordField);
        userForm.add(passwordConfirmField);
        userForm.add(new EqualPasswordInputValidator(passwordField, passwordConfirmField));      
         
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
        
        add(userForm);
        add(new FeedbackPanel("feedback"));
    }

    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    private void resetFormData() {
        user = new User();
        user.setUserInformation(new UserInformation());
        setPassword(null);
        setPasswordConfirm(null);
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getPasswordConfirm() {
        return passwordConfirm;
    }
    
    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
    
    @Override
    public String getPageTitle() {
        return "Create user";
    }
}
