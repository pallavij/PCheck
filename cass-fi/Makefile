


all: test1

test1: format run



##########################################################
# this is all mechanisms
##########################################################


run:
	bin/cfi
	bin/cassandra -p 0
	bin/cnode 1
	bin/cnode 2
	bin/cnode 3


#	bin/cnode 1
#	bin/cnode 2


nodetool:
	bin/nodetool -h localhost ring

run2:
	bin/cfi
	bin/cnode0

#	bin/cnode1
#	bin/cnode2


data:
	find /tmp/fi/bfs-data/node1 | cat -n
	find /tmp/fi/bfs-data/node2 | cat -n
	find /tmp/fi/bfs-data/node3 | cat -n
	find /tmp/fi/bfs-data/node4 | cat -n

kill:
	killall -9 java
	psjava

format:
	rm -rf /tmp/fi/*
	mkdir /tmp/fi/logs
	mkdir /tmp/fi/rpcFiles
	mkdir /tmp/fi/pids

