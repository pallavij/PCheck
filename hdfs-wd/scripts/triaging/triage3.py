#!/usr/bin/python
import sys
import os
import fInfo

#expsPath = "/tmp/fi/expResult/"
expsPath = "/Users/pallavi/Research/faultInjection/hdfs-wd/results/append/nodes6rep3-sl-allButNode-2f/"
lastToFailedExpsMap = {}
parser = fInfo.Parser()
stDepth = 2
choice = 1

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
			fsn = fsns[l-1]
			p = os.path.join(expsPath + '/' + expPath)
			fsnP = os.path.join(p, fsn)
			rId = (parser.getRId(fsnP, stDepth, choice))[1] 	
			rFsnsStr = rFsnsStr + rId
			rFsnsStr = rFsnsStr
			lastStr = rFsnsStr

			#lastStr = " ".join(fsns[0:l-1])
			if lastStr in lastToFailedExpsMap.keys():
				exps = lastToFailedExpsMap[lastStr]
				exps.add(expPath)
			else:
				lastToFailedExpsMap[lastStr] = set([expPath])

for lastStr in lastToFailedExpsMap.keys():
	print lastStr + ":"
	exps = lastToFailedExpsMap[lastStr]
	for exp in exps:
		print exp
	print "------------------------------------------------------------------"	

