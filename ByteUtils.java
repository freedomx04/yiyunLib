package org.tsaikd.java.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Deprecated
public class ByteUtils {

	static Log log = LogFactory.getLog(ByteUtils.class);

	public static String bytes2Hex(byte bt) {
		return (""+"0123456789ABCDEF".charAt(0xf&bt>>4)+"0123456789ABCDEF".charAt(bt&0xf));
	}

	public static String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i=0 ; i<bts.length ; i++) {
			tmp = Integer.toHexString(bts[i] & 0xFF);
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}

	public static byte[] hex2Bytes(char[] hex) {
		int length = hex.length / 2;
		byte[] raw = new byte[length];
		for (int i=0 ; i<length ; i++) {
			int high = Character.digit(hex[i * 2], 16);
			int low = Character.digit(hex[i * 2 + 1], 16);
			int value = (high << 4) | low;
			if (value > 127) {
				value -= 256;
			}
			raw[i] = (byte) value;
		}
		return raw;
	}

	public static byte[] hex2Bytes(String hex) {
		return hex2Bytes(hex.toCharArray());
	}
}
