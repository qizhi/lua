package com.cubeia.poker.hand;

import java.util.Random;

public interface DeckProvider {

    Deck createNewDeck(Random randomizer, int playersAtTable);
}
