package com.cubeia.firebase.test.blackbox;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.test.systest.game.tests.DeleteTableProcessor;

public class DeleteTableTest extends LoginTest {
	
	@Test
	public void testDelete() throws Exception {
		int tableId = tableProxy.createTable(1, "test/666", "kkk", null, DeleteTableProcessor.class.getName());
		assertTrue(tableProxy.destroyTable(tableId));
	}
	
	@Test
	public void testDeleteWithJoin() throws Exception {
		GameTable table = super.createTable(1, DeleteTableProcessor.class);
		
		// table.join(client, true);
		
		assertTrue(super.destroyTable(table));
	}
}
