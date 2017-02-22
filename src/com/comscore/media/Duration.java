package com.comscore.media;

import java.util.concurrent.TimeUnit;

public class Duration {
	private int hours = 0;
	private int min = 0;
	private int sec = 0;

	// take a time in milliseconds and convert to hours/min/sec
	public Duration(long millis) {
		hours = (int) TimeUnit.MILLISECONDS.toHours(millis);
		min = (int) (TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS
				.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
		sec = (int) (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES
				.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
	}
	
	public Duration(int hours, int min, int sec) {
		this.hours = hours;
		this.min = min;
		this.sec = sec;
	}
	
	//expects a string of HHH:MM:SS, not necessarily 0 left padded
	public Duration(String timeString) {
		String[] durationElements = timeString.split(":");
		if (durationElements.length != 3) {
			System.err.println("Duration: duration string passed to constructor does not have correct format.");
		}
		
		try {
			this.hours = Integer.parseInt(durationElements[0]);
			this.min = Integer.parseInt(durationElements[1]);
			this.sec = Integer.parseInt(durationElements[2]);
		} catch (NumberFormatException nfe) {
			System.err.println("Duration: NaN for duration string in constructor");
			nfe.printStackTrace();
		}
	}

	// getters only, immutable objects are safer
	public int getHours() {
		return hours;
	}

	public int getMin() {
		return min;
	}

	public int getSec() {
		return sec;
	}
	
	public int toSeconds() {
		int timeSec = 0;
		timeSec += hours * 60 * 60;
		timeSec += min * 60;
		timeSec += sec;
		return timeSec; 
		
	}
	
	public String toString() {
		return "Duration: " + String.format("%02d", hours) + "h " + String.format("%02d", min) + "m " + String.format("%02d", sec) + "s";
	}
	
	public String toTimeString() {
		return String.format("%02d", hours) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec);
	}

}
