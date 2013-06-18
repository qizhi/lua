/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.api.game.activator;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.util.ClassPathConfig;
import com.cubeia.firebase.api.util.ConfigSource;

/**
 * This is a bean implementation of the default activator config
 * file. It contains, the number of seats at the table, the minimum 
 * amount of tables the system should have, the minimum available
 * (ie empty) table, the number of table to increment with, the time 
 * in milliseconds after which a table is considered "old" and available
 * for removal, and the interval (in millis) in which to scan for changes.
 *
 * @author lars.j.nilsson
 */
public class DefaultActivatorConfig {
	
	private static final String DEF_CONFIG_PATH = "com/cubeia/firebase/api/game/activator/defaultActivatorConfig.xml";

	
	// --- INSTANCE MEMBERS --- //
	
	private int seats;
	private int minTables;
	private int minAvailTables;
	private int incrementSize;
	private long tableTimeout;
	private long scanFrequency;
	private long initalDelay;
	
	public DefaultActivatorConfig() throws SystemException { 
		doDefaultInit();
	}

	public DefaultActivatorConfig(ConfigSource source) throws SystemException { 
		if(source == null) doDefaultInit();
		else doInit(source);
	}

	
	// --- ACCESSORS --- //
	
	/**
	 * @return The size of incrementations batches, ie how many tables are created at once
	 */
	public int getIncrementSize() {
		return incrementSize;
	}

	/**
	 * @param incrementSize The size of incrementations batches, ie how many tables are created at once
	 */
	public void setIncrementSize(int incrementSize) {
		this.incrementSize = incrementSize;
	}

	
	/**
	 * @return The minimum of availible tables, after which new tables are created
	 */
	public int getMinAvailTables() {
		return minAvailTables;
	}

	/**
	 * @param minAvailTables The minimum of availible tables, after which new tables are created
	 */
	public void setMinAvailTables(int minAvailTables) {
		this.minAvailTables = minAvailTables;
	}

	
	/**
	 * @return The minimum number of tables that should at all exist in the cluster
	 */
	public int getMinTables() {
		return minTables;
	}

	/**
	 * @param minTables The minimum number of tables that should at all exist in the cluster
	 */
	public void setMinTables(int minTables) {
		this.minTables = minTables;
	}

	
	/**
	 * @return The fixed table seat size
	 */
	public int getSeats() {
		return seats;
	}

	/**
	 * @param seats The fixed table seat size
	 */
	public void setSeats(int seats) {
		this.seats = seats;
	}

	
	/**
	 * @return The max time in millis an empty table should live, -1 for indefinately
	 */
	public long getTableTimeout() {
		return tableTimeout;
	}

	/**
	 * @param tableTimeout The max time in millis an empty table should live, -1 for indefinately
	 */
	public void setTableTimeout(long tableTimeout) {
		this.tableTimeout = tableTimeout;
	}

	
	/**
	 * @return The interval in millis in which the activator should scan the table state, -1 for no scanning
	 */
	public long getScanFrequency() {
		return scanFrequency;
	}

	
	/**
	 * @return Initial delay for the table scanning in millis
	 */
	public long getInitialDelay() {
		return initalDelay;
	}
	
	
	/**
	 * @param scanFrequency The interval in millis in which the activator should scan the table state, -1 for no scanning
	 */
	public void setScanFrequency(long scanFrequency) {
		this.scanFrequency = scanFrequency;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void doDefaultInit() throws SystemException {
		try {
			doInit(new ClassPathConfig(DEF_CONFIG_PATH, DEF_CONFIG_PATH));
		} catch(IOException e) {
			throw new SystemException("Incomplemete packaging, missing default activator config source.", e);
		}
	}
	
	private void doInit(ConfigSource config) throws SystemException {
		Document d = read(config);
		this.incrementSize = selectInt(d, "/activator/tables/increment-size/text()");
		this.minAvailTables = selectInt(d, "/activator/tables/min-available-tables/text()");
		this.minTables = selectInt(d, "/activator/tables/min-tables/text()");
		this.scanFrequency = selectLong(d, "/activator/scan-frequency/text()");
		this.initalDelay = selectLong(d, "/activator/initial-delay/text()");
		this.seats = selectInt(d, "/activator/tables/seats/text()");
		this.tableTimeout = selectLong(d, "/activator/tables/timeout/text()");
	}
	
	private Document read(ConfigSource config) throws SystemException {
		try {
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			InputStream in = config.getInputStream();
			DocumentBuilder builder = fact.newDocumentBuilder();
			return builder.parse(in);
		} catch(Exception e) {
			throw new SystemException("Failed to parse XML from given config source '" + config + "'", e);
		}
	}
	
	private static long selectLong(Node root, String xpath) throws SystemException {
		try {
			String s = selectString(root, xpath);
			if(s == null || s.length() == 0) return -1;
			else return Long.parseLong(s);
		} catch(NumberFormatException e) {
			throw new SystemException("Illegal activator config; Text value at path '" + xpath + "' is not a number.", e);
		}
	}
	
	private static int selectInt(Node root, String xpath) throws SystemException {
		try {
			String s = selectString(root, xpath);
			if(s == null || s.length() == 0) return -1;
			else return Integer.parseInt(s);
		} catch(NumberFormatException e) {
			throw new SystemException("Illegal activator config; Text value at path '" + xpath + "' is not a number.", e);
		}
	}
	
	private static String selectString(Node root, String xpath) throws SystemException {
		XPath path = XPathFactory.newInstance().newXPath();
		try {
			return path.evaluate(xpath, root);
		} catch (XPathExpressionException e) {
			throw new SystemException("Internal XPATH error: " + e.getMessage(), e);
		}
	}
}
