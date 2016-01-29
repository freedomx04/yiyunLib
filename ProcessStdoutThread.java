package org.tsaikd.java.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessStdoutThread extends Thread {

	static Log log = LogFactory.getLog(ProcessStdoutThread.class);
	Process prog = null;
	Type type = Type.Stdout;

	public enum Type {
		Stdout,
		Stderr,
	}

	public ProcessStdoutThread(Process prog) {
		this.prog = prog;
	}

	public ProcessStdoutThread(Process prog, Type type) {
		this.prog = prog;
		this.type = type;
	}

	public void run() {
		String line;
		try {
			InputStream is;
			BufferedReader brCleanUp;

			switch (type) {
			case Stderr:
				is = prog.getErrorStream();
				brCleanUp = new BufferedReader(new InputStreamReader(is));
				while ((line = brCleanUp.readLine ()) != null) {
					log.error(line);
				}
				break;
			case Stdout:
			default:
				is = prog.getInputStream();
				brCleanUp = new BufferedReader(new InputStreamReader(is));
				while ((line = brCleanUp.readLine ()) != null) {
					log.info(line);
				}
				break;
			}

			brCleanUp.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
