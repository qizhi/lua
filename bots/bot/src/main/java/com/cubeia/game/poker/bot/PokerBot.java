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

package com.cubeia.game.poker.bot;

import com.cubeia.firebase.bot.Bot;
import com.cubeia.firebase.bot.ai.BasicAI;
import com.cubeia.firebase.bot.ai.LoginCredentials;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.ProbePacket;
import com.cubeia.games.poker.io.protocol.BuyInInfoRequest;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import java.security.MessageDigest;

/**
 * Poker Bot.
 * <p/>
 * Relies on the Cubeia load test framework.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class PokerBot extends BasicAI implements AiProvider {

    private static transient Logger log = Logger.getLogger(PokerBot.class);

    private GameHandler handler;
    private int operatorId = 0;
    private boolean hashPasswd = false;

    private String pokerAi;

    public PokerBot(Bot bot) {
        super(bot);
        handler = new GameHandler(this);
    }

    public void setHashPasswd(boolean hashPasswd) {
        this.hashPasswd = hashPasswd;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }

    public boolean isHashPasswd() {
        return hashPasswd;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public synchronized void handleGamePacket(GameTransportPacket packet) {
        if (table.getId() != packet.tableid) {
            log.fatal("I received wrong table id! I am seated at: " + table.getId() + ". I got packet from: " + packet.tableid + " Packet: " + handler
                    .unpack(packet));
        }
        handler.handleGamePacket(packet);
    }

    @Override
    protected void handleLoggedin() {
        super.handleLoggedin();
    }

    /**
     * I don't care, said Pierre,
     * cause I am from France
     */
    public void handleProbePacket(ProbePacket packet) {
    }

    public void stop() {
    }

    public boolean trackTableState() {
        return true;
    }

    public String getPokerAi() {
        return pokerAi;
    }

    public void setPokerAi(String pokerAi) {
        this.pokerAi = pokerAi;
    }

    @Override
    public LoginCredentials getCredentials() {
        LoginCredentials cred = new LoginCredentials("Bot_" + getBot().getId(), getPassword());
        cred.setOperatorId(operatorId);
        return cred;
    }

    private String getPassword() {
        String password = String.valueOf(getBot().getId());
        if (hashPasswd) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.reset();
                md.update(password.getBytes("ISO-8859-1"));
                byte[] bytes = md.digest();
                return Hex.encodeHexString(bytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return password;
        }
    }

    /**
     * Send a buy in info request as soon as we are seated.
     */
    @Override
    protected void handleSeated() {
        super.handleSeated();
        BuyInInfoRequest buyInInfoRequest = new BuyInInfoRequest();
        getBot().sendGameData(getTable().getId(), getBot().getPid(), buyInInfoRequest);
    }

}
