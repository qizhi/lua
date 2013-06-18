package com.cubeia.firebase.test.blackbox.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.defined.Parameter.Type;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.ParamFilter;

public class ParameterParserUtil {
	/**
     * Format
     * 
     * key=value
     * key>value
     * key<value
     * key>=value
     * key<=value
     * 
     * 
     * @param params
     * @return
     */
    public static List<ParamFilter> getFilteredParams(String params) throws Exception {
    	List<ParamFilter> result = new ArrayList<ParamFilter>();
    	
    	Pattern pattern = Pattern.compile("(\\p{Punct}?\\p{Alnum}++)(\\p{Punct}++)(\\S++)");
		Matcher matcher = pattern.matcher(params);
		
		while (matcher.find()) {
    		String key = matcher.group(1);
    		String op = matcher.group(2);
    		String value = matcher.group(3);
    		
    		Param identifier = new Param();
    		identifier.key = key;
    		identifier.type = getTypeCode(value);
    		identifier.value = getValueData(value);
    		
    		ParamFilter filter = new ParamFilter();
    		filter.op = getOpCode(op);
    		filter.param = identifier;
    		
    		result.add(filter);
    		
		}
    	
    	return result;
	}
    
	public static List<Parameter<?>> convertParamsToParameters(List<Param> params) {
		List<Parameter<?>> parameters = new ArrayList<Parameter<?>>();
		for (Param p : params) {
			parameters.add(convertParamToParameter(p));
		}
		return parameters;
	}
	
	public static Parameter<?> convertParamToParameter(Param p) {
		Parameter<?> param;
		if (p.type == Type.STRING.ordinal()) {
			param = convertParamToStringParameter(p);
		} else if (p.type == Type.INT.ordinal()) {
			param = convertParamToByteParameter(p);
		} else {
			throw new IllegalArgumentException("Unknown type: " + p.type);
		}
		return param;
	}
	
	public static Parameter<Integer> convertParamToByteParameter(Param p) {
		int i = byteArrayToInt(p.value);
		Parameter<Integer> param = new Parameter<Integer>(p.key, i, Type.STRING);
		return param;
	}

	public static Parameter<String> convertParamToStringParameter(Param p) {
		if (p.value.length < 3) return null; // Not enough data
		byte[] bs = new byte[p.value.length-1];
		ByteBuffer buf = ByteBuffer.wrap(p.value);
		buf.get();
		buf.get(bs);
		Parameter<String> param = new Parameter<String>(p.key, new String(bs), Type.STRING);
		return param;
	}
	
    private static int byteArrayToInt(byte[] b) {
        return byteArrayToInt(b, 0);
    }

    private static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
    
    private static byte[] getValueData(String value) {
    	if (value.matches("[0-9]+")) {
			int i = Integer.parseInt(value);
			return intToBytes(i);
		} else {
			return value.getBytes();
		} 
	}
    
	private static byte[] intToBytes(int i) {
		byte[] dword = new byte[4];
		dword[0] = (byte) ((i >> 24) & 0x000000FF);
		dword[1] = (byte) ((i >> 16) & 0x000000FF);
		dword[2] = (byte) ((i >> 8) & 0x000000FF);
		dword[3] = (byte) (i & 0x00FF);
		return dword;
	}

    private static byte getTypeCode(String value) {
		if (value.matches("[0-9]+")) {
			return (byte)Parameter.Type.INT.ordinal();
			
		} else {
			return (byte)Parameter.Type.STRING.ordinal();
			
		} 
	}

    private static byte getOpCode(String op) {
    	if (op.equals("=")) {
    		return (byte)Parameter.Operator.EQUALS.ordinal();
    		
    	} else if (op.equals(">")) {
    		return (byte)Parameter.Operator.GREATER_THAN.ordinal();
    		
    	}else if (op.equals("<")) {
    		return (byte)Parameter.Operator.SMALLER_THAN.ordinal();
    		
    	}else if (op.equals(">=") || op.equals("=>")) {
    		return (byte)Parameter.Operator.EQUALS_OR_GREATER_THAN.ordinal();
    		
    	}else if (op.equals("<=") || op.equals("=<")) {
    		return (byte)Parameter.Operator.EQUALS_OR_SMALLER_THAN.ordinal();
    		
    	} else {
    		return (byte)-1;
    	}
    }
}
