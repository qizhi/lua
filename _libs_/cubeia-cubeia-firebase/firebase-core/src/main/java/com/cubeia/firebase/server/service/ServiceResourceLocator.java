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
package com.cubeia.firebase.server.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.ResourceLocator;

public class ServiceResourceLocator implements ResourceLocator {

	private final ServiceArchive arch;

	public ServiceResourceLocator(ServiceArchive arch) {
		Arguments.notNull(arch, "archive");
		this.arch = arch;
	}

	public InputStream openResource(String path) throws IOException {
		Arguments.notNull(path, "path");
		ServiceArchiveLayout layout = arch.getLayout();
		File file = layout.getResource(arch, path);
		if(file != null && file.isFile()) return new FileInputStream(file);
		else return null;
	}
}
