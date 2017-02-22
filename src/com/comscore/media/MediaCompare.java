package com.comscore.media;

import java.util.Comparator;

import com.comscore.media.Media.Column;

// Inner classes for sorting Media by a specific column
public class MediaCompare implements Comparator<Media>{
	
	Column compareBy = null;
	
	public MediaCompare(Column c) {
		compareBy = c;
	}
	 
    @Override
    public int compare(Media m1, Media m2) {
		switch (compareBy) {
		case STB: case TITLE: case PROVIDER: case DATE:
			return m1.getAsString(compareBy).compareTo(m2.getAsString(compareBy));
		case REV:
			if (m1.getRevenue() == m2.getRevenue()) return 0;
			if (m1.getRevenue() < m2.getRevenue()) return -1;
			else return 1;
		case VIEW_TIME:
			if (m1.getViewTime().toSeconds() == m2.getViewTime().toSeconds()) return 0;
			if (m1.getViewTime().toSeconds() < m2.getViewTime().toSeconds()) return -1;
			else return 1;
		default:
			return 0;
		}
    	
    }
}