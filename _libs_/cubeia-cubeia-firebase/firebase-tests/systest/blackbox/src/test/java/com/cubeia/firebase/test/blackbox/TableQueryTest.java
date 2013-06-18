package com.cubeia.firebase.test.blackbox;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.io.protocol.PlayerInfoPacket;
import com.cubeia.firebase.io.protocol.SeatInfoPacket;
import com.cubeia.firebase.io.protocol.TableQueryRequestPacket;
import com.cubeia.firebase.io.protocol.TableQueryResponsePacket;
import com.cubeia.firebase.test.blackbox.util.ParameterParserUtil;
import com.cubeia.firebase.test.common.GameTable;

public class TableQueryTest extends LoginTest {

	@Test
	public void testTableQuery() throws Exception {
		GameTable table = super.createTable(2);
	
		table.join(client, true);
		
		client.sendFirebasePacket(new TableQueryRequestPacket(table.getTableId()));
		
		TableQueryResponsePacket packet = client.expectFirebasePacket(TableQueryResponsePacket.class);
		
		verifyResponse(packet);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void verifyResponse(TableQueryResponsePacket tableQueryResponse) throws IllegalArgumentException {
		Assert.assertNotNull(tableQueryResponse.seats);
		List<SeatInfoPacket> seats = tableQueryResponse.seats;
		Assert.assertEquals(seats.size(), 1);
		Assert.assertNotNull(seats.get(0).player);
		PlayerInfoPacket playerInfo = seats.get(0).player;
		Assert.assertNotNull(playerInfo.details);
		Assert.assertEquals(playerInfo.details.size(), 2);
		List<Parameter<?>> parameters = ParameterParserUtil.convertParamsToParameters(playerInfo.details);
		for (Parameter<?> param : parameters) {
			if (param.getValue() instanceof Integer) {
				verifyIntegerParameter((Integer) param.getValue());
			} else {
				verifyStringParameter((String) param.getValue());
			}
		}
	}

	private void verifyStringParameter(String value) {
		if (!value.contains("lobby")) {
			Assert.fail("string detail did not contain 'lobby', was " + value);
		}
	}

	private void verifyIntegerParameter(Integer value) {
		Assert.assertEquals(value, Integer.valueOf(100 * client.getPlayerId()));
	}
}