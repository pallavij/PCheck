#!/usr/bin/python
import os
import sys

zkRootDir = "/Users/pallavi/Research/faultInjection/zook-fi/"
nZkServers = 3

expNumPath = zkRootDir + "covFiles/expNum"
if os.path.exists(expNumPath):
	f = open(expNumPath, "r")
	expNum = int(f.read().strip())
	f.close()
else:
	expNum = 1
f = open(expNumPath, "w")
f.write(str(expNum+1) + "\n")
f.close()

#print "expNum = " + expNum

os.system("mkdir " + zkRootDir + "covFiles/exp-" + str(expNum))

#for i in range(1, nZkServers+1):
#	ctlPort = 5000 + i
#	os.system("java -cp " + zkRootDir +"lib/emma.jar emma ctl -connect localhost:" + str(ctlPort) + " -command coverage.dump,/Users/pallavi/Research/faultInjection/zook-fi/emma/coverage" + str(i) + ".emma,true,true -command coverage.reset")

os.system("java -cp " + zkRootDir + "lib/emma.jar emma report -r html -sp " + zkRootDir + "src/java/main -in " + zkRootDir + "/emma/metadata.emma," + zkRootDir + "/emma/coverage1.emma," + zkRootDir + "/emma/coverage2.emma," + zkRootDir + "/emma/coverage3.emma -Dreport.html.out.file=" + zkRootDir + "covFiles/exp-" + str(expNum) + "/coverage.html")

os.system("rm -rf " + zkRootDir + "emma/coverage*")
