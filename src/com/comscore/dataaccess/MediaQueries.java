package com.comscore.dataaccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.comscore.media.Media;
import com.comscore.media.MediaCompare;
import com.comscore.media.Media.Column;

public class MediaQueries {

	public MediaQueries() {
		super();
	}

	// Test to see if an input parameter is a valid part of the Enum
	protected boolean isColumn(String s) {

		try {
			@SuppressWarnings("unused")
			Column c = Column.valueOf(s);
			return true;
		} catch (IllegalArgumentException ex) {
			System.out.println("WARNING: " + s
					+ " is not a valid column specifier. Ignoring.");
			return false;
		}
	}

	/*
	 * Return only matching records from the data store
	 * 
	 * @param media - list of Media Objects
	 * @param filter - string corresponding to command line filters
	 * @return - filtered list of media, sub-list of original.
	 */
	public List<Media> filter(List<Media> media,
			String filter) {
		List<Media> filteredMedia = new ArrayList<Media>();

		if (filter == null)
			return media;

		/*
		 * We're using Java7. Java8 has a thing called 'Predicates' which could
		 * probably do this in a more elegant manner. Also, this only does an
		 * AND on the provided filters.
		 */

		String[] filters = filter.split(",");

		/*
		 * Note this sticks us with the 6 elements present in the Media object,
		 * and isn't terribly generic for any object.
		 */

		Map<Column, String> filtersMap = new LinkedHashMap<Column, String>();

		// Match the filters to a column (if possible) and set the filter value
		for (int i = 0; i < filters.length; i++) {
			String[] columnFilter = filters[i].split("=");
			
			if (!isColumn(columnFilter[0])) continue;
			
			filtersMap.put(Column.valueOf(columnFilter[0]), columnFilter[1]);

		}

		// check to make sure there are valid filters
		if (filtersMap.isEmpty())
			return media;

		/*
		 * Eliminate non-matching rows named loop since we want to break out of
		 * the inner loop entirely and start the next iteration of the outer
		 * loop
		 */
		medialoop: for (Media m : media) {
			// weed out the ones we don't want
			for (Column c : filtersMap.keySet()) {
				if (!filtersMap.get(c).equals(m.getAsString(c)))
					continue medialoop;
			}

			// we've passed the test, add to filtered media

			filteredMedia.add(m);
		}

		return filteredMedia;
	}

	/*
	 * Ordering the lists, uses Java Collections sort. By sorting last column
	 * specified first, and the Collections.sort will not reorder equal
	 * elements, it should have the effect of having it ordered first by the
	 * first column, then second, then...
	 * 
	 * @param media - list of Media Objects
	 * @param order - string corresponding to command line column ordering
	 * @return - ordered list of media.
	 */
	public List<Media> order(List<Media> media,
			String order) {
		List<Media> orderedMedia = new ArrayList<Media>();

		String[] orders = order.split(",");

		/*
		 * LinkedHashMap maintains order. Order last to first, reversing what's
		 * on the command line
		 */
		List<String> orderList = Arrays.asList(orders);
		Collections.reverse(orderList);
		// turn the media map into a list
		ArrayList<Media> mediaList = new ArrayList<Media>(media);

		for (String s : orderList) {
			// Collections.sort - equal elements will not be reordered
			if (isColumn(s))
				Collections
						.sort(mediaList, new MediaCompare(Column.valueOf(s)));
		}

		// need to add this back into the new (ordered) HashMap
		for (Media m : mediaList) {
			orderedMedia.add(m);
		}
		
		return orderedMedia;
	}

	/*
	 * Overloaded method to print media
	 * 
	 * @param media - media to print to STDOUT
	 */
	public void printMedia(List<Media> media) {
		printMedia(media,null);
	}
	
	/*
	 * Display method, implements printing to console and the -s switch to command line
	 * which determines which columns to display.
	 * 
	 * @param media - list of media objects
	 * @param columns, string passed in from command line -s method
	 * 
	 */
	public void printMedia(List<Media> media, String columns) {
		boolean showAll = false;

		if (columns == null) {
			showAll = true;

		}

		String[] displayColumns = null;

		if (!showAll) {
			displayColumns = columns.split(",");		
		}

		// Reminder - LinkedHashMap is ordered
		Map<Column, String> columnMap = new LinkedHashMap<Column, String>();

		// set show all columns - value "true" is just a dummy because we care
		// about the key only
		if (showAll) {
			for (Column c : Column.values()) {
				columnMap.put(c, "true");
			}

		} else {
			for (int i = 0; i < displayColumns.length; i++) {
				if (isColumn(displayColumns[i])) {
					columnMap.put(Column.valueOf(displayColumns[i]), "true");
				}
			}
		}

		System.out.print("Columns: ");
		/*
		 * Java 8 may have some better ways to do this, using join
		 */
		//int colNum = 0;
		StringJoiner joiner = new StringJoiner(",");
		for (Column c : columnMap.keySet()) {
			joiner.add(c.name());
		}
		System.out.println(joiner.toString());

		for (Media m : media) {
			//colNum = 0;
			joiner = new StringJoiner(",");  //reset this
			for (Column c : columnMap.keySet()) {
				joiner.add(m.getAsString(c));
			}
			
			System.out.println(joiner.toString());

		}

	}

}
