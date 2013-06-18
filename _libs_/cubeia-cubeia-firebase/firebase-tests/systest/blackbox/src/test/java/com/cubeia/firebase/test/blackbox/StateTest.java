package com.cubeia.firebase.test.blackbox;

import java.security.MessageDigest;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.Filter;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.test.systest.game.tests.StateTestProcessor;
import com.cubeia.test.systest.io.protocol.StateTestPacket;

public class StateTest extends LoginTest {

	@Test
	public void testState() throws Exception {
		GameTable table = super.createTable(4, StateTestProcessor.class);
	
		table.join(client, true);
		
		String[] arr = new String[] { "olle ", "kalle ", "bertil ", "david", "bulle" };
		
		for (int i = 0; i < arr.length; i++) {
			if(i == 3) {
				/*
				 * This is a fail, and should not be included in the state, ie.
				 * the state should be rolled back server side
				 */
				client.sendGamePacket(new StateTestPacket(arr[i], new byte[0], true), table.getTableId());
			} else {
				/*
				 * Normal packet
				 */
				client.sendGamePacket(new StateTestPacket(arr[i], new byte[0], false), table.getTableId());
			}
		}
		
		String concat = "";
		
		for (int i = 0; i < arr.length; i++) {
			/*
			 * We should have everything except item 3 which was a failed
			 * execution.
			 */
			if(i != 3) {
				concat += arr[i];
				client.expect(new FluidBuilder()
						.expect(StateTestPacket.class, serializer)
						.where("payload").is(concat)
						.andWith(new Md5Filter(concat)), 600000);
			}
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private byte[] md5sum(String data) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(data.getBytes("UTF-8"));
		} catch(Exception e) {
			Logger.getLogger(getClass()).error("Failed to compute checksum", e);
			return null;
		}
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class Md5Filter implements Filter {
		
		private byte[] checksum;

		private Md5Filter(String data) {
			checksum = md5sum(data);
		}
		
		public boolean accept(Object o) {
			StateTestPacket p = (StateTestPacket)o;
			boolean b = Arrays.equals(p.checksum, checksum);
			if(b) {
				return true;
			} else {
				Assert.fail("MD5 sums does not match");
				return false;
			}
		}
	}
}
