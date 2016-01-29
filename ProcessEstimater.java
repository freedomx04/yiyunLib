package org.tsaikd.java.utils;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessEstimater {

	static Log log = LogFactory.getLog(ProcessEstimater.class);

	private int SLOT_SIZE;
	private int SLOT_GAPTIME;
	private long[] numList;
	private long[] timeList;
	private long max = 0;
	private long skip = 0;
	private int slot = -1;
	private String outputFormat = "%1$s";

	public ProcessEstimater(int slotSize, int slotGapTime, long max, long skip) {
		init(slotSize, slotGapTime, max, skip);
	}

	public ProcessEstimater(long max, long skip) {
		init(30, 1000, max, skip);
	}

	public ProcessEstimater(long max) {
		init(30, 1000, max, 0);
	}

	private void init(int slotSize, int slotGapTime, long max, long skip) {
		this.SLOT_SIZE = 30;
		this.SLOT_GAPTIME = 1000;
		numList = new long[SLOT_SIZE];
		timeList = new long[SLOT_SIZE];

		this.max = max;
		this.skip = skip;
	}

	public synchronized ProcessEstimater setNum(long num) {
		long now = System.currentTimeMillis();
		if (slot < 0) {
			slot = 0;
			for (int i=0 ; i<SLOT_SIZE ; i++) {
				numList[i] = num;
				timeList[i] = now;
			}
		} else {
			if ((now - timeList[(slot + SLOT_SIZE - 1) % SLOT_SIZE]) > SLOT_GAPTIME) {
				slot = (slot + 1) % SLOT_SIZE;
			}
			numList[slot] = num;
			timeList[slot] = now;
		}
		return this;
	}

	public long getNum() {
		if (slot < 0) {
			return 0;
		}
		return numList[slot];
	}

	public ProcessEstimater addNum(long add) {
		return setNum(getNum() + add);
	}

	public ProcessEstimater addNum() {
		return addNum(1);
	}

	public ProcessEstimater setRestNum(long rest) {
		long num = max - skip - rest;
		return setNum(num);
	}

	public long getRestNum() {
		return max - getNum();
	}

	private long getRestTime() {
		if (slot < 0) {
			return -1;
		}
		int slotcmp = (slot + 1) % SLOT_SIZE;
		long diffTime = timeList[slot] - timeList[slotcmp];
		long diffNum = numList[slot] - numList[slotcmp];
		if (diffNum < 1) {
			return 0;
		}
		return getRestNum() * diffTime / diffNum;
	}

	public String getRestString() {
		long rest = getRestTime();
		rest = Math.max(rest, 0);

		int msec = (int) (rest % 1000);
		rest /= 1000;

		int sec = (int) (rest % 60);
		rest /= 60;

		int min = (int) (rest % 60);
		rest /= 60;

		int hr = (int) rest;

		if (hr > 0) {
			return String.format("%1$02d:%2$02d:%3$02d", hr, min, sec, msec);
		} else if (min > 0) {
			return String.format("%2$02d:%3$02d", hr, min, sec, msec);
		} else {
			return String.format("%3$02d.%4$03d", hr, min, sec, msec);
		}
	}

	/**
	 * Set format of output string <BR/>
	 *   %1$s   : rest time <BR/>
	 *   %2$d   : cur num <BR/>
	 *   %3$d   : rest num <BR/>
	 *   %4$d   : max num <BR/>
	 *   %5$.2f : 100.0 * (cur num) / (max num) <BR/>
	 *   %6$.2f : 100.0 * (rest num) / (max num) <BR/>
	 *   default : "%1$s" <BR/>
	 *   example : "%2$d / %4$d (%5$.2f%%) , Rest time: %1$s"
	 * @param format
	 */
	public ProcessEstimater setFormat(String format) {
		this.outputFormat = format;
		return this;
	}

	public ProcessEstimater setFormatDefNum() {
		return setFormat("%2$d / %4$d (%5$.2f%%) , Rest time: %1$s");
	}

	public ProcessEstimater setFormatDefRest() {
		return setFormat("%3$d / %4$d (%6$.2f%%) , Rest time: %1$s");
	}

	@Override
	public String toString() {
		long num, rest;
		float curPercent, restPercent;
		num = getNum();
		rest = getRestNum();
		if (max > 0) {
			curPercent = (float) 100.0 * num / max;
			restPercent = (float) 100.0 * rest / max;
		} else {
			curPercent = restPercent = 0;
		}
		return String.format(outputFormat, getRestString(), num, rest, max, curPercent, restPercent);
	}

	private Date prevMsg = null;
	public ProcessEstimater debug(Log log, long minTime, String msg) {
		if (minTime > 0 && prevMsg != null && prevMsg.after(new Date())) {
			return this;
		}
		prevMsg = new Date(new Date().getTime() + minTime);
		if (msg != null) {
			log.debug(toString() + " " + msg);
		} else {
			log.debug(toString());
		}
		return this;
	}

	public ProcessEstimater debug(Log log, long minTime) {
		return debug(log, minTime, null);
	}

	public ProcessEstimater debug(Log log, String msg) {
		return debug(log, 0, msg);
	}

	public ProcessEstimater debug(Log log) {
		return debug(log, 0, null);
	}

}
