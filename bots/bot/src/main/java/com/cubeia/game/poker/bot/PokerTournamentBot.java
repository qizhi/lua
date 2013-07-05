package com.cubeia.game.poker.bot;

import com.cubeia.firebase.bot.Bot;
import com.cubeia.firebase.bot.ai.LoginCredentials;
import com.cubeia.firebase.bot.ai.MttAI;
import com.cubeia.firebase.io.protocol.GameTransportPacket;
import com.cubeia.firebase.io.protocol.MttTransportPacket;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import java.security.MessageDigest;

public class PokerTournamentBot extends MttAI implements AiProvider {

    private static transient Logger log = Logger.getLogger(PokerBot.class);

    private GameHandler handler;

    private int operatorId = 0;

    private boolean hashPasswd = false;

    private String pokerAi;

    public PokerTournamentBot(Bot bot) {
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

    public String getPokerAi() {
        return pokerAi;
    }

    public void setPokerAi(String pokerAi) {
        this.pokerAi = pokerAi;
    }

    @Override
    public synchronized void handleGamePacket(GameTransportPacket packet) {
        if (table.getId() != packet.tableid) {
            log.fatal("I received wrong table id! I am seated at: " + table.getId() + ". I got packet from: " + packet.tableid + " Packet: " + handler
                    .unpack(packet));
        }
        handler.handleGamePacket(packet);
    }

    @Override
    public void handleTournamentPacket(MttTransportPacket packet) {
        handler.handleTournamentPacket(packet);
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

}
