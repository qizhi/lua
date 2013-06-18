/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
 *
 * This program is licensed under a Firebase Enterprise Edition
 * License. You should have received a copy of the Firebase Enterprise
 * Edition License along with this program. If not, contact info@cubeia.com.
 */
package com.cubeia.firebase.service.persistence;

import java.io.File;

/**
 * Wrapper for a datasource definition file.
 * 
 * @author Fredrik
 *
 */
public class DatasourceDefinition {
	
	private String name;
	
	private File definition;

	
	public DatasourceDefinition(String name, File definition) {
		super();
		this.name = name;
		this.definition = definition;
	}
	
	public String toString() {
		return name;
	}
	
	/**
	 * @return the definition
	 */
	public File getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(File definition) {
		this.definition = definition;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	
	
}
