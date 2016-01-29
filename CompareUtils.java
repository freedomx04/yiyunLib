package org.tsaikd.java.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CompareUtils {

	static Log log = LogFactory.getLog(CompareUtils.class);

	public static boolean equals(String a, String b) {
		if (a == null) {
			if (b != null) {
				return false;
			}
		} else {
			if (!a.equals(b)) {
				return false;
			}
		}
		return true;
	}

}
