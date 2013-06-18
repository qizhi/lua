package com.cubeia.firebase.test.blackbox;

import static org.testng.Assert.fail;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.GameClient;
import com.cubeia.firebase.test.common.GameTable;
import com.cubeia.firebase.test.common.rules.FluidBuilder;
import com.cubeia.test.systest.game.tests.SequenceCheckProcessor;
import com.cubeia.test.systest.io.protocol.SequenceCheckPacket;

public class MultiClientSequenceTest extends MultiClientTest {

	private int packets;
	private ExecutorService exec;
	private CountDownLatch waitLatch;
	
	public MultiClientSequenceTest() {
		super.setNumberOfClients(2);
	}
	
	@BeforeMethod
	public void initThreads() {
		exec = Executors.newCachedThreadPool();
		waitLatch = new CountDownLatch(noClients);
	}
	
	@AfterMethod
	public void closeThreads() {
		exec.shutdown();
	}
	
	@BeforeClass
	@Parameters({ "packets" })
	public void setTestLength(
			@Optional("100") int packets) {
				this.packets = packets;
	}
	
	@Test
	public void testSingleSequence() throws Exception {
		doRealTest(false);
	}
	
	@Test
	public void testBatchSequence() throws Exception {
		doRealTest(true);
	}
	
	
	// --- PRUVATE METHODS --- ///
	
	private void doRealTest(boolean batch) throws InterruptedException {
		List<String> errors = new CopyOnWriteArrayList<String>();
		GameTable table = super.createTable(4, SequenceCheckProcessor.class);
		for (GameClient cl : super.clients) {
			exec.execute(new ClientRunner(cl, table, batch, errors));
		}
		waitLatch.await();
		if(errors.size() > 0) {
			String tmp = appendErrors(errors);
			fail("One or more workers reported an error: " + tmp);
		}
	}
	
	private String appendErrors(List<String> errors) {
		String s = errors.get(0);
		for (int i = 1; i < errors.size(); i++) {
			s += "; " + errors.get(i);
		}
		return s;
	}


	// --- PRIVATE CLASSES --- //
	
	private class ClientRunner implements Runnable {
		
		private final Random random = new Random();
		
		private final GameClient client;
		private final GameTable table;
		private final boolean batch;
		private final List<String> errors;
		
		private int currentId;
		
		public ClientRunner(GameClient client, GameTable table, boolean batch, List<String> errors) {
			this.client = client;
			this.table = table;
			this.batch = batch;
			this.errors = errors;
		}
		
		@Override
		public void run() {
			table.join(client, true);
			try {
				Thread.sleep(500); // just a nicety
				if(batch) {
					testBatch();
				} else {
					testSingle();
				}
			} catch(Exception e) {
				errors.add("Client " + client.getPlayerId() + " failed when waiting for id " + currentId); // " + e.getMessage());
			} finally {
				table.leave(client, false);
				waitLatch.countDown();
			}
		}

		private void testBatch() throws Exception {
			// send all
			for (int i = 0; i < packets; i++) {
				if(errors.size() > 0) {
					return; // CUT EARLY, SOMEONE HAS A PROBLEM
				}
				send(i);
			}
			// await all
			for (int i = 0; i < packets; i++) {
				if(errors.size() > 0) {
					return; // CUT EARLY, SOMEONE HAS A PROBLEM
				}
				await(i);
			}
		}

		private void await(int id) {
			currentId = id;
			client.expect(new FluidBuilder()
						.expect(SequenceCheckPacket.class, serializer)
						.where("seq").is(id));
		}

		private void testSingle() throws Exception {
			// send and wait for all
			for (int i = 0; i < packets; i++) {
				if(errors.size() > 0) {
					return; // CUT EARLY, SOMEONE HAS A PROBLEM
				}
				send(i);
				await(i);
			}
		}

		private void send(int id) throws Exception {
			client.sendGamePacket(new SequenceCheckPacket(id, id), table.getTableId());
			Thread.sleep(random.nextInt(10));
		}
	}
}
