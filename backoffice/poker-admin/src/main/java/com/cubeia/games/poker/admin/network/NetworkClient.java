package com.cubeia.games.poker.admin.network;

import java.util.List;

import com.cubeia.backoffice.operator.api.OperatorDTO;

public interface NetworkClient {

	public List<OperatorDTO> getOperators();
	
    public List<String> getCurrencies();
    
}
