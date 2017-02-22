package com.comscore.main;

// best practice for making sure your constants are of the right type, also makes constructing them easier
public class Constants {
		
		public enum StringConstants {
			DATASTORE_EXTENSION("-STB.json");
			
			private final String stringValue;
			
			StringConstants(String value) {
				this.stringValue = value;
			}
			public String value() {return stringValue;}
		}

}
