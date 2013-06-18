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
package com.cubeia.backoffice.users.phonelookup.strategies;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.cubeia.backoffice.users.entity.UserInformation;
import com.cubeia.backoffice.users.phonelookup.LookupStrategy;

/**
 * Models a single lookup strategy. NOT THREAD SAFE, use a new instance for
 * every lookup bitte!
 * 
 * Icky sticky String parsing! HTML DOM's are for n00bs...
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class SweEniro implements LookupStrategy {

	private static String BASE_URL = "http://www.eniro.se/query?geo_area=&what=all&lang=&ax=&search_word=";
	
	private UserInformation user = new UserInformation();

	private String number;
	
	@Override
	public UserInformation lookup(String number) {
		this.number = number;
		parseHtml();
		return user;
	}
	
	
	private void parseHtml() {
		String html = readHTML();
		parseNames(html);
		parseAddress(html);
	}

	private void parseNames(String html) {
		try {
			String match1 = "<span>1. ";
			String match2 = "h4><span><a href=\"http://personer.eniro.se";
			int start = html.indexOf(match1);
			if (start < 1) {
				start = html.indexOf(match2);
			}
				
			if (start > 0) {
				String firstUser = html.substring(start);
				String userLine = firstUser.substring(0, firstUser.indexOf("</a>"));
				String userName = userLine.substring(11+userLine.indexOf("geo_area=\">")).trim();
				String decoded = StringEscapeUtils.unescapeHtml(userName);
				String[] persons = decoded.split("&");
				if (persons.length > 0) {
					String person = persons[0];
					String firstName = person.substring(0, person.lastIndexOf(" ")).trim();
					String lastName = person.substring(person.lastIndexOf(" ")).trim();
					user.setFirstName(firstName);
					user.setLastName(lastName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p class="adr">
	 *   <span class="street-address">Stadsgarden 10 </span>
     *   <span class="postal-code">116 45</span>
     *   <span class="locality">STOCKHOLM</span>
     * </p>
     *
	 * @param html
	 */
	private void parseAddress(String html) {
		String streetTag = "street-address\">";
		String street = html.substring(streetTag.length()+html.indexOf(streetTag));
		street = street.substring(0, street.indexOf("</span>")).trim();
		
		String zipTag = "postal-code\">";
		String zipcode = html.substring(zipTag.length()+html.indexOf(zipTag));
		zipcode = zipcode.substring(0, zipcode.indexOf("</span>")).trim();
		zipcode = StringUtils.remove(zipcode, " ");
		
		String cityTag = "locality\">";
		String city = html.substring(cityTag.length()+html.indexOf(cityTag));
		city = city.substring(0, city.indexOf("</span>")).trim();
		
		user.setBillingAddress(street);
		user.setZipcode(zipcode);
		user.setCity(city);
		
	}
	
	private String readHTML() {
		StringBuffer html = new StringBuffer();
		try {
			URL url = new URL(BASE_URL+number);
	        URLConnection connection = url.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String inputLine;
	        
	        while ((inputLine = in.readLine()) != null) {
	            html.append(inputLine);
	        }
	        in.close();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		return html.toString();
	}
}
