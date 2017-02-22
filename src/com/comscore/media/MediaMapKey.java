package com.comscore.media;

/*
 * Object functioning as a primary key for media in the datastore.
 */
public class MediaMapKey {
	
	private String stb;
	private String title;
	private String date;
	
	/*
	 * Initialize once with constructor.
	 */
	public MediaMapKey(String stb, String title, String date) {
		this.stb = stb;
		this.title = title;
		this.date = date;
	}

	// strings are all immutable
	public String getStb() {
		return stb;
	}

	public String getTitle() {
		return title;
	}

	public String getDate() {
		return date;
	}

	@Override
    public boolean equals(Object obj) {
        if(obj instanceof MediaMapKey) {
            MediaMapKey m = (MediaMapKey)obj;
            return stb.equals(m.stb) && title.equals(m.title) && date.equals(m.date);
        }
        return false;
    }

    @Override
    public int hashCode() {
    	/* 
    	 * note - the chance of collisions here where concatenation A,B,C = AB,C or A,BC hashes
    	 * the same is close to 0, given the nature of the data.  i.e for two strings | divided
    	 * 
    	 * paramount|movie = para|mountmovie
    	 * 
    	 * Adding ~ as a separator only helps this.
    	 */
        return (stb + "~" + title + "~" + date).hashCode();
    }
	
}
