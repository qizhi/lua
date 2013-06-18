package com.cubeia.firebase.test.blackbox.util;

import com.cubeia.firebase.test.common.TableCreator;
import com.cubeia.test.systest.game.ActivatorMBean;

public class TableCreatorImpl implements TableCreator {

	private final ActivatorMBean proxy;
	private final int seats;
	private final String properties;
	private final String processor;

	public TableCreatorImpl(ActivatorMBean proxy, int seats, String properties, String processor) {
		this.proxy = proxy;
		this.seats = seats;
		this.properties = properties;
		this.processor = processor;
	}
	
	public int create() {
		return proxy.createTable(seats, "Systest Table <"+ seats + ">", properties, processor);
	}
}
