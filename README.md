
# 1. Description
################################################################################
This program contains solutions to sections 2 and 2.1 of the aforementioned 
problem.

The solution is implemented in Java.  It requires two libraries for the code 
itself (included and automatically downloadable with the gradle script):

commons-cli  (Apache project for parsing command line options, like getopt in C)
json-simple  (Google project for a simple, lightweight encoding and decoding of 
	JSON data)

maven 'package' will build the dependences into the jar.

java -jar comScore-0.0.1-SNAPSHOT-jar-with-dependencies.jar  serves as both the 
data loader and query tool, depending on command line arguments.  
-l <file> will put it in load mode.

The datastore is implemented in JSON.  This made sense as it is a simple, 
readable format, although the data file is a single line and not pretty-printed.  
JSON also permits ease of use with other applications, including web and 
browser-based tools.

Cut and paste into http://jsonprettyprint.com/ if you need to view it in a more 
readable format.

# 2. Building
################################################################################

Utility requires JDK 8 or higher.

Maven goal 'package' should build the jar with dependencies, running JUnit 
tests.

Under the 'target' directory, it will create:

comScore-0.0.1-SNAPSHOT-jar-with-dependencies.jar

Which can be run as a jar with a Main class (com.comscore.main.MediaAccess) 
specified in the Manifest.

mvn clean
mvn package



# 3. Execution
################################################################################

JDK/JRE 8 is required.  These assume you're in the 'target' directory of the 
archive or have copied off the jar-with-dependencies somewhere else.

Any missing or wrong parameter prints the help message.


gregb@saturnus ~/workspace/comScore/target $ 
	java -jar comScore-0.0.1-SNAPSHOT-jar-with-dependencies.jar -halp
usage: MediaUtil
 -d,--datastore <arg>   directory for datastore (required for all)
 -f,--filter <arg>      filter by COLUMN=VALUE
 -l,--load-file <arg>   load entries from file
 -o,--order-by <arg>    comma separated columns to order by
 -s,--select <arg>      comma separated list of columns to select

Columns: STB,TITLE,PROVIDER,DATE,REV,VIEW_TIME

By default the program will print all columns for all entries in the data 
stores, as there is no filter on the query.  
Since it is a query, the assumption is we can build a list of all the results, 
even if they come from all data stores.

The -d is required, and lets the program know the location of the datastore 
JSON(s).  The obvious convention is to have a .json file for this.  JSON files 
are separated by STB, all entries for one STB go into one file.  This allows 
only partial data from one input file to be loaded into memory during a new 
load, if entries are more or less split evenly between STB's.

During a load (-l) of new data in the input format, new JSON file for each STB 
is created if needed.

-l loads the pipe-delimited file specified in the argument into the JSON data 
store.  Previous entries with the STB,TITLE,DATE key from either the JSON store 
or the same pipe-delimited file are overwritten.

Example, showing data directory off of home directory (Unix), assuming input 
files are moved to ~/data-input/.  These will need to be run before query 
examples below will work.

java -jar comScore-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d ~/data/ -l ~/data-input/stb_data.txt
java -jar comScore-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d ~/data/ -l ~/data-input/stb_data2.txt

*NOTE*: for filters which contain a space in the field to filter by, please 
enclose the parameter in single quotes:

java -jar comScore-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d ~/data/ -f 'PROVIDER=warner bros,STB=stb5'

-f is a comma separated list of filters with FIELD1=value1,FIELD2=value2.
-o is a list of columns to order by, these columns will always order, but may 
	not be displayed depending on -s
-s is a list of columns to display.  They will display in the order specified 
	to -s

Examples:

java -jar comScore-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d ~/data/ -f 'PROVIDER=warner bros' -o REV,VIEW_TIME
java -jar comScore-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d ~/data/ -f 'PROVIDER=warner bros' -o REV,VIEW_TIME -s TITLE,PROVIDER,DATE

Assuming we've copied the jar-with-dependences into ~/workspace/tmp

gregb@saturnus:~/workspace/tmp$ java -jar comScore-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d ~/data/ -f 'PROVIDER=warner bros' -o REV,VIEW_TIME -s VIEW_TIME,STB,REV
Columns: VIEW_TIME,STB,REV
01:30:00,stb1,4.00
01:30:00,stb4,4.00
02:45:00,stb5,8.00
02:45:00,stb2,99.00
22:45:00,stb4,99.00


Invalid columns in params will generate a warning then are ignored.

gregb@saturnus:~/workspace/tmp$ java -jar comScore-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d ~/data/ -f 'PROVIDER=warner bros' -o REV,VIEW_TIME -s TITLE,PROVIDER,DAT
WARNING: DAT is not a valid column specifier. Ignoring.
Columns: TITLE,PROVIDER
the matrix,warner bros
the matrix,warner bros
the hobbit,warner bros
the hobbit,warner bros
the hobbit,warner bros



