package com.cubeia.maven.plugin.firebase;

import java.io.File;

import junit.framework.TestCase;

public class LogFileUtilTest extends TestCase {

	public void testRead() throws Exception {
		File root = new File("test");
		FirebaseDirectory d = new FirebaseDirectory("1.0", root);
		String xml = new LogFileUtil(null, d).getLog4jConfig();
		File test = new File(root, File.separator + "logs" + File.separator + "system.log");
		// for windows machines, replace all backslashes
		String logFileName = test.getAbsolutePath().replace("\\", "/");
		assertTrue(xml.contains(logFileName));
	}
}
