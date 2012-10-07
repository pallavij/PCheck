#!/usr/bin/python
import sys
import os
import fInfo

#expsPath = "/tmp/fi/expResult/"
expsPath = "/Users/pallavi/Research/faultInjection/hdfs-wd/results/append/nodes8rep4-sl-3f"
rFsnsToFailedExpsMap = {}
parser = fInfo.Parser()

stDepth = 2
choice = 1

rIdToRFInfoMap = {}

for expPath in os.listdir(expsPath):
	if expPath.find('wiped') == -1 and expPath.find('time') == -1 and expPath.find('lFailPts') == -1 and expPath.find('gFailPts'):
		isFailedExp = False
		fsns = []
		for expSubPath in os.listdir(expsPath + '/' + expPath):
			if expSubPath.find('FAIL') != -1:
				isFailedExp = True
			if expSubPath.startswith('fsn'):
				fsns.append(expSubPath)
		if isFailedExp:
			fsns = sorted(fsns)
			rFsnsStr = ""
			for fsn in fsns:
				p = os.path.join(expsPath + '/' + expPath)
				fsnP = os.path.join(p, fsn)
				(rStr, rId) = (parser.getRId(fsnP, stDepth, choice)) 
				rIdToRFInfoMap[rId] = rStr
				rFsnsStr = rFsnsStr + str(rId)
				rFsnsStr = rFsnsStr + " "
			rFsnsStr = rFsnsStr[:-1]
			if rFsnsStr in rFsnsToFailedExpsMap.keys():
				exps = rFsnsToFailedExpsMap[rFsnsStr]
				exps.add(expPath)
			else:
				rFsnsToFailedExpsMap[rFsnsStr] = set([expPath])
for rFsnsStr in rFsnsToFailedExpsMap.keys():
	#print rFsnsStr + ":"
	rIds = rFsnsStr.split(" ")
	for rId in rIds:
		print rIdToRFInfoMap[int(rId)]
	exps = rFsnsToFailedExpsMap[rFsnsStr]
	for exp in exps:
		print exp
	print "------------------------------------------------------------------"	
