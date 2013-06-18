package com.cubeia;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

public class ProtocolGeneratorMojoTest extends AbstractMojoTestCase {
    private static final String TEST_CODE_DIR = "target/jruby-protocol-plugin/test-generated-sources";
    private static final String TEST_CODE_FULL_DIR = TEST_CODE_DIR + "/java/com/cubeia/test/protocol";
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();
        
        deleteGeneratedSource();
    }

    public void testMojoGoal() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/mojo-test-pom.xml");
        ProtocolGeneratorMojo mojo = (ProtocolGeneratorMojo) lookupMojo("generate", testPom);
        assertNotNull(mojo);
        setVariableValueToObject(mojo, "project", new MavenProjectStub());
        
        mojo.execute();
        assertTrue(new File(TEST_CODE_FULL_DIR + "/EnumTestPacket.java").exists());
        assertTrue(new File(TEST_CODE_FULL_DIR + "/TypeTestPacket.java").exists());
        assertTrue(new File(TEST_CODE_FULL_DIR + "/ListTestPacket.java").exists());
        assertTrue(new File(TEST_CODE_FULL_DIR + "/DepTestPacket.java").exists());
        assertTrue(new File(TEST_CODE_FULL_DIR + "/DepTestPacket.java").exists());
        assertTrue(new File(TEST_CODE_FULL_DIR + "/ProtocolObjectFactory.java").exists());
        assertTrue(new File(TEST_CODE_FULL_DIR + "/PacketVisitor.java").exists());
        assertTrue(new File(TEST_CODE_FULL_DIR + "/Enums.java").exists());
        
        // execute again, no code should be generated (already up to date)
        File f = new File(TEST_CODE_FULL_DIR + "/EnumTestPacket.java");        
        long lastModified = f.lastModified();
        mojo.execute();
        assertEquals(lastModified, f.lastModified());
    }
    
    public void testNormalizedEnumList() throws Exception {
        /*
         * Create and execute Mojo
         */
    	createAndExecuteJsMojo();
        /*
         * Create JavaScript engine
         */
        ScriptEngine engine = createJsEngine();
        /*
         * Concatenate header, enum and list of enum
         */
        String s = concatJsFiles(
        				"javascript/header/header.js",
        				"javascript/script/EnumTypeEnum.js",
        				"javascript/script/ListOfEnums.js"
        			);
        /*
         * Concatenate test method, with explanations below...
         */
        s += "function testList() { " +
        		    "var list = new testpackage.ListOfEnums(); " + // create list struct
        		    "list.l.push(testpackage.EnumTypeEnum.INT); " + // push one INT enum to list
        		    "var norm = list.getNormalizedObject(); " + // normalize
        		    "return norm.details[\"l\"][0]; " + // return normalized member at 0
        		"}";
        /*
         * Evaluate and make sure the test method returns the string
         * version of the INT enum...
         */
        engine.eval(s);
        Object o = ((Invocable)engine).invokeFunction("testList");
        assertEquals("INT", o);
    }
    
    public void testNormalizedIntList() throws Exception {
        /*
         * Create and execute Mojo
         */
    	createAndExecuteJsMojo();
        /*
         * Create JavaScript engine
         */
        ScriptEngine engine = createJsEngine();
        /*
         * Concatenate header, enum and list of enum
         */
        String s = concatJsFiles(
        				"javascript/header/header.js",
        				"javascript/script/ListOfInts.js"
        			);
        /*
         * Concatenate test method, with explanations below...
         */
        s += "function testList() { " +
        		    "var list = new testpackage.ListOfInts(); " + // create list struct
        		    "list.l.push(1); " + // push 1
        		    "list.l.push(2); " + // push 1
        		    "list.l.push(3); " + // push 1
        		    "var norm = list.getNormalizedObject(); " + // normalize
        		    "return norm.details[\"l\"]; " + // return normalized list
        		"}";
        /*
         * Evaluate and make sure the test method returns the strings
         * 1 - 3.
         */
        engine.eval(s);
        int c = 1;
        ListOfInts iface = ((Invocable)engine).getInterface(ListOfInts.class);
        for (int i : iface.testList()) {
        	assertEquals(c, i);
        	c++;
        }
    }

	public void testFailOnBadStructOrder() throws Exception {
        final String firstPacketJavaFile = TEST_CODE_FULL_DIR + File.separator + "FirstPacket.java";
        
        File testPom = new File(getBasedir(), "src/test/resources/mojo-test-pom.xml");
        ProtocolGeneratorMojo mojo = (ProtocolGeneratorMojo) lookupMojo("generate", testPom);
        assertNotNull(mojo);
        
        setVariableValueToObject(mojo, "project", new MavenProjectStub());
        setVariableValueToObject(mojo, "protocol_file", "src/test/resources/out-of-order-dependencies.xml");
        setVariableValueToObject(mojo, "output_dir", null);

        mojo.execute();
        assertTrue(new File(firstPacketJavaFile).exists());
        deleteGeneratedSource();
        
        setVariableValueToObject(mojo, "fail_on_bad_packet_order", false);
        mojo.execute();
        assertTrue(new File(firstPacketJavaFile).exists());
        deleteGeneratedSource();
        
        setVariableValueToObject(mojo, "fail_on_bad_packet_order", true);
        try {
            mojo.execute();
            fail("an exception should have been thrown");
        } catch (MojoFailureException e) {
            // this should happen
        }
        
        assertFalse(new File(firstPacketJavaFile).exists());
    }
    
    
    // --- PRIVATE METHODS --- //
	
    private void deleteGeneratedSource() throws IOException {
        FileUtils.deleteDirectory(new File(TEST_CODE_DIR));
    }
	
    private String concatJsFiles(String...files) throws Exception {
		StringBuilder b = new StringBuilder();
		for (String s : files) {
			b.append(fileToString(new File(new File(TEST_CODE_DIR), s)));
		}
		return b.toString();
	}
    
	private ScriptEngine createJsEngine() {
		ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("javascript");
		return engine;
	}
    
	private void createAndExecuteJsMojo() throws Exception, IllegalAccessException, MojoExecutionException, MojoFailureException {
		File testPom = new File(getBasedir(), "src/test/resources/mojo-test-js-pom.xml");
        ProtocolGeneratorMojo mojo = (ProtocolGeneratorMojo) lookupMojo("generate", testPom);
        setVariableValueToObject(mojo, "project", new MavenProjectStub());
        mojo.execute();
	}
    
    private String fileToString(File file) throws Exception {
    	FileReader r = new FileReader(file);
    	char[] buff = new char[256];
    	int len = 0;
    	StringBuilder b = new StringBuilder();
    	while((len = r.read(buff)) != -1) {
    		b.append(buff, 0, len);
    	}
    	r.close();
    	return b.toString();
    }
    
    
    // --- PRIVATE CLASSES --- //
    
    private interface ListOfInts {
    	
    	public int[] testList();
    	
    }
}