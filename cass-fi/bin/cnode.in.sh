#!/bin/sh


if [ "x$NODE_ID" = "x" ]; then

    echo ""
    echo "  FATAL ERROR: NODE_ID has not been specified !!! "
    echo ""    
    exit

fi

# ------------------------------------------------------
# change this appropriately !!!!!!!!!!!!!!!!
# ------------------------------------------------------
nodeId=$NODE_ID
cassandra_home="/Users/pallavi/Research/faultInjection/cass-fi"
FM_MY_JARS="/Users/pallavi/Research/faultInjection/cass-fi/lib/cass"


# adding:
# ------------------------
# 1) cassandra classes
# ------------------------
cassandra_classes="$cassandra_home/build/classes"
CLASSPATH="${CLASSPATH}:$cassandra_classes"
# ------------------------
# 2) conf folder
# ------------------------
CASSANDRA_CONF="$cassandra_home/conf/conf$nodeId"
CLASSPATH="${CLASSPATH}:$CASSANDRA_CONF"
# ------------------------
# 3) lib jars
# ------------------------
for jar in $cassandra_home/lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done
# ------------------------
# 4) aspect stuff
# ------------------------
FM_ASPECTJ1="$FM_MY_JARS/aspectj/aspectjrt-1.6.5.jar"
FM_ASPECTJ2="$FM_MY_JARS/aspectj/aspectjtools-1.6.5.jar"
CLASSPATH="${CLASSPATH}:${FM_ASPECTJ1}:${FM_ASPECTJ2}"
# ------------------------
# 5) JOL stuff
# ------------------------
FM_JOL="$FM_MY_JARS/jol/jol.jar"
CLASSPATH="${CLASSPATH}:${FM_JOL}"
# ------------------------
# 6) RPC stuffs
# ------------------------
FM_RPC1="$FM_MY_JARS/apache-xmlrpc-3.1.3/lib/xmlrpc-client-3.1.3.jar"
FM_RPC2="$FM_MY_JARS/apache-xmlrpc-3.1.3/lib/xmlrpc-server-3.1.3.jar"
FM_RPC3="$FM_MY_JARS/apache-xmlrpc-3.1.3/lib/xmlrpc-common-3.1.3.jar"
FM_RPC4="$FM_MY_JARS/apache-xmlrpc-3.1.3/lib/ws-commons-util-1.0.2.jar"
FM_RPC5="$FM_MY_JARS/apache-xmlrpc-3.1.3/lib/commons-logging-1.1.jar"
CLASSPATH="${CLASSPATH}:${FM_RPC1}:${FM_RPC2}:${FM_RPC3}:${FM_RPC4}:${FM_RPC5}"
# ------------------------
# 7) boot opts
# ------------------------
FM_WOVENRT="$cassandra_home/build/woven-rt.jar"
FM_BOOT_OPTS="-Xbootclasspath:$FM_WOVENRT::lib/emma.jar:lib/emma_ant.jar"
((ctlPort=5000+nodeId))


# ------------------------------------------------------
# setting up node address and jmx address automatically
# ------------------------------------------------------
#node0 starts at 127.0.0.1
#node1 starts at 127.0.0.2

((nodeIpLast=1+$nodeId))
nodeIp="127.0.0.$nodeIpLast"
((nodePort=9000+$nodeId))
nodeAddr="$nodeIp:$nodePort"
((jmxPort=10000+$nodeId))


# ----------------------------
echo  ""
echo  "nodeId = $nodeId"
echo  "nodeIp = $nodeIp"
echo  "nodeIp = $nodePort"
echo  ""

#jinsu testing...
#TESTING= "-XX:+UseCompressedOops \\" # enables compressed references, reducing memory overhead on 64bit JVMs

# --------------------------------------------------
# Now set JVM_OPTS
# --------------------------------------------------
JVM_OPTS=" \
        -ea \
        -Xms128M \
        -Xmx1G \
        -XX:TargetSurvivorRatio=90 \
        -XX:+AggressiveOpts \
        -XX:+UseParNewGC \
        -XX:+UseConcMarkSweepGC \
        -XX:+CMSParallelRemarkEnabled \
        -XX:+HeapDumpOnOutOfMemoryError \
        -XX:SurvivorRatio=128 \
        -XX:MaxTenuringThreshold=0 \
        -Xdebug \
        -Xrunjdwp:transport=dt_socket,server=y,address=$nodeAddr,suspend=n \
        -Dcom.sun.management.jmxremote.port=$jmxPort \
        -Dcom.sun.management.jmxremote.ssl=false \
        -Dcom.sun.management.jmxremote.authenticate=false \
	$FM_BOOT_OPTS"

