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

package com.cubeia.games.poker.admin.wicket;

import com.cubeia.games.poker.admin.Configuration;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SearchPage extends BasePage {

    private static final String APPLICATION_JSON = "application/json";

    private static final long serialVersionUID = 1L;

    // TODO Add any page properties or variables here

    @SpringBean(name = "webConfig")
    private Configuration config;

    /**
     * Constructor that is invoked when page is invoked without a session.
     *
     * @param parameters Page parameters
     */
    public SearchPage(PageParameters parameters) {
        super(parameters);

        // Builder b = ImmutableSettings.settingsBuilder();
        // Settings s = b.put("compress.default.type", "lzf").build();


        // TODO Fix config
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

        StringValue value = parameters.get("query");
        String[] parts = (value.isEmpty() ? new String[0] : value.toString().split(" "));

        BoolQueryBuilder root = QueryBuilders.boolQuery();

        for (String s : parts) {
            if (s.endsWith("*")) {
                s = s.substring(0, s.length() - 1).toLowerCase();
                root.must(QueryBuilders.prefixQuery("_all", s));
            } else {
                root.must(QueryBuilders.matchQuery("_all", s));
            }
        }

        SearchResponse resp;
        try {
            resp = client.prepareSearch("network").setQuery(root).execute().get();
            List<User> users = new ArrayList<SearchPage.User>();

            for (SearchHit h : resp.getHits().getHits()) {
                System.out.println(">>>>>>>>> ");
                System.out.println(h.sourceAsString());
                System.out.println(">>>>>>>>> ");
                if (h.getType().equals("users")) {
                    users.add(new User(h));
                }
            }

            UserProvider provider = new UserProvider(users);
            UserView view = new UserView("userresults", provider);

            add(view);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        client.close();

    }

    @Override
    public String getPageTitle() {
        return "Search";
    }

    // --- PRIVATE METHODS --- //


    // --- PRIVATE CLASSES --- //

    private static class UserView extends DataView<User> {

        protected UserView(String id, IDataProvider<User> dataProvider) {
            super(id, dataProvider);
        }

        @Override
        protected void populateItem(Item<User> item) {
            User user = item.getModelObject();
            item.setModel(new CompoundPropertyModel<SearchPage.User>(user));
            item.add(new Label("username"));
            item.add(new Label("firstname"));
            item.add(new Label("lastname"));
        }
    }

    private static class UserProvider implements IDataProvider<User> {

        private final List<User> list;

        private UserProvider(List<User> list) {
            this.list = list;
        }

        @Override
        public void detach() {
        }

        @Override
        public Iterator<? extends User> iterator(long first, long count) {
            return list.subList((int)first, (int) (first + count)).iterator();
        }

        @Override
        public long size() {
            return list.size();
        }

        @Override
        public IModel<User> model(User object) {
            return Model.of(object);
        }
    }

    private static class User implements Serializable {

        private static final long serialVersionUID = 1191176946462071998L;

        private String username;
        private String firstname;
        private String lastname;

        @SuppressWarnings("unchecked")
        private User(SearchHit h) {
            username = h.getSource().get("userName").toString();
            // TODO: Check for null
            firstname = ((Map<String, Object>) h.getSource().get("userInformation")).get("firstName").toString();
            lastname = ((Map<String, Object>) h.getSource().get("userInformation")).get("lastName").toString();
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastName) {
            this.lastname = lastName;
        }
    }
}
