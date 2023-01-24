package me.sirrus86.s86powers.utils;

import me.sirrus86.s86powers.localization.LocaleString;

public class PowerTime {

	private final static byte MILLISECONDS_PER_TICK = 50;
	private final static short MILLISECONDS_PER_SECOND = 1000;
	private final static int MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * 60;
	private final static int MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * 60;
	private final static int MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * 24;
	
	public static String asClock(long millis, boolean showDays, boolean showHours, boolean showMinutes, boolean showSeconds, boolean showMillis) {
		String tmp = "";
		long time = millis, remain;
		if (millis >= MILLISECONDS_PER_DAY
				|| showDays) {
			if (time >= MILLISECONDS_PER_DAY) {
				remain = time / MILLISECONDS_PER_DAY;
				tmp = tmp + remain + ":";
				time %= MILLISECONDS_PER_DAY;
			}
		}
		if (millis >= MILLISECONDS_PER_HOUR
				|| showHours) {
			if (time >= MILLISECONDS_PER_HOUR) {
				remain = time / MILLISECONDS_PER_HOUR;
				tmp = tmp + (remain > 9L ? remain : "0" + remain) + ":";
				time %= MILLISECONDS_PER_HOUR;
			}
			else {
				tmp = tmp + "00:";
			}
		}
		if (millis >= MILLISECONDS_PER_MINUTE
				|| showMinutes) {
			if (time >= MILLISECONDS_PER_MINUTE) {
				remain = time / MILLISECONDS_PER_MINUTE;
				tmp = tmp + (remain > 9L ? remain : "0" + remain) + ":";
				time %= MILLISECONDS_PER_MINUTE;
			}
			else {
				tmp = tmp + "00:";
			}
		}
		if (millis >= MILLISECONDS_PER_SECOND
				|| showSeconds) {
			if (time >= MILLISECONDS_PER_SECOND) {
				remain = time / MILLISECONDS_PER_SECOND;
				tmp = tmp + (remain > 9L ? remain : "0" + remain);
				time %= MILLISECONDS_PER_SECOND;
			}
			else {
				tmp = tmp + "00:";
			}
		}
		if (time > 0 && showMillis) {
			remain = time;
			tmp = tmp + "." + (remain > 99L ? "" : "0") + (remain > 9L ? "" : "0") + remain;
		}
		if (tmp.endsWith(":")) {
			tmp = tmp.substring(0, tmp.lastIndexOf(":"));
		}
		return tmp;
	}
	
	public static String asLongString(long millis) {
		String tmp = "";
		long time = millis;
		if (time >= MILLISECONDS_PER_DAY) {
			tmp = tmp + time / MILLISECONDS_PER_DAY + ((time / MILLISECONDS_PER_DAY) == 1 ?
					" " + LocaleString.DAY + " " :
					" " + LocaleString.DAYS + " ");
			time %= MILLISECONDS_PER_DAY;
		}
		if (time >= MILLISECONDS_PER_HOUR) {
			tmp = tmp + time / MILLISECONDS_PER_HOUR + ((time / MILLISECONDS_PER_HOUR) == 1 ?
					" " + LocaleString.HOUR + " " :
					" " + LocaleString.HOURS + " ");
			time %= MILLISECONDS_PER_HOUR;
		}
		if (time >= MILLISECONDS_PER_MINUTE) {
			tmp = tmp + time / MILLISECONDS_PER_MINUTE + ((time / MILLISECONDS_PER_MINUTE) == 1 ?
					" " + LocaleString.MINUTE + " " :
					" " + LocaleString.MINUTES + " ");
			time %= MILLISECONDS_PER_MINUTE;
		}
		if (time >= MILLISECONDS_PER_SECOND) {
			tmp = tmp + time / MILLISECONDS_PER_SECOND + ((time / MILLISECONDS_PER_SECOND) == 1 ?
					" " + LocaleString.SECOND + " " :
					" " + LocaleString.SECONDS + " ");
		}
		if (tmp.equalsIgnoreCase("")) {
			return LocaleString.LESS_THAN_ONE_SECOND.toString();
		}
		else {
			tmp = tmp.substring(0, tmp.lastIndexOf(" "));
			return tmp;
		}
	}
	
	/**
	 * Converts a more familiar time format into milliseconds.
	 * Expected format is days, hours, minutes, seconds, milliseconds.
	 * For example, PowerTime.toMillis(3, 30, 0) will return the number of milliseconds in 3 minutes, 30 seconds.
	 * @param time - How much time to calculate.
	 * @return Time in milliseconds
	 */
	public static long toMillis(int... time) {
		int millis = 0;
		for (int i = 0; i < time.length; i ++) {
			if (i == time.length - 1) {
				millis += time[i];
			}
			else if (i == time.length - 2) {
				millis += time[i] * MILLISECONDS_PER_SECOND;
			}
			else if (i == time.length - 3) {
				millis += time[i] * MILLISECONDS_PER_MINUTE;
			}
			else if (i == time.length - 4) {
				millis += time[i] * MILLISECONDS_PER_HOUR;
			}
			else if (i == time.length - 5) {
				millis += time[i] * MILLISECONDS_PER_DAY;
			}
		}
		return millis;
	}
	
	public static long toMillis(String time) {
		int millis = 0;
		if (time.contains(":")) {
			String[] times = time.split(":");
			for (int i = 0; i < times.length; i ++) {
				if (i == times.length - 1) {
					if (times[i].contains(".")) {
						String[] secs = times[i].split("\\.");
						millis += Integer.parseInt(secs[0]);
						millis += Integer.parseInt(secs[1].substring(0, 2));
					}
					else {
						millis += Integer.parseInt(times[i]) * MILLISECONDS_PER_SECOND;
					}
				}
				else if (i == times.length - 2) {
					millis += Integer.parseInt(times[i]) * MILLISECONDS_PER_MINUTE;
				}
				else if (i == times.length - 3) {
					millis += Integer.parseInt(times[i]) * MILLISECONDS_PER_HOUR;
				}
				else if (i == times.length - 4) {
					millis += Integer.parseInt(times[i]) * MILLISECONDS_PER_DAY;
				}
			}
			return millis;
		}
		else {
			return Long.parseLong(time);
		}
	}
	
	public static long toTicks(long millis) {
		return millis / MILLISECONDS_PER_TICK;
	}
	
	public static int toTicks(int... time) {
		return (int) toTicks(toMillis(time));
	}
	
}
