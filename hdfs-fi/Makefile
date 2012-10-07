

dfs := /tmp/fi/hadoop-${USER}/dfs/
img := current/fsimage
edit := current/edits
fstime := current/fstime


all: clean rmlogs startfi startdfs mkdir putfile1

test2:
	@echo "----------------------------------- test2"
	bin/hadoop fs -mkdir state2


test-ip: cleantmpfi format startdfs mkdir putfile1 data

cleantmpfi:
	rm -Rf /tmp/fi
	rm -Rf /tmp/hadoop*
	mkdir /tmp/fi
	mkdir /tmp/fi/logs
	mkdir /tmp/fi/pids
	mkdir /tmp/fi/socketHistory

clean: cleantmpfi format rmlogs rmpids

format:
	bin/hadoop namenode -format

rmlogs:
	rm -f logs/*

rmpids:
	rm -f pids/*

kill:
	killall java
	psjava

stop:
	bin/stop-dfs.sh

startfi:
	bin/start-fi.sh

startdfs:
	bin/start-dfs.sh

mkdir:
	@echo "----------------------------------- mkdir"
	bin/hadoop fs -mkdir files

putfile1:
	@echo "----------------------------------- put files 4K"
	bin/hadoop fs -put file4KB files/fileA

ls:
	@echo "----------------------------------- ls files"
	bin/hadoop fs -ls files


data:
	find /tmp/fi/hadoop-${USER}/dfs/data1 | cat -n
	find /tmp/fi/hadoop-${USER}/dfs/data2 | cat -n
	find /tmp/fi/hadoop-${USER}/dfs/data3 | cat -n
	find /tmp/fi/hadoop-${USER}/dfs/data4 | cat -n
	find /tmp/fi/hadoop-${USER}/dfs/data5 | cat -n
	find /tmp/fi/hadoop-${USER}/dfs/data6 | cat -n
