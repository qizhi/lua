/**
 * Copyright (C) 2012 BetConstruct
 */

package com.cubeia.poker.timing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;


@Entity
public class TimingProfile implements Serializable {

    private static final long serialVersionUID = -3330607790093489574L;

    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private long pocketCardsTime = 1000;

    @Column(nullable = false)
    private long flopTime = 1000;

    @Column(nullable = false)
    private long turnTime = 1000;

    @Column(nullable = false)
    private long riverTime = 1000;

    @Column(nullable = false)
    private long startNewHandTime = 3000;

    @Column(nullable = false)
    private long actionTimeout = 15000;

    @Column(nullable = false)
    private long autoPostBlindDelay = 300;

    @Column(nullable = false)
    private long latencyGracePeriod = 1000;

    @Column(nullable = false)
    private long disconnectExtraTime = 3 * 15000;

    @Column(nullable = false)
    private long genericTime = 3 * 15000;

    private boolean archived;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFlopTime() {
        return flopTime;
    }

    public void setFlopTime(long flopTime) {
        this.flopTime = flopTime;
    }

    public long getPocketCardsTime() {
        return pocketCardsTime;
    }

    public void setPocketCardsTime(long pocketCardsTime) {
        this.pocketCardsTime = pocketCardsTime;
    }

    public long getTurnTime() {
        return turnTime;
    }

    public void setTurnTime(long turnTime) {
        this.turnTime = turnTime;
    }

    public long getRiverTime() {
        return riverTime;
    }

    public void setRiverTime(long riverTime) {
        this.riverTime = riverTime;
    }

    public long getStartNewHandTime() {
        return startNewHandTime;
    }

    public void setStartNewHandTime(long startNewHandTime) {
        this.startNewHandTime = startNewHandTime;
    }

    public long getActionTimeout() {
        return actionTimeout;
    }

    public void setActionTimeout(long actionTimeout) {
        this.actionTimeout = actionTimeout;
    }

    public long getAutoPostBlindDelay() {
        return autoPostBlindDelay;
    }

    public void setAutoPostBlindDelay(long autoPostBlindDelay) {
        this.autoPostBlindDelay = autoPostBlindDelay;
    }

    public long getLatencyGracePeriod() {
        return latencyGracePeriod;
    }

    public void setLatencyGracePeriod(long latencyGracePeriod) {
        this.latencyGracePeriod = latencyGracePeriod;
    }

    public long getDisconnectExtraTime() {
        return disconnectExtraTime;
    }

    public void setDisconnectExtraTime(long disconnectExtraTime) {
        this.disconnectExtraTime = disconnectExtraTime;
    }

    public long getGenericTime() {
        return genericTime;
    }

    public void setGenericTime(long genericTime) {
        this.genericTime = genericTime;
    }

    public long getTime(Periods period) {
        switch (period) {
            case POCKET_CARDS:
                return pocketCardsTime;
            case FLOP:
                return flopTime;
            case TURN:
                return turnTime;
            case RIVER:
                return riverTime;
            case START_NEW_HAND:
                return startNewHandTime;
            case ACTION_TIMEOUT:
                return actionTimeout;
            case AUTO_POST_BLIND_DELAY:
                return autoPostBlindDelay;
            case LATENCY_GRACE_PERIOD:
                return latencyGracePeriod;
            case DISCONNECT_EXTRA_TIME:
                return disconnectExtraTime;
            default:
                return genericTime;
        }
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimingProfile that = (TimingProfile) o;

        if (actionTimeout != that.actionTimeout) return false;
        if (archived != that.archived) return false;
        if (autoPostBlindDelay != that.autoPostBlindDelay) return false;
        if (disconnectExtraTime != that.disconnectExtraTime) return false;
        if (flopTime != that.flopTime) return false;
        if (genericTime != that.genericTime) return false;
        if (id != that.id) return false;
        if (latencyGracePeriod != that.latencyGracePeriod) return false;
        if (pocketCardsTime != that.pocketCardsTime) return false;
        if (riverTime != that.riverTime) return false;
        if (startNewHandTime != that.startNewHandTime) return false;
        if (turnTime != that.turnTime) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (pocketCardsTime ^ (pocketCardsTime >>> 32));
        result = 31 * result + (int) (flopTime ^ (flopTime >>> 32));
        result = 31 * result + (int) (turnTime ^ (turnTime >>> 32));
        result = 31 * result + (int) (riverTime ^ (riverTime >>> 32));
        result = 31 * result + (int) (startNewHandTime ^ (startNewHandTime >>> 32));
        result = 31 * result + (int) (actionTimeout ^ (actionTimeout >>> 32));
        result = 31 * result + (int) (autoPostBlindDelay ^ (autoPostBlindDelay >>> 32));
        result = 31 * result + (int) (latencyGracePeriod ^ (latencyGracePeriod >>> 32));
        result = 31 * result + (int) (disconnectExtraTime ^ (disconnectExtraTime >>> 32));
        result = 31 * result + (int) (genericTime ^ (genericTime >>> 32));
        result = 31 * result + (archived ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TimingProfile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pocketCardsTime=" + pocketCardsTime +
                ", flopTime=" + flopTime +
                ", turnTime=" + turnTime +
                ", riverTime=" + riverTime +
                ", startNewHandTime=" + startNewHandTime +
                ", actionTimeout=" + actionTimeout +
                ", autoPostBlindDelay=" + autoPostBlindDelay +
                ", latencyGracePeriod=" + latencyGracePeriod +
                ", disconnectExtraTime=" + disconnectExtraTime +
                ", genericTime=" + genericTime +
                ", archived=" + archived +
                '}';
    }
}
