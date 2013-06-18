package com.cubeia.firebase.test.blackbox.util;

import java.util.List;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.PlayerInfoPacket;
import com.cubeia.firebase.io.protocol.SeatInfoPacket;
import com.cubeia.firebase.io.protocol.TableQueryResponsePacket;

public class QueryVerification {

	public static void verifyResponse(TableQueryResponsePacket tableQueryResponse) throws IllegalArgumentException {
		if (tableQueryResponse == null) {
			throw new IllegalArgumentException("response was null");
		}
		
		List<SeatInfoPacket> seats = tableQueryResponse.seats;
		if (seats == null) {
			throw new IllegalArgumentException("seats was null");
		}
		if (seats.size() != 1) {
			throw new IllegalArgumentException("number of seat infos was " + seats.size() + ", should be 1");
		}
		
		PlayerInfoPacket playerInfo = seats.get(0).player;
		if (playerInfo == null) {
			throw new IllegalArgumentException("playerinfo was null");
		}
		
		List<Param> details = playerInfo.details;
		if (details == null) {
			throw new IllegalArgumentException("details was null");
		}
		if (details.size() != 2) {
			throw new IllegalArgumentException("number of details was " + seats.size() + ", should be 2");
		}
		List<Parameter<?>> parameters = ParameterParserUtil.convertParamsToParameters(details);
		
		for (Parameter<?> param : parameters) {
			if (param.getValue() instanceof Integer) {
				verifyIntegerParameter((Integer) param.getValue());
			} else {
				verifyStringParameter((String) param.getValue());
			}
		}
	}
	
	public static void verifyStringParameter(String value) {
		if (!value.contains("lobby")) {
			throw new IllegalArgumentException("string detail did not contain 'lobby', was " + value);
		}
	}

	public static void verifyIntegerParameter(Integer value) {
		if (value != 1100) {
			throw new IllegalArgumentException("int detail was " + value + ", should be 1");
		}
	}	
}
