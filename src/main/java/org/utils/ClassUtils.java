package org.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassUtils {

	static Log log = LogFactory.getLog(ClassUtils.class);

	public static String getClassName() {
		return getClassName(false, 0);
	}

	public static String getClassName(boolean full, int offset) {
		String name;
		StackTraceElement[] stackTraceElement = new Throwable().getStackTrace();
		for (StackTraceElement stack : stackTraceElement) {
			name = stack.getClassName();
			if (name.equals("org.tsaikd.java.utils.ClassUtils")) {
				continue;
			}
			if (offset > 0) {
				offset--;
				continue;
			}

			if (!full) {
				name = getLastName(name);
			}
			return name;
		}

		return null;
	}

	public static String getLastName(String fullName) {
		String[] names = fullName.split("\\.");
		return names[names.length-1];
	}

}
