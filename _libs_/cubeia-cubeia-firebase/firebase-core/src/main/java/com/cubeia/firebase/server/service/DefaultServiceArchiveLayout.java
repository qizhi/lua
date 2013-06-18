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
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cubeia.firebase.api.service.ServiceInfo;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.util.JarFileFilter;
import com.cubeia.util.IoUtil;
import com.cubeia.util.XPaths;

/*
 * ./*.jar
 * ./META-INF/
 * ./META-INF/service.xml
 * ./META-INF/lib/*.jar
 */
public class DefaultServiceArchiveLayout implements ServiceArchiveLayout {
	
	private final Logger log = Logger.getLogger(getClass());

	private static final String SERVICE_FILE = "META-INF" + File.separator + "service.xml";
	//private static final String CLASS_FOLDER = "META-INF" + File.separator + "classes";
	private static final String LIB_FOLDER = "META-INF" + File.separator + "lib";
	
	private static final DefaultServiceArchiveLayout INST = new DefaultServiceArchiveLayout();
	
	public static DefaultServiceArchiveLayout getInstance() {
		return INST;
	}
	
	private DefaultServiceArchiveLayout() { }

	public File[] getLibraryFiles(ServiceArchive arch) {
		Arguments.notNull(arch, "archive");
		List<File> list = new LinkedList<File>();
		File root = arch.getRootFolder();
		addJarsFromFolder(root, list);
		File libs = new File(root, LIB_FOLDER);
		addJarsFromFolder(libs, list);
		/*File file = new File(root, CLASS_FOLDER);
		if(file.exists()) {
			list.add(file);
		}*/
		for (File file : list) {
			log.debug("Service archive '" + arch.getName() + "' adds library file: " + file.getPath());
		}
		return list.toArray(new File[list.size()]);
	}
	 
	public InternalServiceInfo getServiceInfo(ServiceArchive arch) throws IllegalArchiveException, IOException {
		Arguments.notNull(arch, "archive");
		File root = arch.getRootFolder();
		return readInfo(new File(root, SERVICE_FILE), arch);
	}
	
	public File getResource(ServiceArchive arch, String path) {
		Arguments.notNull(arch, "archive");
		if(path.startsWith("/")) path = path.substring(1);
		File root = arch.getRootFolder();
		File test = new File(root, path);
		if(test.exists()) return test;
		else return null;
	}
	
	
	
	// --- PRIVATE METHODS --- //
	
	private InternalServiceInfo readInfo(File file, ServiceArchive arch) throws IllegalArchiveException, IOException {
		if(!file.exists()) throw new IllegalArchiveException("missing archive service descriptor in archive '" + arch.getName() + "'");
		Document doc = readInfoDoc(file);
		try {
			String pid = XPaths.selectString(doc, "/service/public-id/text()");
			if(pid == null) throw new IllegalArchiveException("missing public-id element in descriptor for archive '" + arch.getName() + "'");
			String[] faces = XPaths.selectStrings(doc, "/service/contract/text()");
			if(faces == null) throw new IllegalArchiveException("missing contract elements in descriptor for archive '" + arch.getName() + "'");
			boolean proxy = false;
			String imple = XPaths.selectString(doc, "/service/service/text()");
			if(imple == null || imple.length() == 0) {
				imple = XPaths.selectString(doc, "/service/invocation-handler/text()");
				if(imple != null && imple.length() != 0) {
					proxy = true;
				} else {
					throw new IllegalArchiveException("Missing service element in descriptor for archive '" + arch.getName() + "'");
				}
			}
			String name = XPaths.selectString(doc, "/service/name/text()");
			if(name == null) throw new IllegalArchiveException("missing name element in descriptor for archive '" + arch.getName() + "'");
			String desc = XPaths.selectString(doc, "/service/description/text()");
			String tmp = XPaths.selectString(doc, "/service/@auto-start");
			boolean auto = (tmp != null && tmp.equals("true"));
			tmp = XPaths.selectString(doc, "/service/@is-public");
			boolean pub = (tmp != null && tmp.equals("true"));
			tmp = XPaths.selectString(doc, "/service/@legacy-context-class-loader");
			boolean legacy = (tmp != null && tmp.equals("true"));
			Dependency[] deps = createDependencies(doc, arch);
			ServiceInfo info = createPlainInfo(auto, name, pid, faces, desc);
			PackageSet exp = checkExports(doc, arch);
			for(String face : faces) {
				exp.addResource(face); // Add service contract
			}
			return new InternalServiceInfo(imple, exp, info, deps, pub, proxy, legacy);
		} catch (XPathExpressionException e) {
			throw new IllegalArchiveException("illegal archive descriptor structure for archive '" + arch.getName() + "'", e);
		}	
	}
	
	private Dependency[] createDependencies(Document doc, ServiceArchive arch) throws XPathExpressionException {
		NodeList list = XPaths.selectNodes(doc, "/service/dependencies/child::*/text()");
		List<Dependency> arr = new LinkedList<Dependency>();
		for (int i = 0; i < list.getLength(); i++) {
			Node text = list.item(i);
			Node par = text.getParentNode();
			String tmp = par.getNodeName();
			if(tmp.equals("public-id") || tmp.equals("contract")) {
				arr.add(new Dependency(text.getNodeValue(), tmp.equalsIgnoreCase("contract")));
			}
		}
		return arr.toArray(new Dependency[arr.size()]);
	}

	private PackageSet checkExports(Document doc, ServiceArchive arch) throws XPathExpressionException {
		PackageSetImpl impl = new PackageSetImpl(arch.getName());
		NodeList list = XPaths.selectNodes(doc, "/service/exported/child::node()");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String nodeName = node.getNodeName();
			if(node instanceof Element && nodeName.equals("class") || nodeName.equals("package")) {
				String val = XPaths.selectString(node, "child::text()");
				log.trace("Service archive '" + arch.getName() + "' adds export statement: " + val);
				impl.addResource(val);
			}
		}
		return impl;
	}

	private Document readInfoDoc(File file) throws IOException {
		// FIXME: Illegal states in catch blocks?
		InputStream in = new FileInputStream(file);
		try {
			return XPaths.read(in);
		} catch (SAXException e) {
			throw new IllegalStateException("failed to read service info '" + file + "'", e);
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("failed to read service info '" + file + "'", e);
		} finally {
			IoUtil.safeClose(in);
		}
	}
	
	private void addJarsFromFolder(File dir, List<File> list) {
		File[] files = dir.listFiles(new JarFileFilter());
		if(files != null) {
			for (File file : files) {
				list.add(file);
			}
		}
	}
	
	private ServiceInfo createPlainInfo(final boolean isAutoStart, final String name, final String id, final String[] faces, final String desc) {
		return new ServiceInfo() {
			
			public boolean isAutoStart() {
				return isAutoStart;
			}
			
			public String[] getContractClasses() {
				return faces.clone();
			}
		
			public String getPublicId() {
				return id;
			}
		
			public String getName() {
				return name;
			}
		
			public String getDescription() {
				return desc;
			}
		};
	}
}
