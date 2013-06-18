/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.admin.wicket.pages.history;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cubeia.poker.handhistory.api.*;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.cubeia.games.poker.admin.Configuration;
import com.cubeia.games.poker.admin.service.history.HistoryService;
import com.cubeia.games.poker.admin.wicket.BasePage;
import com.cubeia.games.poker.admin.wicket.util.ExternalLinkPanel;

public class ShowHand extends BasePage {

    private static final long serialVersionUID = -3963453168151993944L;

    @SpringBean
    private HistoryService historyService;

    @SpringBean(name = "webConfig")
    private Configuration config;

    public ShowHand(PageParameters parameters) {
        super(parameters);
        String handId = parameters.get("handId").toString();
        HistoricHand hand = historyService.findHandById(handId);
        addSummary(hand);
        addPlayerList(hand);
        addEvents(hand);
    }

    private void addPlayerList(final HistoricHand hand) {
        DataView<Player> players = new DataView<Player>("players", new ListDataProvider<Player>(hand.getSeats())) {

            private static final long serialVersionUID = 1908334758912501993L;

            @Override
            protected void populateItem(Item<Player> item) {
                Player player = item.getModelObject();
                Results results = hand.getResults();
                HandResult resultsForPlayer = results.getResults().get(player.getPlayerId());
                item.add(new ExternalLinkPanel("playerName", player.getName(), createPlayerLink(player)));
                item.add(new Label("playerId", String.valueOf(player.getPlayerId())));
                if (resultsForPlayer == null) {
                    item.add(new Label("seat", String.valueOf(player.getSeatId()) + " (waiting)"));
                    item.add(new Label("bet", ""));
                    item.add(new Label("net", ""));
                    item.add(new Label("initialBalance", formatAmount(player.getInitialBalance())));
                    item.add(new Label("finalBalance", formatAmount(player.getInitialBalance())));
                }
                else {
                    item.add(new Label("seat", String.valueOf(player.getSeatId())));
                    item.add(new Label("bet", formatAmount(resultsForPlayer.getTotalBet())));
                    String net = formatAmount(resultsForPlayer.getNetWin());
                    if (resultsForPlayer.getTransactionId() != null) {
                        item.add(new ExternalLinkPanel("net", net, createTransactionLink(resultsForPlayer)));
                    } else {
                        item.add(new Label("net", net));
                    }
                    item.add(new Label("initialBalance", formatAmount(player.getInitialBalance())));
                    item.add(new Label("finalBalance", formatAmount(player.getInitialBalance().add(resultsForPlayer.getNetWin()))));
                }
            }

            private String createTransactionLink(HandResult handResult) {
                String tmp = normalizeBaseUrl();
                tmp += "wicket/bookmarkable/com.cubeia.backoffice.web.wallet.TransactionInfo?transactionId=" + handResult.getTransactionId();
                return tmp;
            }

            private String createPlayerLink(Player player) {
                String tmp = normalizeBaseUrl();
                tmp += "wicket/bookmarkable/com.cubeia.backoffice.web.user.UserSummary?userId=" + player.getPlayerId();
                return tmp;
            }

            private String normalizeBaseUrl() {
                String tmp = config.getNetworkUrl();
                if (!tmp.endsWith("/")) {
                    tmp += "/";
                }
                return tmp;
            }
        };
        add(players);
    }

    private void addSummary(HistoricHand hand) {
        add(new Label("handId", hand.getId()));
        add(new Label("tableId", String.valueOf(hand.getTable().getTableId())));
        add(new Label("tableIntegrationId", hand.getTable().getTableIntegrationId()));
        add(new Label("startTime", new Date(hand.getStartTime()).toString()));
        add(new Label("endTime", new Date(hand.getEndTime()).toString()));
        add(new Label("totalRake", formatAmount(hand.getResults().getTotalRake())));
    }

    private void addEvents(final HistoricHand hand) {
        DataView<HandHistoryEvent> events = new DataView<HandHistoryEvent>("events", new ListDataProvider<HandHistoryEvent>(hand.getEvents())) {
            @Override
            protected void populateItem(Item<HandHistoryEvent> item) {
                HandHistoryEvent event = item.getModelObject();

                Model<String> action = new Model<String>();
                Model<String> amount = new Model<String>();
                Model<String> playerId = new Model<String>();
                CardList kickers = new CardList("kickers");
                CardList cards = new CardList("cards");

                item.add(new Label("time", new Date(event.getTime()).toString()));
                item.add(new Label("action", action));
                item.add(new Label("amount", amount));
                item.add(cards);
                item.add(kickers);
                item.add(new Label("playerId", playerId));

                if (event instanceof PlayerAction) {
                    PlayerAction playerAction = (PlayerAction) event;
                    action.setObject(playerAction.getAction().getName());
                    playerId.setObject(String.valueOf(playerAction.getPlayerId()));
                    amount.setObject(formatAmount(playerAction.getAmount()));
                } else if (event instanceof PotUpdate) {
                    PotUpdate potUpdate = (PotUpdate) event;
                    action.setObject("Pot update");
                    amount.setObject(stringRepresentation(potUpdate));
                } else if (event instanceof PlayerCardsDealt) {
                    PlayerCardsDealt playerCards = (PlayerCardsDealt) event;
                    action.setObject("Player cards dealt");
                    cards.setList(playerCards.getCards());
                    playerId.setObject(String.valueOf(playerCards.getPlayerId()));
                } else if (event instanceof TableCardsDealt) {
                    TableCardsDealt playerCards = (TableCardsDealt) event;
                    action.setObject("Table cards dealt");
                    cards.setList(playerCards.getCards());
                } else if (event instanceof PlayerCardsExposed) {
                    PlayerCardsExposed exposed = (PlayerCardsExposed) event;
                    action.setObject("Players cards exposed");
                    playerId.setObject(String.valueOf(exposed.getPlayerId()));
                    cards.setList(exposed.getCards());
                } else if (event instanceof ShowDownSummary) {
                    action.setObject("Show down summary");
                } else if (event instanceof PlayerBestHand) {
                    PlayerBestHand bestHand = (PlayerBestHand) event;
                    playerId.setObject(String.valueOf(bestHand.getPlayerHand().getPlayerId()));
                    HandStrengthCommon handInfo = null;
                    if (bestHand.getHandInfoCommon() instanceof HandStrengthCommon) {
                        handInfo = (HandStrengthCommon)bestHand.getHandInfoCommon();
                    }
                    if (handInfo != null) {
                        BigDecimal winAmount = hand.getResults().getResults().get(bestHand.getPlayerHand().getPlayerId()).getTotalWin();
                        if (winAmount.compareTo(BigDecimal.ZERO) > 0) {
                            amount.setObject("Win (" + formatAmount(winAmount) + ")");
                        }
                        List<GameCard> kickerCards = handInfo.getKickerCards();
                        BestHandType handType = handInfo.getHandType();
                        action.setObject(handType.getName());
                        List<GameCard> list = new ArrayList<GameCard>();
                        switch (handType) {
                            case HIGH_CARD:
                                list.add(handInfo.getGroups().get(0).get(0));
                                kickerCards.remove(0);
                                cards.setList(list);
                                break;
                            case TWO_PAIRS:
                                list.addAll(handInfo.getCardsUsedInHand());
                                cards.setList(list.subList(0, 4));
                                break;
                            case PAIR:
                            case THREE_OF_A_KIND:
                            case FOUR_OF_A_KIND:
                                cards.setList(handInfo.getGroups().get(2));
                                break;
                            case NOT_RANKED:
                            case STRAIGHT:
                            case FLUSH:
                            case FULL_HOUSE:
                            case STRAIGHT_FLUSH:
                            case ROYAL_STRAIGHT_FLUSH:
                            default:
                                cards.setList(handInfo.getCardsUsedInHand());
                                break;
                        }
                        if (kickerCards != null && kickerCards.size() > 0) {
                            kickers.setList(kickerCards);
                        }
                    }
                }
            }
        };
        add(events);
    }

    @Override
    public String getPageTitle() {
        return "Show Hand";
    }

    private String formatAmount(Amount amount) {
        if (amount == null) {
            return "";
        }
        return formatAmount(amount.getAmount());
    }

    private String formatAmount(BigDecimal amount) {
       return amount.toPlainString();
    }

    private String stringRepresentation(PotUpdate potUpdate) {
        StringBuilder result = new StringBuilder();
        List<GamePot> pots = potUpdate.getPots();
        if (pots.size() > 0) {
            result.append("Pot: " + formatAmount(pots.get(0).getPotSize()));
        }
        for (int i = 1; i < pots.size(); i++) {
            result.append("\nSide pot " + i + ": " + formatAmount(pots.get(i).getPotSize()));
        }
        return result.toString();
    }

    private static class CardList extends ListView<GameCard> {

        public CardList(String id) {
            super(id);
        }

        @Override
        protected void populateItem(ListItem<GameCard> item) {
            GameCard card = item.getModelObject();
            String imageName = card.getRank().getAbbreviation() + card.getSuit().name().charAt(0);
            item.add(new ContextImage("cardImage", "images/cards/" + imageName.toLowerCase() + ".svg"));
        }
    }
}