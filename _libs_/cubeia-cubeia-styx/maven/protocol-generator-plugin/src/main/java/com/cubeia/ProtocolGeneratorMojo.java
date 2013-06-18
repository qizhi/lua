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
package com.cubeia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * 
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution
 */
public class ProtocolGeneratorMojo extends AbstractMojo {
    
    /**
     * @parameter
     * @required
     */
    private String language;
    
    /**
     * Protocol definition file. The path is relative to ${basedir}.
     * @parameter
     */
    private String protocol_file;
    
    /**
     * Protocol dependency. In the format:
     * 
     * <pre>
     *     &lt;groupId&gt;:&lt;artifactId&gt;:&lt;fileName&gt;	
     * </pre>
     * 
     * The file will be searched for in the given dependency archive.
     * 
     * @parameter
     */
    private String protocol_dependency;
    
	/**
	 * @parameter expression="${project.build.directory}"
	 */
	private File outputDir;

    /**
     * @parameter
     * @required
     */
    private String package_name;
    
    /**
     * @parameter
     */
    private String javascript_package_name;

    
    /**
     * The path is relative to ${basedir}.
     * @parameter
     * @deprecated
     */
    private String output_dir;
    
    /**
     * Base directory for generated sources. The path is relative to ${basedir}.
     * @parameter default-value="target/jruby-protocol-plugin/generated-sources"
     */
    private String output_base_dir;
    
    /**
     * @parameter default-value="true"
     */
    private boolean append_language_to_output_base_dir = true;
    
    /**
     * @parameter default-value="${project.dependencies}" 
     * @required
     * @readonly
     */
    @SuppressWarnings({ "unused", "rawtypes" })
	private List dependencies;

    /**
     * @parameter
     * @required
     */
    private boolean generate_visitors;

    /**
     * @parameter
     */
    private String version;
    
    /**
     * @parameter default-value="false"
     */
    private boolean fail_on_bad_packet_order;
    
    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    private enum Languages {java, flash, cpp, csharp, cplusplus, protobuf, javascript};
    
    private final String GENERATOR_WRAPPER_SCRIPT = "/code_generator_wrapper.rb";
    
    
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Cubeia protocol code generator plugin called with parameters:");
        getLog().info("  IN language = " + language);
        getLog().info("  IN output_base_dir = " + output_base_dir);
        getLog().info("  IN output_dir (DEPRECATED) = " + output_dir);
        getLog().info("  IN package_name = " + package_name);
        getLog().info("  IN javascript_package_name = " + javascript_package_name);
        getLog().info("  IN protocol_file = " + protocol_file);
        getLog().info("  IN protocol_dependency = " + protocol_dependency);
        getLog().info("  IN generate_visitors = " + generate_visitors);
        getLog().info("  IN fail_on_bad_packet_order = " + fail_on_bad_packet_order);
        getLog().info("  IN version = " + version);
        
        
        if (output_dir != null) {
            getLog().warn("'output_dir' is deprecated and has no effect, use 'output_base_dir instead");
        } 
        
        File outputBaseDir = new File(project.getBasedir() + "/" + output_base_dir);
        File protocolFile = findProtocolFile();
        
        getLog().info("protocol file: " + protocolFile);
        getLog().info("output base directory: " + outputBaseDir);
        
        if (!protocolFile.exists()) {
            throw new MojoFailureException("protocol file not found: " + protocolFile);
        }
        
        // add generated java code to compile path
        project.addCompileSourceRoot(outputBaseDir + "/java");
        
        if (isProtocolFileNewerThanGeneratedCode(protocolFile, outputBaseDir)) {
            if ("all".equals(language)) {
                for (Languages lang : Languages.values()) {
                    generateCode(lang.name(), protocolFile, outputBaseDir, package_name, generate_visitors, version, fail_on_bad_packet_order, javascript_package_name);
                }
            } else {
            	String[] languages = language.split(",");
            	for ( String lang : languages ) {
            		generateCode(lang, protocolFile, outputBaseDir, package_name, generate_visitors, version, fail_on_bad_packet_order, javascript_package_name);
            	}
            }
        } else {
            getLog().info("no changes detected, won't generate code");
        }
    }

    @SuppressWarnings("unchecked")
	private File findProtocolFile() throws MojoFailureException, MojoExecutionException {
		if(protocol_file != null) {
			/*
			 * Prefer a file reference...
			 */
			return new File(project.getBasedir() + "/" + protocol_file);
		} else {
			if(protocol_dependency == null) {
				throw new MojoFailureException("'protocol_file' or 'protocol_dependency' must be specified");
			}
			/*
			 * Split dependency into discreet parts
			 */
			String[] path = protocol_dependency.split(":");
			if(path.length != 3) throw new MojoFailureException("'protocol_dependency' must be in format <groupdId>:<artifactId>:<file>");
			/* 
			 * Iterate through the dependency artifacts and match against
			 * the given protocol dependency.
			 * 
			 * This works as we've resolved the damned dependencies magically
			 * in the parameters and using meta data... *shudder*
			 */
			Set<Artifact> arts = project.getDependencyArtifacts();
			for (Artifact a : arts) {
				if(a.getGroupId().equals(path[0]) && a.getArtifactId().equals(path[1])) {
					// Bingo, now try to get the file...
					return extractDependencyFile(a, path[2]);
				}
			}
			throw new MojoFailureException("Could not find 'protocol_dependency': " + protocol_dependency);
		}
	}

    /*
     * Check the artifact, if file try to unzip, if directory
     * try to access immediately
     */
	private File extractDependencyFile(Artifact a, String path) throws MojoExecutionException {
		File f = a.getFile();
		getLog().info("using resolved 'protocol_dependency': " + f);
		File tempDir = createTmpDir();
		if(f.isFile()) {
			getLog().info("resolved 'protocol_dependency' is a file, will attempt to unzip to work directory");
			unzipToTmpDir(f, tempDir);
			return new File(tempDir, path);
		} else {
			getLog().info("resolved 'protocol_dependency' is a directory, will attempt direct access");
			return new File(f, path);
		}
	}
	
	/*
	 * Try to create a directory fo unzip protocol dependency to
	 */
	private File createTmpDir() throws MojoExecutionException {
		if(!outputDir.exists() && !outputDir.mkdir()) throw new MojoExecutionException("Output directory not found");
		File dir = new File(outputDir, "protocol");
		if(!dir.exists()) {
			dir.mkdir();
		}
		return dir;
	}
	
	/*
	 * Open the distribution in the work directory
	 */
	private void unzipToTmpDir(File zipFile, File tempDir) throws MojoExecutionException {
		try {
			ZipFile zip = new ZipFile(zipFile);
			explode(zip, tempDir);
		} catch(IOException e) {
			throw new MojoExecutionException("Failed to unzip distribution", e);
		}
	}

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

	private void generateCode(
        String lang, 
        File protocolFile, 
        File outputBaseDirectory, 
        String packageName, 
        boolean generateVisitors, 
        String version,
        boolean failOnBadPacketOrder, String javascript_package_name) throws MojoExecutionException, MojoFailureException {
		
		if(append_language_to_output_base_dir) {
			outputBaseDirectory = appendLangToBaseDir(lang, outputBaseDirectory);
			getLog().info("Appended language '" + lang + "' to base dir, new base dir: " + outputBaseDirectory);
        }
        
        ScriptEngineManager factory = new ScriptEngineManager();

        // Create a JRuby engine.
        ScriptEngine engine = factory.getEngineByName("jruby");

        // Evaluate JRuby code from string.
        InputStream scriptIn = getClass().getResourceAsStream(GENERATOR_WRAPPER_SCRIPT);
        if (scriptIn == null) {
            new MojoExecutionException("unable to find code generator script resource: " + GENERATOR_WRAPPER_SCRIPT);
        }
        
        Object[] args = new Object[] {
            protocolFile.getPath(), 
            lang, 
            outputBaseDirectory.getPath(), 
            packageName, 
            generateVisitors ? "true" : null,
            version, 
            failOnBadPacketOrder ? "true" : null,
            javascript_package_name};
        
        InputStreamReader scriptReader = new InputStreamReader(scriptIn);
        
        try {
            engine.eval(scriptReader);
            Invocable invocableEngine = (Invocable) engine;
            invocableEngine.invokeFunction("generate_code", args);
        } catch (ScriptException e) {
            throw new MojoFailureException("code generation error: " + e.toString());
        } catch (NoSuchMethodException e) {
            throw new MojoExecutionException("error calling code generator script: " + e.getMessage());
        }
    }

    private File appendLangToBaseDir(String lang, File outputBaseDirectory) {
		if(lang.equals("csharp")) {
			return new File(outputBaseDirectory, "C#");
		} else {
			return new File(outputBaseDirectory, lang);
		}
	}

	private boolean isProtocolFileNewerThanGeneratedCode(File protocolFile, File outputDir) {
        return protocolFile.lastModified() > outputDir.lastModified();
    }
}
