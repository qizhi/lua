package com.cubeia.games.poker.tournament.configuration.blinds;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
public class Level implements Serializable {

    /** Small blind amount, in number of chips, not cents. */
    private BigDecimal smallBlindAmount;

    /** Big blind amount, in number of chips, not cents. */
    private BigDecimal bigBlindAmount;

    /** Ante amount, in number of chips, not cents. */
    private BigDecimal anteAmount;

    private int durationInMinutes;

    private boolean isBreak;

    @Id
    @GeneratedValue
    private int id;

    public Level() {
    }

    public Level(BigDecimal smallBlindAmount, BigDecimal bigBlindAmount, BigDecimal anteAmount, int durationInMinutes, boolean isBreak) {
        this.smallBlindAmount = smallBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
        this.anteAmount = anteAmount;
        this.durationInMinutes = durationInMinutes;
        this.isBreak = isBreak;
    }

    public Level(BigDecimal smallBlindAmount, BigDecimal bigBlindAmount, BigDecimal anteAmount) {
        this(smallBlindAmount, bigBlindAmount, anteAmount, 0, false);
    }

    public BigDecimal getSmallBlindAmount() {
        return smallBlindAmount;
    }

    public BigDecimal getBigBlindAmount() {
        return bigBlindAmount;
    }

    public BigDecimal getAnteAmount() {
        return anteAmount;
    }

    public boolean isBreak() {
        return isBreak;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setSmallBlindAmount(BigDecimal smallBlindAmount) {
        this.smallBlindAmount = smallBlindAmount;
    }

    public void setBigBlindAmount(BigDecimal bigBlindAmount) {
        this.bigBlindAmount = bigBlindAmount;
    }

    public void setAnteAmount(BigDecimal anteAmount) {
        this.anteAmount = anteAmount;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public void setBreak(boolean isBreak) {
        this.isBreak = isBreak;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Level{" +
                "smallBlindAmount=" + smallBlindAmount +
                ", bigBlindAmount=" + bigBlindAmount +
                ", anteAmount=" + anteAmount +
                ", durationInMinutes=" + durationInMinutes +
                ", isBreak=" + isBreak +
                ", id=" + id +
                '}';
    }

}
