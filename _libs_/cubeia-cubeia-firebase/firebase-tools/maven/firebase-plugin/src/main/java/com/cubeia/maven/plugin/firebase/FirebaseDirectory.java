/**
 * Copyright 2009 Cubeia Ltd 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cubeia.maven.plugin.firebase;

import java.io.File;

public class FirebaseDirectory {

	public final String version;
	public final File firebaseDirectory;
	public final File libDirectory;
	public final File commonLibDirectory;
	public final File binDirectory;
	public final File confDirectory;
	public final File workDirectory;
	public final File gameDirectory;

	public FirebaseDirectory(String version, File firebaseDir) {
		this.version = version;
		this.firebaseDirectory = firebaseDir;
		libDirectory = new File(firebaseDir, "lib");
		commonLibDirectory = new File(libDirectory, "common");
		binDirectory = new File(firebaseDir, "bin");
		confDirectory = new File(firebaseDir, "conf");
		gameDirectory = new File(firebaseDir, "game");
		workDirectory = new File(firebaseDir, "work");
	}
}
