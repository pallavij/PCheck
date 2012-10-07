#!/usr/bin/python
import os
import sys

cassRootDir = "/Users/pallavi/Research/faultInjection/cass-fi/"
nZkServers = 4

expNumPath = cassRootDir + "covFiles/expNum"
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

os.system("mkdir " + cassRootDir + "covFiles/exp-" + str(expNum))


os.system("java -cp " + cassRootDir + "lib/emma.jar emma report -r html -sp " + cassRootDir + "src/java/ -in " + cassRootDir + "/emma/metadata.emma," + cassRootDir + "/emma/coverage1.emma," + cassRootDir + "/emma/coverage2.emma," + cassRootDir + "/emma/coverage3.emma," + cassRootDir + "/emma/coverage4.emma -Dreport.html.out.file=" + cassRootDir + "covFiles/exp-" + str(expNum) + "/coverage.html")

os.system("rm -rf " + cassRootDir + "emma/coverage*")
