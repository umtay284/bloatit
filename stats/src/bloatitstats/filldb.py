#!python
import getopt, sys
from bloatitstats.parser.logparser import  logparser
from bloatitstats.commun.database import database
from bloatitstats.parser.entry_processor import entry_processor

version = '0.1'

def parse_file(dbname, logfile):
    parser = logparser(logfile)
    base = database(dbname)
    
    base.create_table()
    
    logentries = dict()
    while parser.readline():
        if parser.is_request:
            if parser.thread in logentries:
                logentries[parser.thread].add_request(parser.request)
                logentries[parser.thread].process(base.cursor)
                del logentries[parser.thread]
            else:
                logentries[parser.thread] = entry_processor(parser.date, parser.thread, parser.level)
                logentries[parser.thread].add_request(parser.request)
        else:
            if parser.thread in logentries:
                logentries[parser.thread].add_context(parser.context)
                logentries[parser.thread].process(base.cursor)
                del logentries[parser.thread]
            else:
                logentries[parser.thread] = entry_processor(parser.date, parser.thread, parser.level)
                logentries[parser.thread].add_context(parser.context)
    base.close_connection()

def usage():
    print '''
    Parse a log file and add the entries into a database.
    
-h --help              Show this help.
-v --version           Print the version number and exit.
-d --database FILE     Use 'FILE' as a database (default is ./stats.db)
-l --logfile FILE      Read log from 'FILE' (default is standard input)

'''

def main():
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hvd:l:", ["help", "version", 'database', 'logfile'])
    except getopt.GetoptError, err:
        # print help information and exit:
        print str(err) # will print something like "option -a not recognized"
        usage()
        sys.exit(2)
        
    database = './stats.db'
    logfile = None
    for o, a in opts:
        if o in ('-v', '--version'):
            print 'version: ' + version
            sys.exit()
        elif o in ("-h", "--help"):
            usage()
            sys.exit()
        elif o in ("-d", "--database"):
            database = a
        elif o in ("-l", "--logfile"):
            logfile = a
        else:
            print 'ERROR: unknown option'
            usage()
            sys.exit()
            
    parse_file(database, logfile)
            

if __name__ == "__main__":
    main()