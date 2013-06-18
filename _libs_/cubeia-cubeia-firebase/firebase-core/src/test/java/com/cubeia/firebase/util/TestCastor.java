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
package com.cubeia.firebase.util;

import java.io.FileReader;
import java.io.FileWriter;

import junit.framework.TestCase;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

import com.cubeia.firebase.api.game.GameDefinition;

public class TestCastor extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testCastor() throws Exception {
		write();
		read();
		
//		writeMany();
//		readMany();
	}
	
	
public void write() throws Exception {
		
		GameDefinition def = new GameDefinition();
		def.setId(112);
		def.setName("Tank");
		def.setClassname("com.apa.Tank");
		
		
//		 Create a File to marshal to
		FileWriter writer = new FileWriter("test.xml");

//		 Marshal the person object
		Marshaller.marshal(def, writer);

	}
	
	public void read() throws Exception {
		
		
//		 Create a Reader to the file to unmarshal from
		FileReader reader = new FileReader("test.xml");

//		 Marshal the person object
		GameDefinition game = (GameDefinition)Unmarshaller.unmarshal(GameDefinition.class, reader);
		
		assertEquals(112, game.getId());
		assertEquals("Tank", game.getName());
		assertEquals("com.apa.Tank", game.getClassname());
		
	}
	
//	
//	
//	public void writeMany() throws Exception {
//		
//		GameDefinition def = new GameDefinition();
//		def.setId(112);
//		def.setName("Tank");
//		def.setClassname("com.apa.Tank");
//		
//		GameDefinition def2 = new GameDefinition();
//		def2.setId(113);
//		def2.setName("Tank");
//		def2.setClassname("com.apa.Tank2");
//		
//		
//		List<GameDefinition> defs = new ArrayList<GameDefinition>();
//		defs.add(def);
//		defs.add(def2);
//		
//		
////		 Create a File to marshal to
//		FileWriter writer = new FileWriter("testMany.xml");
//
////		 Marshal the person object
//		Marshaller.marshal(defs, writer);
//
//	}
//	
//	
//	@SuppressWarnings("unchecked")
//	public void readMany() throws Exception {
//		
//		
////		 Create a Reader to the file to unmarshal from
//		FileReader reader = new FileReader("test.xml");
//
////		 Marshal the person object
//		ArrayList games = (ArrayList<GameDefinition>)Unmarshaller.unmarshal(ArrayList.class, reader);
//		
//		assertEquals(2, games.size());
//		
//		Iterator it = games.iterator();
//		while (it.hasNext()) {
//			GameDefinition def = (GameDefinition)it.next();
//		}
//		
//	}
	
}
