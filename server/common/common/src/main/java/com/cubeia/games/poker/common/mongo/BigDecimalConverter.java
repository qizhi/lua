package com.cubeia.games.poker.common.mongo;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;

import java.math.BigDecimal;

public class BigDecimalConverter extends TypeConverter implements SimpleValueConverter {

    public BigDecimalConverter() {
        super(BigDecimal.class);
    }

    @Override
    public Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) throws MappingException {
          return new BigDecimal(fromDBObject.toString());
    }
    @Override
    public String encode(Object value, MappedField optionalExtraInfo) throws MappingException {
        return ((BigDecimal) value).toPlainString();
    }
}

