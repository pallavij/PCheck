#!/usr/bin/env bash


usage="Usage: start-fi.sh "

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/hadoop-config.sh


# ------------------------------------------------
# haryadi: setup conf dirs, and num dn
# ------------------------------------------------
if [ -e build/classes/org/fi/FMDriver.class ]; then
    echo "";     echo "";     echo ""; 
    echo "# -------------------------------------------------"
    echo "#    FM Driver is starting  [opt=$fiStartOpt]    "
    echo "# -------------------------------------------------"
    echo "";     echo "";     echo ""; 
    "$bin"/hadoop-daemon.sh --config $HADOOP_CONF_DIR start fi 
else
    echo ""; echo ""; echo "";
    echo "# -------------------------------------------------"
    echo "#           FM Driver does NOT exist, no FI        "
    echo "# -------------------------------------------------"
    echo ""; echo ""; echo "";
fi



