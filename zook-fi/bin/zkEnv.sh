#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script should be sourced into other zookeeper
# scripts to setup the env variables

# We use ZOOCFGDIR if defined,
# otherwise we use /etc/zookeeper
# or the conf directory that is
# a sibling of this script's directory
if [ "x$ZOOCFGDIR" = "x" ]
then
    if [ -d "/etc/zookeeper" ]
    then
        ZOOCFGDIR="/etc/zookeeper"
    else
        ZOOCFGDIR="$ZOOBINDIR/../conf"
    fi
fi

if [ "x$ZOOCFG" = "x" ]
then
    ZOOCFG="zoo.cfg"
fi

ZOOCFG="$ZOOCFGDIR/$ZOOCFG"

if [ -e "$ZOOCFGDIR/java.env" ]
then
    . "$ZOOCFGDIR/java.env"
fi

if [ "x$ZOO_LOG_DIR" = "x" ]
then
    ZOO_LOG_DIR="."
fi

if [ "x$ZOO_LOG4J_PROP" = "x" ]
then
    ZOO_LOG4J_PROP="INFO,CONSOLE"
fi


# ------------------------------------------------------------
# haryadi: I don't want to load zookeeper-dev.jar 
# because I will not be able to see the weaved classes
# instead let's do add the path to build/classes
# ------------------------------------------------------------
#for f in ${ZOOBINDIR}/../zookeeper-*.jar
#do
#    echo "    Adding to CP: $f"
#    CLASSPATH="$CLASSPATH:$f"
#done
# ------------------------------------------------------------
CLASSPATH="$CLASSPATH:build/classes"
# ------------------------------------------------------------


ZOOLIBDIR=${ZOOLIBDIR:-$ZOOBINDIR/../lib}
for i in "$ZOOLIBDIR"/*.jar
do
    #echo "    Adding to CP: $i"
    CLASSPATH="$CLASSPATH:$i"
done

#PALLAVI: Added to use cobertura
for i in "$ZOOLIBDIR"/cobertura/*.jar
do
    #echo "    Adding to CP: $i"
    CLASSPATH="$CLASSPATH:$i"
done

#make it work for developers
for d in ${ZOOBINDIR}/../src/java/lib/*.jar
do
    #echo "    Adding to CP: $d"
    CLASSPATH="$CLASSPATH:$d"
done
#add the zoocfg dir to classpath
CLASSPATH=$ZOOCFGDIR:$CLASSPATH




# ------------------------------------------------------------
# haryadi: Then we want to load all jar stuffs for FI, change my jars
# ------------------------------------------------------------


# haryadi, change this:
FM_MY_JARS="/home/st/pallavi/Research/PCheck/zook-fi/lib/fi"

# ------------------------                                                               
# 4) aspect stuff                                                                        
# ------------------------                                                               
FM_ASPECTJ1="$FM_MY_JARS/aspectj/aspectjrt-1.6.5.jar"
FM_ASPECTJ2="$FM_MY_JARS/aspectj/aspectjtools-1.6.5.jar"
CLASSPATH=${CLASSPATH}:${FM_ASPECTJ1}:${FM_ASPECTJ2}

# ------------------------                                                               
# 5) JOL stuff                                                                           
# ------------------------                                                               
FM_JOL="$FM_MY_JARS/jol/jol.jar"
CLASSPATH=${CLASSPATH}:${FM_JOL}

# ------------------------                                                               
# 6) RPC stuffs                                                                          
# ------------------------                                                               
FM_RPC1="$FM_MY_JARS/xmlrpc/xmlrpc-client-3.1.3.jar"
FM_RPC2="$FM_MY_JARS/xmlrpc/xmlrpc-server-3.1.3.jar"
FM_RPC3="$FM_MY_JARS/xmlrpc/xmlrpc-common-3.1.3.jar"
FM_RPC4="$FM_MY_JARS/xmlrpc/ws-commons-util-1.0.2.jar"
FM_RPC5="$FM_MY_JARS/xmlrpc/commons-logging-1.1.jar"
CLASSPATH=${CLASSPATH}:${FM_RPC1}:${FM_RPC2}:${FM_RPC3}:${FM_RPC4}:${FM_RPC5}

