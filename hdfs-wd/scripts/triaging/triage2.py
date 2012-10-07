#!/usr/bin/python
import sys
import os
import fInfo

#expsPath = "/tmp/fi/expResult/"
expsPath = "/Users/pallavi/Research/faultInjection/hdfs-wd/results/append/nodes6rep3-sl-allButNode-2f/"
pfxToFailedExpsMap = {}
parser = fInfo.Parser()
stDepth = 2
choice = 1
rIdToRFInfoMap = {}


for expPath in os.listdir(expsPath):
	if expPath.find('wiped') == -1 and expPath.find('time') == -1:
		isFailedExp = False
		fsns = []
		for expSubPath in os.listdir(expsPath + '/' + expPath):
			if expSubPath.find('FAIL') != -1:
				isFailedExp = True
			if expSubPath.startswith('fsn'):
				fsns.append(expSubPath)
		if isFailedExp:
			fsns = sorted(fsns)
			l = len(fsns)
			
			rFsnsStr = ""
			for fsn in fsns[0:l-1]:
				p = os.path.join(expsPath + '/' + expPath)
				fsnP = os.path.join(p, fsn)
				(rStr, rId) = (parser.getRId(fsnP, stDepth, choice)) 
				rIdToRFInfoMap[rId] = rStr
				rFsnsStr = rFsnsStr + str(rId)
				rFsnsStr = rFsnsStr + " "
			pfxStr = rFsnsStr[0:-1]

			#pfxStr = " ".join(fsns[0:l-1])
			if pfxStr in pfxToFailedExpsMap.keys():
				exps = pfxToFailedExpsMap[pfxStr]
				exps.add(expPath)
			else:
				pfxToFailedExpsMap[pfxStr] = set([expPath])

for pfxStr in pfxToFailedExpsMap.keys():
	rIds = pfxStr.split(" ")
	for rId in rIds:
		print rIdToRFInfoMap[int(rId)]
	exps = pfxToFailedExpsMap[pfxStr]
	for exp in exps:
		print exp
	print "------------------------------------------------------------------"	

