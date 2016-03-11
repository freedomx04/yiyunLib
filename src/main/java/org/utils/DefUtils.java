package org.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefUtils {

	static Log log = LogFactory.getLog(DefUtils.class);

	public static String getDef(String value, String def1) {
		if (value != null) {
			return value;
		}
		return def1;
	}

	public static String getDef(String value, String def1, String def2) {
		if (value != null) {
			return value;
		}
		if (def1 != null) {
			return def1;
		}
		return def2;
	}

	public static String getDef(String value, String def1, String def2, String def3) {
		if (value != null) {
			return value;
		}
		if (def1 != null) {
			return def1;
		}
		if (def2 != null) {
			return def2;
		}
		return def3;
	}

}
