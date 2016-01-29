package org.tsaikd.java.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HashUtils {

	static Log log = LogFactory.getLog(HashUtils.class);

	protected static MessageDigest mdsha512;

	static {
		try {
			mdsha512 = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static String sha512(byte[] input) {
		byte[] result = mdsha512.digest(input);
		return Hex.encodeHexString(result);
	}

}
