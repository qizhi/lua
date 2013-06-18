package com.cubeia.games.poker.admin.wicket;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.wicket.util.convert.converter.BigDecimalConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomBigDecimalConverter extends BigDecimalConverter {

	private static final long serialVersionUID = 1L;
	
	static Logger log = LoggerFactory.getLogger(CustomBigDecimalConverter.class);
	
	@Override
	public BigDecimal convertToObject(String value, Locale locale) {
		value = value.replaceAll(",", ".");
		return new BigDecimal(value);
	}
	
	@Override
	public String convertToString(BigDecimal value, Locale locale) {
		return value.toPlainString();
	}

}
