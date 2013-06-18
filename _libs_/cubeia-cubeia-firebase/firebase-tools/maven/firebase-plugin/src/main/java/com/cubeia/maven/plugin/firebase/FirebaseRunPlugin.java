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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.cubeia.firebase.api.util.TaskRepeater;

/**
 * @goal run
 * @requiresDependencyResolution runtime
 * @requiresProject
 */
public class FirebaseRunPlugin extends AbstractMojo {
	
	private static final String DIST_CLASSIFIER_PART = "release";
    private static final String DIST_TYPE = "zip";
    private static final String MY_GROUP_ID = "com.cubeia.tools";
	private static final String MY_ARTIFACT_ID = "firebase-maven-plugin";

	private static final String DIST_GROUP_ID = "com.cubeia.firebase";
	private static final String DIST_ARTIFACT_ID = "firebase";
	
	private static final String SERVER_CLASS = "com.game.server.bootstrap.Server";
	private static final String SERVER_THREAD_NAME = "Server instance main thread";
	
	private static final String BIND_ADDRESS_PROP = "client-bind-address";
	private static final String WEB_DIR_PROP = "static-web-directory";
	
	
	// --- MAGICAL MAVEN COMPONENTS --- //
	
    /**
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String finalName;
	
	/**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * @component
     */
    private ArtifactMetadataSource metadataSource;
    
    /**
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    private List<?> remoteRepositories;

	/**
	 * @readonly
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;
	
	/**
	 * @parameter expression="${project.build.directory}"
	 */
	private File outputDir;
	
	/** @component */
	private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;
	 
	/** @component */
	private org.apache.maven.artifact.resolver.ArtifactResolver resolver;
	 
	// --- CONFIGURATION --- ///
	/**
	 * Used to override the Firebase version to run
	 * @parameter 
	 */
	private String firebaseVersion;
	
	/**
	 * @parameter default-value="true"
	 */
	private boolean deleteOnExit;
	
	/**
	 * @parameter default-value="true"
	 */
	private boolean deleteOnStart;
	
	/**
	 * If true include Firebase archive dependencies with scope provided.
	 * @parameter default-value="true"
	 */
	private boolean includeProvidedArchives;
	
	/**
	 * @parameter 
	 */
	private String serverBindAddress;
	
	/**
	 * If true copy all files from {@link #overlaySourceDirectory} to the Firebase base directory.
	 * @parameter default-value="true"
	 */
	private boolean overlaysEnabled;
	
	/**
	 * Source files for Firebase runtime overlays.
	 * @parameter default-value="${basedir}/src/test/resources/firebase"
	 */
	private File overlaySourceDirectory;
	
	/**
	 * Overwrite the standard Firebase log4j configuration with a plugin
	 * specific which modified the log directory and logs non-firebase 
	 * at DEBUG level to standard out. 
	 * 
	 * @parameter default-value="true"
	 */
	private boolean modifyLog4jConfiguration;
	
	
	
	// --- PLUGIN METHOD --- //
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Artifact artifact = findDistributionArtifact();
		File zipFile = getArtifactFile(artifact);
		File tempDir = findCreateTmpFirebaseDir();
		unzipDirsToTmpDir(zipFile, tempDir);
		File firebaseDir = getFirebaseDir(tempDir, artifact);
		FirebaseDirectory dir = new FirebaseDirectory(artifact.getVersion(), firebaseDir);
		getLog().info("Instance directory: " + dir.firebaseDirectory.getAbsolutePath());
		copyProjectArtifactToDeploy(firebaseDir);
		copyProjectDependenciesToDeploy(firebaseDir);
		modifyLogConfiguration(dir);
		copyOverlayToFirebaseDir(firebaseDir);
		modifyClusterConfiguration(dir);
		modifyServerConfiguration(dir);
		doLaunch(dir);
	}

	protected void doLaunch(FirebaseDirectory dir) throws MojoExecutionException {
		ClassLoader loader = createClassLoader(dir);
		try {
			Class<?> serverClass = loader.loadClass(SERVER_CLASS);
			String[] cmds = createCommandLine(dir);
			getLog().info("Attempting to start Firebase server.");
			Method method = serverClass.getMethod("main", cmds.getClass());
			method.invoke(null, (Object)cmds);
		} catch (ClassNotFoundException e) {
			throw new MojoExecutionException("Server class '" + SERVER_CLASS + "' not found", e);
		} catch (Exception e) {
			throw new MojoExecutionException("Reflection error", e);
		}
		/*
		 * Major hack here, we'll find the server main thread and wait for it...
		 */
		Thread th = findServerMainThread();
		if(th == null) throw new MojoExecutionException("Firebase Server sub-process did not start correctly; Please check the logs (" + new File(dir.firebaseDirectory, "logs/") + ").");
		else {
			try {
				th.join();
			} catch (InterruptedException e) { }
		}
	}


	
	// --- PRIVATE METHODS --- //
	
    private void modifyLogConfiguration(FirebaseDirectory dir) throws MojoExecutionException {
    	if(modifyLog4jConfiguration) {
    		try {
    			File newFile = new File(dir.confDirectory, "log4j.xml");
    			String xml = new LogFileUtil(getLog(), dir).getLog4jConfig();
    			FileUtils.writeStringToFile(newFile, xml);
    		} catch(IOException e) {
    			throw new MojoExecutionException("Failed to overwrite log4j configuration", e);
    		}
    	} else {
    		getLog().info("Using standard Cubeia Firebase log4j configuration.");
    	}
	}
	
	/*
	 * Modify the server config to a new bind address, if configured
	 */
	private void modifyServerConfiguration(FirebaseDirectory dir) throws MojoExecutionException {
		if(serverBindAddress != null) {
			File conf = new File(dir.confDirectory, "server.props");
			try {
				InputStream in = new FileInputStream(conf);
				Properties p = new Properties();
				p.load(in);
				p.setProperty(BIND_ADDRESS_PROP, serverBindAddress);
				in.close();
				OutputStream out = new FileOutputStream(conf);
				p.store(out, "Properties modified by firebase:run plugin");
				out.close();
			} catch(IOException e) {
				throw new MojoExecutionException("Failed to modify server configuration", e);
			}
		}
	}
	
	/*
	 * Modify the cluster config to add new static web directory
	 */
	private void modifyClusterConfiguration(FirebaseDirectory dir) throws MojoExecutionException {
		File conf = new File(dir.confDirectory, "cluster.props");
		try {
			InputStream in = new FileInputStream(conf);
			Properties p = new Properties();
			p.load(in);
			p.setProperty(WEB_DIR_PROP, new File(dir.gameDirectory, "web").getAbsolutePath());
			in.close();
			OutputStream out = new FileOutputStream(conf);
			p.store(out, "Properties modified by firebase:run plugin");
			out.close();
		} catch(IOException e) {
			throw new MojoExecutionException("Failed to modify server configuration", e);
		}
	}
	
	/*
	 * Ohhhh, ugly method...
	 */
	private Thread findServerMainThread() {
		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
		for (Thread th : map.keySet()) {
			if(th.getName().equals(SERVER_THREAD_NAME)) {
				return th;
			}
		}
		return null;
	}
	
	/*
	 * Create the arguments for the server process
	 */
	private String[] createCommandLine(FirebaseDirectory dir) {
		return new String[] {
				"-n", "singleton",
				"-c", dir.confDirectory.getAbsolutePath(),
				"-l", dir.libDirectory.getAbsolutePath(),
				"-g", dir.gameDirectory.getAbsolutePath(),
				"-w", dir.workDirectory.getAbsolutePath(),
				"-i", "mavenServer1"
		};
	}
	
	/*
	 * Create a URL class loader for all common libs+boostrap
	 */
	@SuppressWarnings("unchecked")
	private ClassLoader createClassLoader(FirebaseDirectory dir) throws MojoExecutionException {
		List<File> list = new LinkedList<File>();
		list.add(dir.binDirectory);
		list.add(new File(dir.binDirectory, "firebase-bootstrap.jar"));
		list.addAll(FileUtils.listFiles(dir.commonLibDirectory, new String[] { "jar" }, false));
		list.add(dir.firebaseDirectory);
		list.add(dir.confDirectory);
		File[] arr = new File[list.size()];
		list.toArray(arr);
		try {
			URL[] urls = FileUtils.toURLs(arr);
			return new URLClassLoader(urls);
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to create URL for class path", e);
		}
	}
	
	/*
	 * Check that the start script exists and is executable
	 */
	/*private void verifyStartScript(File firebaseDir) throws MojoFailureException {
		File file = new File(firebaseDir, startScript);
		if(!file.exists()) {
			throw new MojoFailureException("Start script '" + file + "' not found");
		}
		if(!file.canExecute() && !file.setExecutable(true)) {
			throw new MojoFailureException("Start script '" + file + "' not executable");
		}
	}*/
	
	private void copyOverlayToFirebaseDir(File firebaseDir) throws MojoFailureException, MojoExecutionException {
        if (overlaysEnabled) {
            getLog().info("Firebase overlays enabled, source: " + overlaySourceDirectory + ", target directory: "+firebaseDir);
            try {
                FileUtils.copyDirectory(overlaySourceDirectory, firebaseDir);
            } catch (IOException e) {
                // throw new MojoExecutionException("Failed to copy overlay files ["+overlaySourceDirectory+"] to Firebase base directory ["+firebaseDir+"]", e);
            	getLog().warn("Failed to copy overlay files ["+overlaySourceDirectory+"] to Firebase base directory ["+firebaseDir+"]. Firebase overlay is ignored.");
            }
        } else {
            getLog().debug("Firebase overlays disabled");
        }
    }
	
	/*
	 * Find any relevant dependencies ('gar', 'tar' or 'sar') and copy
	 * them to the deploy directory
	 */
    @SuppressWarnings("unchecked")
	private void copyProjectDependenciesToDeploy(File firebaseDir) throws MojoFailureException, MojoExecutionException {
		// Check provided services
		Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();
		for (Artifact a : dependencyArtifacts) {
			if(isFirebaseArchive(a) && isScopeProvided(a)) {
				if(includeProvidedArchives) {
					getLog().info("Include provided archive: "+a);
					copyArtifactToDeploy(firebaseDir, a);
				} else {
					getLog().info("Excluding provided archive: "+a);
				}
			}
		}
	}
    
    private boolean isScopeProvided(Artifact a) {
		return a.getScope().equals(Artifact.SCOPE_PROVIDED);
	}

	/*
     * Check the archive type...
     */
    private boolean isFirebaseArchive(Artifact a) {
		String tmp = a.getType();
		if(tmp == null) {
			return false;
		} else if (tmp.equals("firebase-gar") || 
				   tmp.equals("firebase-sar") ||
				   tmp.equals("firebase-uar") ||
				   tmp.equals("firebase-tar")) {
			
			getLog().debug("Including dependency: " + a);
			return true;
		} else {
			getLog().debug("Ingoring dependency of type: " + tmp);
			return false;
		}
	}


	/*
     * Get hold of the output artifact file
     */
	private File getProjectArtifactFile() throws MojoFailureException {
       Artifact a = project.getArtifact();
       /*if(a.getType() != null && a.getType().equals("pom") && hasUARChild()) {
    	   // Get UAR artifact and resolve...
       } else {*/
	       String extension = findExtension(a.getType());
	       String name = finalName + extension;
	       File file = new File(outputDir, name);
	       if(!file.exists()) {
	    	   throw new MojoFailureException("Project artifact '" + name + "' not found; Please run 'package' at least once");
	       }
	       return file;
       // }
    }
	
    /*private boolean hasUARChild() {
	    // Find modules, and check for UAR project...
	}*/

	/*
     * Try to get the correct extension for the artifact type...
     */
	private String findExtension(String type) throws MojoFailureException {
		if(type != null && type.equals("firebase-gar")) {
			return ".gar";
		} else if(type != null && type.equals("firebase-sar")) {
			return ".sar";
		} else if(type != null && type.equals("firebase-tar")) {
			return ".tar";
		} else if(type != null && type.equals("firebase-uar")) {
			return ".uar";
		} else {
			throw new MojoFailureException("This plugin can only be run on projects of type 'firebase-gar', 'firebase-sar', 'firebase-uar', or 'firebase-tar'; Unknown type: " + type);
		}
	}


	/*
	 * Get hold of the artifact for this project, and copy it to the
	 * firebase deploy directory.
	 */
	private void copyProjectArtifactToDeploy(File firebaseDir) throws MojoFailureException, MojoExecutionException {
		File artifact = getProjectArtifactFile();
		File target = ensureFirebaseDeployLib(firebaseDir);
		try {
			FileUtils.copyFileToDirectory(artifact, target);
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to copy project artifact to deploy directory", e);
		}
	}
	
	
	/*
	 * Get hold of the artifact for this project, and copy it to the
	 * firebase deploy directory.
	 */
	private void copyArtifactToDeploy(File firebaseDir, Artifact artifact) throws MojoFailureException, MojoExecutionException {
		if(artifact.getFile() == null) throw new MojoFailureException("Artifact '" + artifact + "' is not resolved");
		File target = ensureFirebaseDeployLib(firebaseDir);
		try {
			FileUtils.copyFileToDirectory(artifact.getFile(), target);
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to copy project artifact to deploy directory", e);
		}
	}
	
	
	/*
	 * Get the deploy lib (and make sure it exists)
	 */
	private File ensureFirebaseDeployLib(File firebaseDir) throws MojoExecutionException {
		File file = new File(firebaseDir, "game/deploy");
		if(!file.exists() && !file.mkdirs()) {
			throw new MojoExecutionException("Failed to create directory '" + file + "'");
		}
		return file;
	}


	/*
	 * Find the final root of the unzipped distribution using the 
	 * artifact version number...
	 */
	private File getFirebaseDir(File tempDir, Artifact artifact) throws MojoExecutionException {
		File file = new File(tempDir, "firebase-" + artifact.getVersion());
		if(!file.exists()) {
			throw new MojoExecutionException("Internal error, root directory '" + file + "' does not exist!");
		}
		return file;
	}
	
	/*
	 * Get file for artifact, and make sure we can read it
	 */
	private File getArtifactFile(Artifact artifact) throws MojoExecutionException {
		File file = artifact.getFile();
		if(!file.exists() || !file.canRead()) {
			throw new MojoExecutionException("Cannot read artifact file '" + file + "'");
		}
		return file;
	}


	/*
	 * Open the distribution in the work directory
	 */
	private void unzipDirsToTmpDir(File zipFile, File tempDir) throws MojoExecutionException {
		try {
			ZipFile zip = new ZipFile(zipFile);
			explode(zip, tempDir);
		} catch(IOException e) {
			throw new MojoExecutionException("Failed to unzip distribution", e);
		}
	}
	
	/*
	 * Explode zip file to destination directory
	 */
	private void explode(ZipFile file, File dir) throws IOException {
		for(Enumeration<? extends ZipEntry> en = file.entries(); en.hasMoreElements(); ) {
			ZipEntry entry = en.nextElement();
			File next = new File(dir, entry.getName());
			if(entry.isDirectory()) {
				next.mkdirs();
			} else {
				next.createNewFile();
				if (next.getParentFile() != null) {
					next.getParentFile().mkdirs();
				}
				InputStream in = file.getInputStream(entry);
				OutputStream out = new FileOutputStream(next);
				try {
					IOUtils.copy(in, out);
				} finally {
					IOUtils.closeQuietly(out);
					IOUtils.closeQuietly(in);
				}
			}
		}
	}
	
	/*
	 * Create a new directory inside the build target dir, the use for
	 * running firebase.
	 */
	private File findCreateTmpFirebaseDir() throws MojoExecutionException {
		if(!outputDir.exists()) throw new MojoExecutionException("Output directory not found; Please run 'package' at least once");
		final File dir = new File(outputDir, "firebase-run");
		TaskRepeater repeat = new Repeater("directory create/delete");
		if(dir.exists() && deleteOnStart) {
			doStartTmpDirCleanup(dir, repeat);
		}
		if(!dir.exists()) {
			doStartTmpDirCreation(dir, repeat);
		}
		getLog().info("Runtime directory '" + dir + "'; deleteOnExit: " + deleteOnExit);
		if(deleteOnExit) {
			dir.deleteOnExit();
			return dir;
		}
		return dir;
	}

	private void doStartTmpDirCreation(final File dir, TaskRepeater repeat) throws MojoExecutionException {
		getLog().debug("Runtime directory '" + dir + "' does not exists, will be created");
		boolean b = repeat.safeExecute(new Callable<Boolean>() {
			
			@Override
			public Boolean call() throws Exception {
				return dir.mkdir();
			}
		});
		if(!b) {
			throw new MojoExecutionException("Failed to create dir '" + dir + "'");
		}
	}

	private void doStartTmpDirCleanup(final File dir, TaskRepeater repeat) throws MojoExecutionException {
		getLog().warn("Runtime directory '" + dir + "' already exists, its content will be deleted");
		boolean b = repeat.safeExecute(new Callable<Boolean>() {
			
			@Override
			public Boolean call() throws Exception {
				return FileUtils.deleteQuietly(dir);
			}
		});
		if(!b) {
			throw new MojoExecutionException("Failed to delete dir '" + dir + "'");
		}
	}
	
	/*
	 * This method attempts to find the Firebase distribution ZIP file. This
	 * should be done like this:
	 * 
	 *  1) If the firebase version has been overridden, try to resolve that
	 *  2) Else, find the com.cubeia.tools:firebase-maven-plugin artifact (our own)
	 *  3) Resolve the transitive dependencies of said artifact
	 *  4) Return the one matching
	 *  
	 * Easy, eh?
	 */
	private Artifact findDistributionArtifact() throws MojoExecutionException {
		Artifact artifact;

		try {
			// check if firebase version has been overridden and try to resolve it
			if ( firebaseVersion != null ) {
				getLog().warn("Firebase version overridden, version to run: " + firebaseVersion);
				artifact = artifactFactory.createArtifactWithClassifier("com.cubeia.firebase", "firebase", firebaseVersion, DIST_TYPE, DIST_CLASSIFIER_PART);
				resolver.resolve( artifact, remoteRepositories, localRepository );
			} else {
				artifact = findOwnArtifact();
			}
		
			return findDistributionArtifact(artifact);
			
		} catch(AbstractArtifactResolutionException e) {
			throw new MojoExecutionException("Failed to resolve dependencies", e);
		}
	}
	
	
	/*
	 * Given the plugin artifact, get the transitive dependencies and locate the 
	 * distribution zip among them.
	 */
	private Artifact findDistributionArtifact(Artifact mySelf) throws ArtifactResolutionException, ArtifactNotFoundException, MojoExecutionException {
		Collection<Artifact> set = getTransitive(mySelf);
		Artifact zip = null;
		for (Artifact a : set) {
			if (a.getGroupId().equals(DIST_GROUP_ID)  &&  a.getArtifactId().equals(DIST_ARTIFACT_ID)  &&  a.getType().equals(DIST_TYPE)) {
			    if (a.getClassifier().contains(DIST_CLASSIFIER_PART)) {
    				zip = a;
    				break;
			    }
			}
		}
		if(zip == null) {
			throw new MojoExecutionException("Failed to resolve Firebase distribution archive at: " + DIST_GROUP_ID + ":" + DIST_ARTIFACT_ID + ":" 
			     + mySelf.getVersion() + ":*" + DIST_CLASSIFIER_PART + "*:" + DIST_TYPE);
		} else {
			return zip;
		}
	}


	/*
	 * Try to find the artifact matching my own group and artifact id.
	 */
	@SuppressWarnings("unchecked")
	private Artifact findOwnArtifact() throws MojoExecutionException {
		Collection<Artifact> set = project.getPluginArtifacts();
		Artifact ours = null;
		for (Artifact a : set) {
			if(a.getGroupId().equals(MY_GROUP_ID) && a.getArtifactId().equals(MY_ARTIFACT_ID)) {
				ours = a;
				break;
			}
		}
		if(ours == null) {
			throw new MojoExecutionException("Failed to resolve Firebase maven plugin artifect: " + MY_GROUP_ID + ":" + MY_ARTIFACT_ID);
		} else {
			return ours;
		}
	}


	/*
	 * This method magically resolves all dependencies for an artifact, which means
	 * that the returned artifacts all have a file location in the local repository.
	 */
	@SuppressWarnings("unchecked")
	private Collection<Artifact> getTransitive(Artifact ours) throws ArtifactResolutionException, ArtifactNotFoundException {
		ArtifactResolutionResult result =  artifactResolver.resolveTransitively(
												Collections.singleton(ours), 
												project.getArtifact(), 
                                                Collections.EMPTY_MAP, 
                                                localRepository, 
                                                remoteRepositories, 
                                                metadataSource, null, Collections.EMPTY_LIST);
		return result.getArtifacts();
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class Repeater extends TaskRepeater {

		public Repeater(String name) {
			super(name, 10, 500);
		}
		
		@Override
		protected void debug(String msg) {
			getLog().debug(msg);
		}
		
		@Override
		protected void error(String msg, Exception e) {
			getLog().error(msg, e);
		}
		
		@Override
		protected void warn(String msg) {
			getLog().warn(msg);
		}
	}
}
