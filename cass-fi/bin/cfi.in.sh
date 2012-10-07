#!/bin/sh



CASSANDRA_INCLUDE=/Users/pallavi/Research/faultInjection/cass-fi/bin/cfi.in.sh
export CASSANDRA_INCLUDE
cd /Users/pallavi/Research/faultInjection/cass-fi


. $CASSANDRA_INCLUDE

echo ""
echo "$JVM_OPTS"
echo ""


JAVA=`which java`
exec $JAVA $JVM_OPTS -cp $CLASSPATH org.fi.FMDriver > /tmp/fi/logs/fi.out  & 

