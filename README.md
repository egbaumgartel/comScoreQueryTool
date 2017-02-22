
# 1. Description
##################################################################################
This program contains solutions to sections 2 and 2.1 of the aforementioned problem.

The solution is implemented in Java.  It requires two libraries for the code itself (included and automatically downloadable with the gradle script):

commons-cli  (Apache project for parsing command line options, like getopt in C)
json-simple  (Google project for a simple, lightweight encoding and decoding of JSON data)

maven 'package' will build the dependences into the jar.

The datastore is implemented in JSON.  This made sense as it is a simple, readable format, although the data file is a single line and not pretty-printed.  JSON also permits ease of use with other applications, including web and browser-based tools.

Cut and paste into http://jsonprettyprint.com/ if you need to view it in a more readable format.

# 2. Building
###################################################################################

Utility requires JDK 8 or higher.

Maven goal 'package' should build the jar with dependencies



# 3. Execution
####################################################################################

Any missing or wrong parameter prints the help message.

gregb@saturnus:~/workspace/tmp$ comScore/bin/comScore -halps
usage: comScore
 -d,--datastore <arg>   datastore (required)
 -f,--filter <arg>      filter by COLUMN=VALUE
 -l,--load-file <arg>   load entries from file
 -o,--order-by <arg>    comma separated columns to order by
 -s,--select <arg>      comma separated list of columns to select


Columns: STB,TITLE,PROVIDER,DATE,REV,VIEW_TIME

By default the program will print all columns for all entries in the data store (in the case of -l, after the file is loaded).

the -d is required, and lets the program know the location of the datastore JSON.  The obvious convention is to have a .json file for this.  If it does not exist, it is created empty, until -l loads data into it.

-l loads the pipe-delimited file specified in the argument into the JSON data store.  Previous entries with the STB,TITLE,DATE key from either the JSON store or the same pipe-delimited file are overwritten.  Loading a file:

comScore/bin/comScore -d ~/data/media.json -l ~/data/stb_data2.txt

Note that other options can be added to the -l: -s, -o, and -f will be applied after the file is loaded and all entries are accessible.

*NOTE*: for filters which contain a space in the field to filter by, please enclose the parameter in single quotes:

comScore/bin/comScore -d ~/data/media.json -f 'PROVIDER=warner bros,STB=stb5'

-f is a comma separated list of filters with FIELD1=value1,FIELD2=value2.
-o is a list of columns to order by, these columns will always order, but may not be displayed depending on -s
-s is a list of columns to display.  They will display in the order specified to -s

Examples:

comScore/bin/comScore -d ~/data/media.json -f 'PROVIDER=warner bros' -o REV,VIEW_TIME
comScore/bin/comScore -d ~/data/media.json -f 'PROVIDER=warner bros' -o REV,VIEW_TIME -s TITLE,PROVIDER,DATE

gregb@saturnus:~/workspace/tmp$ comScore/bin/comScore -d ~/data/media.json -f 'PROVIDER=warner bros' -o REV,VIEW_TIME -s VIEW_TIME,STB,REV
Columns: VIEW_TIME,STB,REV
01:30:00,stb1,4.00
01:30:00,stb4,4.00
02:45:00,stb5,8.00
02:45:00,stb2,99.00
22:45:00,stb4,99.00


Invalid columns in params will generate a warning then are ignored.

gregb@saturnus:~/workspace/tmp$ comScore/bin/comScore -d ~/data/media.json -f 'PROVIDER=warner bros' -o REV,VIEW_TIME -s TITLE,PROVIDER,DAT
WARNING: DAT is not a valid column specifier. Ignoring.
Columns: TITLE,PROVIDER
the matrix,warner bros
the matrix,warner bros
the hobbit,warner bros
the hobbit,warner bros
the hobbit,warner bros

There are a couple of pipe-delimited files in the 'data' subdirectory of the archive, they may not be in sync with the media.json there, however.  Simply reloading them with -l should clear that up.


