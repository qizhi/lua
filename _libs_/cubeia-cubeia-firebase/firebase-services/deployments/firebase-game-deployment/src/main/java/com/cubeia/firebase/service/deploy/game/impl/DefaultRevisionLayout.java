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
package com.cubeia.firebase.service.deploy.game.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.deployment.game.GameDeploymentLayout;
import com.cubeia.firebase.server.deployment.game.GameRevision;
import com.cubeia.firebase.server.util.JarFileFilter;

/*
 * Default GAR layout:
 * 
 * ./*.jar
 * ./GAME-INF/classes/**
 * ./GAME-INF/lib/*.jar
 * ./GAME-INF/game.xml
 */
public class DefaultRevisionLayout implements GameDeploymentLayout {

	private static final String LIB_FOLDER = "GAME-INF" + File.separator + "lib";
	private static final String CLASS_FOLDER = "GAME-INF" + File.separator + "classes";
	private static final String GAME_FILE = "GAME-INF" + File.separator + "game.xml";
	
	private static final DefaultRevisionLayout LAY = new DefaultRevisionLayout();
	
	public static GameDeploymentLayout getInstance() {
		return LAY;
	}

	private DefaultRevisionLayout() { }

	public File[] getGameLibraries(GameRevision rev) {
		return getJars(rev.getResource().getExplodedBase());
	}

	public File getOpenClassFolder(GameRevision rev) {
		return new File(rev.getResource().getExplodedBase(), CLASS_FOLDER);
	}

	public File[] getUtilityLibraries(GameRevision rev) {
		return getJars(new File(rev.getResource().getExplodedBase(), LIB_FOLDER));
	}
	
	public GameDefinition getGameDefinition(GameRevision rev) throws IOException {
		return loadGameDefinition(new File(rev.getResource().getExplodedBase(), GAME_FILE));
	}
	
	public File getResource(GameRevision rev, String path) {
		Arguments.notNull(path, "path");
		if(path.startsWith("/")) path = path.substring(1);
		File file = new File(rev.getResource().getExplodedBase(), path);
		if(file.exists()) return file;
		else return null;
	}
	
	
	
	// --- PRIVATE METHODS ---- //

	private GameDefinition loadGameDefinition(File file) throws IOException {
		// TODO: Different exception, the validation message must be understandable for end users /LJN
		try {
			FileReader reader = new FileReader(file);
			GameDefinition game = (GameDefinition)Unmarshaller.unmarshal(GameDefinition.class, reader);
			return game;
		} catch (ValidationException e) {
			throw new IOException(e.getLocalizedMessage());
		}catch (MarshalException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}
	
	private File[] getJars(File base) {
		return base.listFiles(new JarFileFilter());
	}
}
