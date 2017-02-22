package com.comscore.media;

public class Media {
	
	public enum Column {
	    STB, TITLE, PROVIDER, DATE,
	    REV, VIEW_TIME 
	}

	private String stb = null;
	private String title = null;
	private String provider = null;
	private String leaseDate = null;
	float revenue = 0;
	private Duration viewTime = null;
	
	/*
	 * Caller is forced to use the Builder paradigm due to one constructor
	 */
	
	// Constructor for use with Builder
	private Media(Builder builder) {
		stb = builder.stb;
		title = builder.title;
		provider = builder.provider;
		leaseDate = builder.leaseDate;
		revenue = builder.revenue;
		viewTime = builder.viewTime;
	}

	/*
	 * Best practice for instantiating an object in a more atomic manner - inner class
	 * Media myMedia = new Media.Builder(stb, title, leaseDate).provider(provider)
	 *   					.revenue(rev).duration(duration)
	 *   					.build();
	 */
	public static class Builder {
		private final String stb;
		private final String title;
		private final String leaseDate;
		
		//parameters not needed at time of instantiation
		private String provider = null;
		float revenue = 0;
		private Duration viewTime = null;
		
		public Builder(String stb, String title, String leaseDate) {
			this.stb = stb;
			this.title = title;
			this.leaseDate = leaseDate;
		}		
		public Builder provider(String val) {
			provider = val;
			return this;
		}		
		public Builder revenue(float val) {
			revenue = val;
			return this;
		}
		public Builder duration(Duration val) {
			viewTime = val;
			return this;
		}
		
		public Media build() {
			return new Media(this);
		}
		
	}
	
	// Classic getters.  No setters to make object more immutable and secure
	public String getStb() {
		return stb;
	}


	public String getTitle() {
		return title;
	}


	public String getProvider() {
		return provider;
	}


	public String getLeaseDate() {
		return leaseDate;
	}


	public float getRevenue() {
		return revenue;
	}


	public Duration getViewTime() {
		return viewTime;
	}
	
	public String getAsString(Column c) {
		switch (c) {
		case STB: return this.getStb();
		case TITLE: return this.getTitle();
		case PROVIDER: return this.getProvider();
		case DATE: return this.getLeaseDate();
		case REV: return String.format("%.02f", this.getRevenue());
		case VIEW_TIME: return this.getViewTime().toTimeString();
		default: return null;
		}
	}
	
	public MediaMapKey getKey() {
		return new MediaMapKey(stb, title, leaseDate);
	}
	
	public String toString() {
		return "Media Object: [\n" +
				"stb: " + stb + "\n" +
				"title: " + title + "\n" +
				"provider: " + provider + "\n" +
				"leaseDate: " + leaseDate + "\n" +
				"revenue: " + String.format("%.2f", revenue) + "\n" +
				"viewTime: " + viewTime.toTimeString() + "]\n";
	}
	
}
