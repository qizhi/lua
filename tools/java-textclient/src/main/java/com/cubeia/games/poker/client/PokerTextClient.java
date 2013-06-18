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

package com.cubeia.games.poker.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import com.cubeia.firebase.clients.java.connector.text.Player;
import com.cubeia.firebase.clients.java.connector.text.SimpleTextClient;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.LoginRequestPacket;
import com.cubeia.firebase.io.protocol.MttTransportPacket;
import com.cubeia.games.poker.io.protocol.BuyInRequest;
import com.cubeia.games.poker.io.protocol.Enums.ActionType;
import com.cubeia.games.poker.io.protocol.PerformAction;
import com.cubeia.games.poker.io.protocol.PlayerAction;
import com.cubeia.games.poker.io.protocol.RebuyResponse;
import com.cubeia.games.poker.io.protocol.RequestTournamentLobbyData;

public class PokerTextClient extends SimpleTextClient {

    public static int seq = -1;

    private StyxSerializer styxEncoder = new StyxSerializer(null);


    public PokerTextClient(String host, int port) {
        super(host, port);
        ManualPacketHandler handler = new ManualPacketHandler();
        handler.setTestHandler(new ManualGameHandler(context));
        context.getConnector().addPacketHandler(handler);
        commandNotifier = new PatchedCommandNotifier(context, this, true);
    }

    /**
     * Override this in your implementation to handle
     * client specific commands.
     *
     * @param command
     */
    @Override
    public void handleCommand(String command) {
        try {
            String[] args = command.split(" ");

            if (args.length < 1) {
                reportBadCommand(command);
                return;
            }

            if (args[0].equalsIgnoreCase("help")) {
                printHelp();
            } else {

                if (!handleGenericPokerCommand(args)) {
                    handlePokerCommand(args);
                }
            }


        } catch (Exception e) {
            reportBadCommand(e.toString());
        }
    }

    private boolean handleGenericPokerCommand(String[] args) throws IOException {
        if (args[0].equals("ologin")) {
            String username = args[1];
            String password = args[2];
            int operatorId = Integer.parseInt(args[3]);
            LoginRequestPacket loginRequest = new LoginRequestPacket();
            loginRequest.operatorid = operatorId;
            loginRequest.user = username;
            loginRequest.password = password;
            send(loginRequest);
            context.setPlayer(new Player(username, -1));
        } else if (args[0].equals("buyin")) {
            int tableId = Integer.parseInt(args[1]);
            BuyInRequest packet = new BuyInRequest();
            packet.amount = "" + Integer.parseInt(args[2]);
            packet.sitInIfSuccessful = true;
            send(tableId, packet);
        } else if (args[0].equals("tourlobby")) {
            int tournamentId = Integer.parseInt(args[1]);
            RequestTournamentLobbyData request = new RequestTournamentLobbyData();
            sendTournamentDataPacket(tournamentId, context.getPlayerId(), styxEncoder.pack(request));
        } else if (args[0].equals("rebuy")) {
            int tableId = Integer.parseInt(args[1]);
            System.out.println("Performing rebuy at table " + tableId);
            RebuyResponse response = new RebuyResponse(true);
            send(tableId, response);
        } else {
            return false;
        }

        return true;
    }

    private void handlePokerCommand(String[] args) {
        PerformAction packet = new PerformAction();
        packet.player = context.getPlayerId();
        packet.seq = seq;

        if (args[0].equals("ante")) {
            PlayerAction type = new PlayerAction();
            type.type = ActionType.ANTE;
            packet.action = type;

        } else if (args[0].equals("small")) {
            PlayerAction type = new PlayerAction();
            type.type = ActionType.SMALL_BLIND;
            packet.action = type;

        } else if (args[0].equals("big")) {
            PlayerAction type = new PlayerAction();
            type.type = ActionType.BIG_BLIND;
            packet.action = type;

        } else if (args[0].equals("check")) {
            PlayerAction type = new PlayerAction();
            type.type = ActionType.CHECK;
            packet.action = type;

        } else if (args[0].equals("call")) {
            PlayerAction type = new PlayerAction();
            type.type = ActionType.CALL;
            packet.action = type;

        } else if (args[0].equals("bet")) {
            try {
                PlayerAction type = new PlayerAction();
                type.type = ActionType.BET;
                packet.action = type;
                packet.betAmount = new BigDecimal(args[2]).toPlainString();
            } catch (Exception e) {
                System.out.println("usage: bet <tid> <amount>");
            }

        } else if (args[0].equals("raise")) {
            try {
                PlayerAction type = new PlayerAction();
                type.type = ActionType.RAISE;
                packet.action = type;
                packet.betAmount = new BigDecimal(args[2]).toPlainString();
            } catch (Exception e) {
                System.out.println("usage: raise <tid> <amount>");
            }

        } else if (args[0].equals("fold")) {
            PlayerAction type = new PlayerAction();
            type.type = ActionType.FOLD;
            packet.action = type;
        }

        int tableId = Integer.parseInt(args[1]);
        send(tableId, packet);
    }

    /**
     * Sends data wrapped in a GameTransportPacket
     * the context attribute is supplied by the super class
     *
     * @param tableId
     * @param packet
     */
    private void send(int tableId, ProtocolObject packet) {
        ByteBuffer buffer = styxEncoder.pack(packet);
        context.getConnector().sendDataPacket(tableId, context.getPlayerId(), buffer);
    }

    private void send(ProtocolObject packet) {
        context.getConnector().sendPacket(packet);
    }

    private void sendTournamentDataPacket(int tournamentId, int playerId, ByteBuffer buffer) {
        MttTransportPacket packet = new MttTransportPacket();
        packet.mttid = tournamentId;
        packet.pid = playerId;
        packet.mttdata = buffer.array();
        context.getConnector().sendPacket(packet);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java PokerTextClient [port] host \nEx.: " +
                    "\n\t java PokerTextClient localhost" +
                    "\n\t java PokerTextClient 4123 localhost");
            return;
        }

        int hostIndex = 0;
        int port = 4123; // Default

        // If the first argument is a string of digits then we take that
        // to be the port number to use
        if (Pattern.matches("[0-9]+", args[0])) {
            port = Integer.parseInt(args[0]);
            hostIndex = 1;
        }

        PokerTextClient client = new PokerTextClient(args[hostIndex], port);
        client.run();
    }

    /**
     * Print bad command to user with a specified error.
     *
     * @param error
     */
    private void reportBadCommand(String error) {
        System.err.println("Invalid command (" + error + ") Format:cmd TID <amount>");
    }

    private void printHelp() {
        System.out.println("Available Poker Commands:");
        System.out.println("\t help             \t : print help");
        System.out.println("\t buyin TID amount \t : Buy in at table");
        System.out.println("\t small TID        \t : post small blind");
        System.out.println("\t big TID          \t : post big blind");

        System.out.println("\t check TID        \t : Check");
        System.out.println("\t call TID         \t : Call");
        System.out.println("\t bet TID <amount> \t : Bet");
        System.out.println("\t fold TID         \t : Fold");
    }
}