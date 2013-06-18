package com.cubeia.poker.rounds;

import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;

public interface RoundCreator {
    Round create(PokerContext context, ServerAdapterHolder serverAdapterHolder);
}
