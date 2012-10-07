#!/usr/bin/python
import os
import sys

import util
import fInfo
import runExp

stackDepth = 3
inclPostExpFIds = False

p = fInfo.Parser()
p.getFIdDescStrs("/tmp/fi/glob/failPts/fIdStrs", stackDepth)
fInfos = p.getFInfos()
fIdTofInfoMap = util.getFIdToInfoMap(fInfos) 

fName = sys.argv[1]
print "fName is " + fName
f = open(fName, "r")
fIdsLn = f.readline()
f.close()


fIdsLn = fIdsLn.strip("\n")
fIds = fIdsLn.split(" ")
fIds = set(fIds)


outputFName = sys.argv[2]
print "Output file is " + outputFName
f = open(outputFName, "w")

for fId in fIds:
	f.write("FID: " + fId + "\n")
	util.printFIdInfo(f, fId, fInfos)
	
f.close()



