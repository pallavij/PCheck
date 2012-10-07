#!/usr/bin/python
import os
import string

coveragePath = "/tmp/fi/coverageInfo"
coveragePathRHashes = "/tmp/fi/coverageInfoRHashes"

f = open(coveragePath, 'r')
coverageLines = f.readlines()
f.close()

fR = open(coveragePathRHashes, 'r')
coverageRHashesLines = fR.readlines()
fR.close()

fPtToSubqFPtsMap = {}

count = 0
for cLine in coverageLines:
	cLineFsns = cLine.split()
	cRLineFsns = coverageRHashesLines[count].split() 
	first = cLineFsns[0]
	second = cLineFsns[1]
	secondR = cRLineFsns[1]
	if (first in fPtToSubqFPtsMap.keys()):
		 subqFPts = fPtToSubqFPtsMap[first]
		 subqFPts.add(secondR+"\t"+second)
	else:	
		 fPtToSubqFPtsMap[first] = set([secondR+"\t"+second])
	count = count + 1		 

subqPtsF = open("failPts/subqFsns", "w")
for first in fPtToSubqFPtsMap.keys():
	subqPtsF.write(first + "\t:\n")
	subqFsnsForFirst = fPtToSubqFPtsMap[first]
	for fsn in sorted(subqFsnsForFirst):
		subqPtsF.write("\t"+fsn+"\n")
subqPtsF.close()	
