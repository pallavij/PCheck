

all: run print

run: antrun niceout

antrun:
	ant run | tee /tmp/out.txt

print:
	while true; do sleep 1 ; cat /tmp/out.txt ; done

kill:
	killall java

niceout:
	sed -e "s/     \[java\] //" /tmp/out.txt > /tmp/workloadOut.txt

result:
	emacs /ramdisk/expResult/exp-001/*


data:
	find /tmp/fi/hadoop-${USER}/dfs/data1 | cat -n
	find /tmp/fi/hadoop-${USER}/dfs/data2 | cat -n
	find /tmp/fi/hadoop-${USER}/dfs/data3 | cat -n
	find /tmp/fi/hadoop-${USER}/dfs/data4 | cat -n
	find /tmp/fi/hadoop-${USER}/dfs/data5 | cat -n
	find /tmp/fi/hadoop-${USER}/dfs/data6 | cat -n


block:
	cd /tmp/fi/hadoop-haryadi/dfs ; \
	find . -name "*blk*" -exec ls -la '{}' \; 

meta:
	find /tmp/fi/hadoop-haryadi -name "*blk*meta"

wipetmpfi:
	rm -R /tmp/fi/*
