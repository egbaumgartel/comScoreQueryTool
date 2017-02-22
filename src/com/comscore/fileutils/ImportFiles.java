package com.comscore.fileutils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.comscore.main.Constants.StringConstants;
import com.comscore.media.Duration;
import com.comscore.media.Media;
import com.comscore.media.MediaMapKey;

public class ImportFiles {
	private final String dirname;

	public ImportFiles(String directory) {
		this.dirname = directory;
	}
	
	/*
	 * Returns a map of all files and paths to those files corresponding to a given STB
	 * 
	 * @return map of STB to Path for all data files in store
	 */
	public Map<String,String> getAllFilenames() throws IOException {
        Map<String,String> stbFiles = new HashMap<String,String>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dirname))) {
            for (Path path : directoryStream) {
            	String pathString = path.toString();
            	// System.out.println("PS: " + pathString);
            	// filename convention ####-STB.json where #### is the STB ID
            	if (pathString.endsWith(StringConstants.DATASTORE_EXTENSION.value())) {
            		// Parse it down to just the STB portion of the file
            		String STB = pathString.substring(pathString.lastIndexOf('/')+1, pathString.lastIndexOf('-'));
            		//System.out.println("STB FOUND! " + path + " STB " + STB);
            		stbFiles.put(STB,path.getFileName().toString());
            	}
            }
        } catch (IOException ex) {
        	System.err.println("Error reading directory: " + dirname);
        	ex.printStackTrace();
        	throw ex;
        }
        return stbFiles;
	}
	
	/*
	 * This imports the raw data.
	 * 
	 * @return Map between MediaMapKey and full media info for a given import file
	 * @throws IOException when it can't access the file
	 */
	public Map<String,List<Media>> importFile(String filename) throws IOException {
		Map<MediaMapKey,Media> mediaMap = new LinkedHashMap<MediaMapKey,Media>();
		
		if (filename == null || filename.isEmpty()) {
			System.err.println("importFile(): no valid filename.");
			return null;
		}

		Path path = Paths.get(dirname + "/" + filename);
		List<String> fileLines = null;

		/* We're going to load all entries FROM IMPORT FILE ONLY, not the whole data store
		 * Then we'll load file for each STB to check if the entries exist
		 */
		try {
			fileLines = Files.readAllLines(path, StandardCharsets.UTF_8);  //import file only, not whole datastore
		} catch (IOException exc) {
			System.err.println("Error accessing file " + filename);
			exc.printStackTrace();
			throw exc;
		}

		int lineNum = 0;
		for (String line : fileLines) {
			// ideally there should be some sort of indicator that it's a title line, but we'll just assume we can skip the first line
			if (lineNum++ == 0) continue;
			String[] elements = line.split("\\|");
			if (elements.length != Media.Column.values().length) {
				System.err
						.println("Skipping Entry: Unable to parse file at line: "
								+ lineNum);
				// skip to the next line in file
				continue;
			}
			/*
			 * 0 = STB 
			 * 1 = TITLE 
			 * 2 = PROVIDER 
			 * 3 = DATE 
			 * 4 = REV 
			 * 5 = VIEW_TIME
			 */
			
			String[] durationElements = elements[5].split(":");
			if (durationElements.length != 2) {
				System.err
						.println("Skipping entry: Unable to parse duration at line: "
								+ lineNum);
				// skip to the next line in file
				continue;
			}
			//Duration hours:minutes, seconds set to 0
			int hours = Integer.parseInt(durationElements[0]);
			int min = Integer.parseInt(durationElements[1]);
			// Limit hours to < 24, min to < 60, as these cause problems writing/reading from DB
			if (min >= 60) {
				System.err.println("WARNING: minutes in line " + lineNum + " are 60 or greater.  Setting to 0");
				min = 0;
			}
			Duration duration = new Duration(hours, min, 0);
			String stb = elements[0];
			String title = elements[1];
			String leaseDate = elements[3];
			//Build the Media object
			Media mediaElement = null;
			
			try {
				mediaElement = new Media.Builder(stb, title,
					leaseDate).provider(elements[2])
					.revenue(Float.parseFloat(elements[4])).duration(duration)
					.build();
			} catch (NumberFormatException nfe) {
				System.err.println("Unable to parse revenue on line " + lineNum + ".  Skipping.");
				continue;
			}
			
			/*
			 * NOTE - the fact that this is a map will overwrite any previous entry in the
			 * map with the same STB, TITLE, DATE key
			 */
			MediaMapKey key = new MediaMapKey(stb,title,leaseDate);
			mediaMap.put(key, mediaElement);

		}
		
		// now media keys should be unique, separate by STB
		
		//return mediaMap;
		
		Map<String,List<Media>> mediaToFile = new HashMap<String,List<Media>>();
		for (MediaMapKey mKey : mediaMap.keySet()) {
			Media m = mediaMap.get(mKey);
			String outFile = dirname + m.getStb() + StringConstants.DATASTORE_EXTENSION.value();
			if (!mediaToFile.containsKey(outFile)) {
				mediaToFile.put(outFile, new ArrayList<Media>());
			}
			mediaToFile.get(outFile).add(m);  //add media to list
		}

		return mediaToFile;
	}
	
	/*
	 * This imports the raw data from a JSON
	 * 
	 * @param filename - read this JSON
	 * @return Map from a Media Key (STB/TITLE/DATE)
	 * 
	 */
	public List<Media> importJson(String filename) {
		List<Media> mediaList = new ArrayList<Media>();
		String jsonString = null;
		
		if (filename == null || filename.isEmpty()) {
			System.err.println("importJson(): no valid filename");
			return null;
		}

		Path path = Paths.get(dirname + "/" + filename);
		List<String> fileLines = null;
		
		//check if the file exists.  If not just return the empty list and create empty file
		if (!Files.exists(path)) {
			System.err.println("WARN: data store file not found: " + path.toString());
			return mediaList;
		}

		// We're going to load all entries FOR JUST THIS FILE
		try {
			fileLines = Files.readAllLines(path, StandardCharsets.UTF_8);
		} catch (IOException exc) {
			System.err.println("Error accessing file " + dirname);
			exc.printStackTrace();
			return null;
		}
		
		StringBuffer buffer = new StringBuffer();
		
		// put this all in one string in case file is on same line
		for (String line : fileLines) {
			buffer.append(line);
		}
		
		// now parse the JSON
		jsonString = buffer.toString();
		JSONObject jObj = (JSONObject) JSONValue.parse(jsonString);
		
		// check for empty file or parse problem, return empty object
		if (jObj == null) return mediaList;
		
		JSONArray jArray = (JSONArray) jObj.get("viewRecords");
		
		// Nothing parsable found, return empty map
		if (jArray == null) return mediaList;
		
		int elements = jArray.size();
		
		for (int i = 0; i < elements; i++) {
			JSONObject mediaObject = (JSONObject) jArray.get(i);
			String stb = (String) mediaObject.get("stb");
			String title = (String) mediaObject.get("title");
			String provider = (String) mediaObject.get("provider");
			String leaseDate = (String) mediaObject.get("leaseDate");
			// ideally try-catch this, but we're (unwisely?) trusting that we wrote to the JSON correctly in the first place.
			float revenue = Float.parseFloat((String) mediaObject.get("revenue"));
			String viewTime = (String) mediaObject.get("viewTime");
			
			Duration d = new Duration(viewTime);
			
			Media mediaElement = new Media.Builder(stb, title,
					leaseDate).provider(provider)
					.revenue(revenue).duration(d)
					.build();
			mediaList.add(mediaElement);
		}	
		
		return mediaList;
	}
	
	
	
	
	@SuppressWarnings("unchecked")  // NOTE - this is due to the JSON libraries, it would be nice if we could allow those warnings
	/*
	 * Write media to specified file, file should have been set via STB prior to this call
	 * 
	 * @param filename - filename to write
	 * @param media - list of media to write to the filename
	 * 
	 */
	public boolean writeMedia(String filename, List<Media> media) {

		
		JSONArray viewList = new JSONArray();
		JSONObject json = new JSONObject(); 
		
		for (Media mediaElement : media) {
			Map<String,String> record=new LinkedHashMap<String,String>();
			   record.put("stb",mediaElement.getStb());
			   record.put("title",mediaElement.getTitle());
			   record.put("provider",mediaElement.getProvider());
			   record.put("leaseDate",mediaElement.getLeaseDate());
			   record.put("revenue",String.format("%.02f",mediaElement.getRevenue()));
			   record.put("viewTime",mediaElement.getViewTime().toTimeString());
			   viewList.add(record);
		}
		
		json.put("viewRecords", viewList);
		// System.out.println(json.toString()); // To string method prints it with specified indentation.
		
		try {
			PrintWriter out = new PrintWriter(filename);
			out.println(json.toString());
			out.close();
		} catch (FileNotFoundException fnfe) {
			// TODO Auto-generated catch block
			System.err.println("File: " + filename + " not accessible for writing.");
			fnfe.printStackTrace();
			return false;
		}

		return true;
	}
}
