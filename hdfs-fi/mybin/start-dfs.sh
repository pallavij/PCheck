#!/usr/bin/env bash

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


# Start hadoop dfs daemons.
# Optinally upgrade or rollback dfs state.
# Run this on master node.

usage="Usage: start-dfs.sh [-upgrade|-rollback]"

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin"/hadoop-config.sh

# get arguments
if [ $# -ge 1 ]; then
	nameStartOpt=$1
	shift
	case $nameStartOpt in
	  (-upgrade)
	  	;;
	  (-rollback) 
	  	dataStartOpt=$nameStartOpt
	  	;;
	  (*)
		  echo $usage
		  exit 1
	    ;;
	esac
fi

# start dfs daemons
# start namenode after datanodes, to minimize time namenode is up w/o data
# note: datanodes will log connection errors until namenode starts


# ------------------------------------------------
# haryadi: setup conf dirs, and num dn
# ------------------------------------------------
MY_CONF_DIR=conf
NN_CONF_DIR=$MY_CONF_DIR
NUM_DN_STR=`grep localhost $MY_CONF_DIR/slaves | wc -l | awk '{print $1}'`
NUM_DN=`expr $NUM_DN_STR + 0`

# ------------------------------------------------
# haryadi: run namenode with nn conf
# ------------------------------------------------

echo ""
echo "- Starting Namenode [$NN_CONF_DIR] ..."
echo ""
"$bin"/hadoop-daemon.sh --config $NN_CONF_DIR start namenode $nameStartOpt


# ------------------------------------------------
# haryadi: run multiple datanodes
# ------------------------------------------------

echo ""
echo "- Running HDFS with $NUM_DN nodes ..."
echo ""

# pallavi-07
if [ $NUM_DN -lt 1 ]; then
    echo "- exiting because of 0 datanodes ..."
    exit
fi




for i in `seq 1 $NUM_DN`
do
    DN_CONF_DIR="$MY_CONF_DIR/dnconf$i"

    echo ""
    echo "- Starting DN-$i [$DN_CONF_DIR] ..."
    echo ""    

    "$bin"/hadoop-daemon.sh --config $DN_CONF_DIR start datanode $dataStartOpt  &

done


# haryadi: disabled secondary namenode, and old dn startup approach
# "$bin"/hadoop-daemons.sh --config $NN_CONF_DIR start datanode $dataStartOpt
# "$bin"/hadoop-daemons.sh --config $NN_CONF_DIR --hosts masters start secondarynamenode
