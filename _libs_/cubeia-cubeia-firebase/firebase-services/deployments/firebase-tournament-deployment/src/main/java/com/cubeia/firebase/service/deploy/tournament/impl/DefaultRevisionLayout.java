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
package com.cubeia.firebase.service.deploy.tournament.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

import com.cubeia.firebase.api.mtt.TournamentDefinition;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.deployment.mtt.TournamentDeploymentLayout;
import com.cubeia.firebase.server.deployment.mtt.TournamentRevision;
import com.cubeia.firebase.server.util.JarFileFilter;

/*
 * Default TAR layout:
 * 
 * ./*.jar
 * ./META-INF/classes/**
 * ./META-INF/lib/*.jar
 * ./META-INF/tournament.xml
 */
public class DefaultRevisionLayout implements TournamentDeploymentLayout {

	private static final String LIB_FOLDER = "META-INF" + File.separator + "lib";
	private static final String CLASS_FOLDER = "META-INF" + File.separator + "classes";
	private static final String DESCRIPTOR = "META-INF" + File.separator + "tournament.xml";
	
	private static final DefaultRevisionLayout LAY = new DefaultRevisionLayout();
	
	public static TournamentDeploymentLayout getInstance() {
		return LAY;
	}

	private DefaultRevisionLayout() { }

	public File[] getGameLibraries(TournamentRevision rev) {
		return getJars(rev.getResource().getExplodedBase());
	}

	public File getOpenClassFolder(TournamentRevision rev) {
		return new File(rev.getResource().getExplodedBase(), CLASS_FOLDER);
	}

	public File[] getUtilityLibraries(TournamentRevision rev) {
		return getJars(new File(rev.getResource().getExplodedBase(), LIB_FOLDER));
	}
	
	public TournamentDefinition getTournamentDefinition(TournamentRevision rev) throws IOException {
		return loadTournamentDefinition(new File(rev.getResource().getExplodedBase(), DESCRIPTOR));
	}
	
	public File getResource(TournamentRevision rev, String path) {
		Arguments.notNull(path, "path");
		if(path.startsWith("/")) path = path.substring(1);
		File file = new File(rev.getResource().getExplodedBase(), path);
		if(file.exists()) return file;
		else return null;
	}
	
	
	
	// --- PRIVATE METHODS ---- //

	private TournamentDefinition loadTournamentDefinition(File file) throws IOException {
		// TODO: Different exception, the validation message must be understandable for end users /LJN
		try {
			FileReader reader = new FileReader(file);
			TournamentDefinition def = (TournamentDefinition)Unmarshaller.unmarshal(TournamentDefinition.class, reader);
			return def;
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
