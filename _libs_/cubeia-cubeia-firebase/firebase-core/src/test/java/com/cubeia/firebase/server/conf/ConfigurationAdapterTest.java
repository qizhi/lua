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
package com.cubeia.firebase.server.conf;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.api.util.StringList;
import com.cubeia.firebase.server.conf.Configuration;
import com.cubeia.firebase.server.conf.ConfigurationAdapter;
import com.cubeia.firebase.server.conf.MapConfiguration;
import com.cubeia.firebase.server.conf.TestIFace.MyEnum;

public class ConfigurationAdapterTest extends TestCase {
	
	private static final String[] PRIMITIVES = {
		"byte",
		"short",
		"int",
		"long",
		"float",
		"double"
	};
	
	
	/// --- INSTANCE MEMBERS --- ///
	
	public void testEnum() throws Exception {
		MapConfiguration conf = getPrimitiveConf(Namespace.NULL, false);
		TestIFace test = getTestIFace(conf);
		MyEnum e = test.getEnum();
		super.assertEquals(MyEnum.TWO, e);
	}
	
	public void testString() throws Exception {
		MapConfiguration conf = getPrimitiveConf(Namespace.NULL, false);
		TestIFace test = getTestIFace(conf);
		StringList l = test.getStrings();
		super.assertEquals(2, l.size());
		super.assertEquals("kalle", l.get(0));
		super.assertEquals("olle", l.get(1));
	}
	
	public void testInetAddress() throws Exception {
		MapConfiguration conf = getPrimitiveConf(Namespace.NULL, false);
		TestIFace test = getTestIFace(conf);
		InetAddress ad = InetAddress.getByName("localhost");
		super.assertEquals(ad, test.getInetAddress());
	}

	public void testPrimitives() throws Exception {
		MapConfiguration conf = getPrimitiveConf(Namespace.NULL, false);
		TestIFace test = getTestIFace(conf);
		verifyPrimitives(test, Namespace.NULL, false);
	}
	
	public void testPrimitiveO() throws Exception {
		MapConfiguration conf = getPrimitiveConf(Namespace.NULL, true);
		TestIFace test = getTestIFace(conf);
		verifyPrimitives(test, Namespace.NULL, true);
	}
	
	public void testChars() throws Exception {
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(Namespace.NULL, "char"), "s");
		MapConfiguration c = new MapConfiguration(map);
		TestIFace test = getTestIFace(c);
		super.assertEquals(test.getChar(), 's');
		map.clear();
		map.put(new PropertyKey(Namespace.NULL, "char"), "suger");
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getChar(), 's');
		map.clear();
		map.put(new PropertyKey(Namespace.NULL, "char"), "");
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getChar(), new Character((char)0).charValue());
		map.clear();
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getChar(), new Character((char)0).charValue());
	}
	
	public void testCharsO() throws Exception {
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(Namespace.NULL, "char_o"), "s");
		MapConfiguration c = new MapConfiguration(map);
		TestIFace test = getTestIFace(c);
		super.assertEquals(test.getCharO(), new Character('s'));
		map.clear();
		map.put(new PropertyKey(Namespace.NULL, "char_o"), "suger");
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getCharO(), new Character('s'));
		map.clear();
		map.put(new PropertyKey(Namespace.NULL, "char_o"), "");
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getCharO(), null);
		map.clear();
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getCharO(), null);
	}
	
	public void testBools() throws Exception {
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(Namespace.NULL, "boolean"), "true");
		MapConfiguration c = new MapConfiguration(map);
		TestIFace test = getTestIFace(c);
		super.assertEquals(test.getBoolean(), true);
		map.clear();
		map.put(new PropertyKey(Namespace.NULL, "boolean"), "false");
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getBoolean(), false);
		map.clear();
		map.put(new PropertyKey(Namespace.NULL, "boolean"), "kalle");
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getBoolean(), false);
		map.clear();
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getBoolean(), false);
	}
	
	public void testBoolsO() throws Exception {
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(Namespace.NULL, "boolean-o"), "true");
		MapConfiguration c = new MapConfiguration(map);
		TestIFace test = getTestIFace(c);
		super.assertEquals(test.getBooleanO(), Boolean.TRUE);
		map.clear();
		map.put(new PropertyKey(Namespace.NULL, "boolean-o"), "false");
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getBooleanO(), Boolean.FALSE);
		map.clear();
		map.put(new PropertyKey(Namespace.NULL, "boolean-o"), "kalle");
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getBooleanO(), Boolean.FALSE);
		map.clear();
		c = new MapConfiguration(map);
		test = getTestIFace(c);
		super.assertEquals(test.getBooleanO(), null);
	}
	
	public void testObjects() throws Exception {
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(Namespace.NULL, "string"), "kalle");
		map.put(new PropertyKey(Namespace.NULL, "file"), "/my/file");
		map.put(new PropertyKey(Namespace.NULL, "url"), "http://www.larsan.net");
		MapConfiguration c = new MapConfiguration(map);
		TestIFace test = getTestIFace(c);
		super.assertEquals("kalle", test.getString());
		super.assertEquals(new File("/my/file"), test.getFile());
		super.assertEquals(new URL("http://www.larsan.net"), test.getUrl());
	}
	
	public void testNullObjects() throws Exception {
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		MapConfiguration c = new MapConfiguration(map);
		TestIFace test = getTestIFace(c);
		super.assertEquals(null, test.getString());
		super.assertEquals(null, test.getFile());
		super.assertEquals(null, test.getUrl());
	}
	
	public void testNs() throws Exception {
		super.assertTrue(TestNsIFace.class.isAnnotationPresent(Configurated.class));
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(new Namespace("my.ns"), "string"), "kalle");
		MapConfiguration c = new MapConfiguration(map);
		TestNsIFace test = getTestNsIFace(c);
		super.assertEquals("kalle", test.getString());
	}
	
	public void testNs2() throws Exception {
		super.assertTrue(TestNsIFace.class.isAnnotationPresent(Configurated.class));
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(new Namespace("my.ns2"), "string"), "kalle");
		MapConfiguration c = new MapConfiguration(map);
		TestNsIFace test = getTestNsIFace(c, new Namespace("my.ns2"));
		super.assertEquals("kalle", test.getString());
		test = getTestNsIFace(c, new Namespace("my.ns"));
		super.assertEquals(null, test.getString());
		test = getTestNsIFace(c);
		super.assertEquals(null, test.getString());
	}
	
	public void testInheritOne() throws Exception {
		super.assertTrue(TestInheritIFaceOne.class.isAnnotationPresent(Configurated.class));
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(new Namespace("my"), "string-one"), "kalle");
		map.put(new PropertyKey(new Namespace("my"), "string-two"), "kalle2");
		MapConfiguration c = new MapConfiguration(map);
		TestInheritIFaceOne test = getTestInheritIFaceOne(c, null);
		super.assertEquals("kalle", test.getStringOne());
		super.assertEquals(null, test.getStringTwo());
		map.clear();
		map.put(new PropertyKey(new Namespace("my.ns"), "string-one"), "olof");
		map.put(new PropertyKey(new Namespace("my.ns"), "string-two"), "kalle2");
		c = new MapConfiguration(map);
		test = getTestInheritIFaceOne(c, null);
		super.assertEquals("olof", test.getStringOne());
		super.assertEquals("kalle2", test.getStringTwo());
	}
	
	public void testInheritTwo() throws Exception {
		super.assertTrue(TestInheritIFaceOne.class.isAnnotationPresent(Configurated.class));
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(new Namespace("my"), "string-one"), "kalle");
		map.put(new PropertyKey(new Namespace("my"), "string-two"), "kalle2");
		MapConfiguration c = new MapConfiguration(map);
		TestInheritIFaceTwo test = getTestInheritIFaceTwo(c, null);
		super.assertEquals(null, test.getStringOne());
		super.assertEquals("kalle2", test.getStringTwo());
		map.clear();
		map.put(new PropertyKey(new Namespace("my.ns"), "string-one"), "olof");
		map.put(new PropertyKey(new Namespace("my.ns"), "string-two"), "kalle2");
		c = new MapConfiguration(map);
		test = getTestInheritIFaceTwo(c, null);
		super.assertEquals("olof", test.getStringOne());
		super.assertEquals("kalle2", test.getStringTwo());
	}
	
	public void testProperties() throws Exception {
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(null, "string-one"), "kalle");
		map.put(new PropertyKey(null, "string-two"), "kalle2");
		MapConfiguration c = new MapConfiguration(map);
		TestPropsIFace test = getTestPropsIFace(c);
		super.assertEquals("kalle", test.getStringThree());
		super.assertEquals("kalle2", test.getStringFour());
		map.clear();
		map.put(new PropertyKey(null, "string-one"), "kalle");
		c = new MapConfiguration(map);
		test = getTestPropsIFace(c);
		super.assertEquals("kalle", test.getStringThree());
		super.assertEquals("kalle3", test.getStringFour());
	}
	
	public void testPropertiesOverrideOne() throws Exception {
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(null, "string-five"), "kalle");
		MapConfiguration c = new MapConfiguration(map);
		TestPropsIFace test = getTestPropsIFace(c);
		super.assertEquals("kalle", test.getOverrideOne());
		super.assertEquals("kalle", test.getStringFive());
		map.clear();
		map.put(new PropertyKey(null, "string-five"), "kalle");
		c = new MapConfiguration(map);
		test = getTestPropsIFace(c, "test");
		super.assertEquals("kalle", test.getOverrideOne());
	}
	
	public void testBooleanWithTrailingSpace() throws Exception {
		Map<PropertyKey, String> map = new HashMap<PropertyKey, String>();
		map.put(new PropertyKey(null, "boolean-one"), "true ");
		MapConfiguration c = new MapConfiguration(map);
		TestPropsIFace test = getTestPropsIFace(c);
		super.assertTrue(test.getBooleanOne());
	}
	
	/// --- PRIVATE METHODS --- ///

	private void verifyPrimitives(TestIFace test, Namespace ns, boolean isO) throws Exception {
		int count = 0;
		for (String s : PRIMITIVES) {
			int check = count++;
			String methName = toGetMethName(s, isO);
			Method m = test.getClass().getMethod(methName, new Class[0]);
			Object o = m.invoke(test, new Object[0]);
			super.assertEquals(check, ((Number)o).intValue());
		}
	}
	
	private String toGetMethName(String s, boolean isO) {
		StringBuilder b = new StringBuilder("get");
		for(int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if(i == 0) ch = Character.toUpperCase(ch);
			b.append(ch);
		}
		if(isO) b.append("O");
		return b.toString();
	}


	private MapConfiguration getPrimitiveConf(Namespace ns, boolean isO) {
		Map<String, String> map = new HashMap<String, String>();
		int count = 0;
		for (String s : PRIMITIVES) {
			map.put(getNsString(ns) + s + getOString(isO), String.valueOf(count++));
		}
		map.put("enum", "two");
		map.put("strings", "kalle,olle");
		map.put("inet-address", "localhost");
		Map<PropertyKey, String> next = MapConfiguration.convert(map);
		MapConfiguration conf = new MapConfiguration(next);
		return conf;
	}

	private String getOString(boolean isO) {
		return (isO ? "-o" : "");
	}

	private String getNsString(Namespace ns) {
		return (Namespace.NULL.equals(ns) ? "" : ns.toString() + ":");
	}

	private TestIFace getTestIFace(Configuration conf) throws Exception {
		return new ConfigurationAdapter(conf).implement(TestIFace.class);
	}
	
	private TestPropsIFace getTestPropsIFace(Configuration conf) throws Exception {
		return new ConfigurationAdapter(conf).implement(TestPropsIFace.class);
	}
	
	private TestPropsIFace getTestPropsIFace(Configuration conf, String ns) throws Exception {
		return new ConfigurationAdapter(conf).implement(TestPropsIFace.class, new Namespace(ns));
	}
	
	private TestNsIFace getTestNsIFace(Configuration conf) throws Exception {
		return getTestNsIFace(conf, null);
	}
	
	private TestNsIFace getTestNsIFace(Configuration conf, Namespace ns) throws Exception {
		if(ns == null) return (TestNsIFace)new ConfigurationAdapter(conf).implement(TestNsIFace.class);
		else return (TestNsIFace)new ConfigurationAdapter(conf).implement(TestNsIFace.class, ns);
	}
	
	private TestInheritIFaceOne getTestInheritIFaceOne(Configuration conf, Namespace ns) throws Exception {
		if(ns == null) return (TestInheritIFaceOne)new ConfigurationAdapter(conf).implement(TestInheritIFaceOne.class);
		else return (TestInheritIFaceOne)new ConfigurationAdapter(conf).implement(TestInheritIFaceOne.class, ns);
	}
	
	private TestInheritIFaceTwo getTestInheritIFaceTwo(Configuration conf, Namespace ns) throws Exception {
		if(ns == null) return (TestInheritIFaceTwo)new ConfigurationAdapter(conf).implement(TestInheritIFaceTwo.class);
		else return (TestInheritIFaceTwo)new ConfigurationAdapter(conf).implement(TestInheritIFaceTwo.class, ns);
	}
}
