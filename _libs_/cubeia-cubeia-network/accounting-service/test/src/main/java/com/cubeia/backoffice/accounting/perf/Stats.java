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

package com.cubeia.backoffice.accounting.perf;

import com.google.inject.Singleton;

@Singleton
public class Stats implements StatsMBean {

	private TimeCounter winTime = new TimeCounter(1000);
	private TimeCounter depositTime = new TimeCounter(100);
	private TimeCounter  betTime = new TimeCounter(1000);
	private TimeCounter  balanceTime = new TimeCounter(1000);
	private TimeCounter  totalTime = new TimeCounter(1000);
	
	private SecondCounter winCount = new SecondCounter();
	private SecondCounter depositCount = new SecondCounter();
	private SecondCounter betCount = new SecondCounter();
	private SecondCounter balanceCount = new SecondCounter();
	private SecondCounter totalCount = new SecondCounter();
	
	public void reportBalance(long time) {
		balanceTime.register(time);
		balanceCount.register();
		totalTime.register(time);
		totalCount.register();
	}
	
	public void reportWin(long time) { 
		winTime.register(time);
		winCount.register();
		totalTime.register(time);
		totalCount.register();
	}
	
	public void reportBet(long time) { 
		betTime.register(time);
		betCount.register();
		totalTime.register(time);
		totalCount.register();
	}
	
	public void reportDeposit(long time) { 
		depositTime.register(time);
		depositCount.register();
		totalTime.register(time);
		totalCount.register();
	}
	
	
	@Override
	public double getBetAverageLatency() {
		return betTime.calculate();
	}
	
	@Override
	public int getBetsPerSecond() {
		return betCount.current();
	}
	
	@Override
	public double getTotalAverageLatency() {
		return totalTime.calculate();
	}
	
	@Override
	public int getTotalPerSecond() {
		return totalCount.current();
	}
	
	@Override
	public double getBalanceAverageLatency() {
		return balanceTime.calculate();
	}
	
	@Override
	public int getBalancesPerSecond() {
		return balanceCount.current();
	}
	
	@Override
	public double getDepositAverageLatency() {
		return depositTime.calculate();
	}
	
	@Override
	public int getDepositsPerSecond() {
		return depositCount.current();
	}
	
	@Override
	public double getWinAverageLatency() {
		return winTime.calculate();
	}
	
	@Override
	public int getWinsPerSecond() {
		return winCount.current();
	}
}
