package com.comscore.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.*;

import com.comscore.dataaccess.MediaQueries;
import com.comscore.fileutils.ImportFiles;
import com.comscore.media.Media;

public class MediaAccess {

	public MediaAccess() {
		// no-op constructor
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		parseOptions(args);


	}

	private static void parseOptions(String[] args) {
		// create the command line parser
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		ImportFiles importFiles = null;
		MediaQueries mq = new MediaQueries();

		// create the Options
		Options options = new Options();
		options.addOption( "l", "load-file", true, "load entries from file" );
		options.addOption( "s", "select", true, "comma separated list of columns to select" );
		options.addOption( "o", "order-by", true, "comma separated columns to order by ");
		options.addOption( "f", "filter", true, "filter by COLUMN=VALUE ");
		options.addOption( "d", "datastore", true, "directory for datastore (required for all) ");
		// The following is the default with nothing but -d specified
		//options.addOption( "a", "dump-all", false, "dump all records");



		try {
			// parse the command line arguments
			CommandLine line = parser.parse( options, args );
			List<Media> displayMedia = new ArrayList<Media>();


			if( line.hasOption( "d" ) ) {
				// set up the data store directory
				importFiles = new ImportFiles(line.getOptionValue( "d" ));

			} else {
				System.out.println("-d or --datastore is a required parameter for specifying the datastore directory.");
				return;
			}

			// option l - if this is present rest are ignored
			if( line.hasOption( "l" ) ) {
				// print the value of load file
				if (line.hasOption("s") || line.hasOption("o") || line.hasOption("f")) {
					System.out.println("Options -s, -o, and -f ignored with -l");
				}
				
				System.out.println( "Load file: " + line.getOptionValue( "l" ) );

				try {
					// get list of map between filenames and media objects
					Map<String,List<Media>> importMedia = importFiles.importFile(line.getOptionValue( "l" ));

					for (String filename : importMedia.keySet()) {
						importFiles.writeMedia(filename, importMedia.get(filename));
					} 
				} catch (IOException ioe) {
					System.out.println("Unable to load file: " + line.getOptionValue( "l" ));
					return;
				}

				System.out.println("Load completed.");
				return;
			}

			/*
			 * Important to do in this order.  Filter, Order, Select
			 */

			/*
			 * This second implements filtering if requested, otherwise gets everything.
			 */
			try {
				Map<String,String> files = importFiles.getAllFilenames();

				//iterate over each STB file found, then search that file
				for (String stb : files.keySet()) {
					String filename = files.get(stb);
					List<Media> importMedia = importFiles.importJson(filename);
					if (line.hasOption("f")) {
						displayMedia.addAll(mq.filter(importMedia, line.getOptionValue( "f" )));
					} else {			
						/*
						 * We're not filtering here, which means everything in the data store is loaded, in contradiction
						 * with item 1) which says the whole datastore is too big for memory.  However, item 2) says
						 * the result of queries can fit in memory, and if no filter is present, then all entries will be
						 * loaded.  Thus this fits with a query with no filter, and conforms to requirements in 2)
						 */
						displayMedia.addAll(importMedia);
					}
				}
			}  catch (IOException ioe) {
				System.out.println("Unable to read data store");
				System.exit(1);
			}


			if( line.hasOption( "o" ) ) {

				displayMedia = new ArrayList<Media>(mq.order(displayMedia,
						line.getOptionValue( "o" )));

			}

			if( line.hasOption( "s" ) ) {
				// print the value of select
				// System.out.println( "select: " + line.getOptionValue( "s" ) );
				mq.printMedia(displayMedia, line.getOptionValue( "s" ));
			} else {
				mq.printMedia(displayMedia);
			}


		}
		catch( ParseException exp ) {
			//System.out.println( "Unexpected exception: " + exp.getMessage() );
			formatter.printHelp( "MediaUtil", options );
		}

		return;
	}

}
