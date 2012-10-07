#!/usr/bin/python
import sys
import os

expsPath = "/Users/pallavi/Research/faultInjection/workload-trunk/failPtsAppend/failPtsWPriotWEqClCrashFail2Rep3"

fsnsExps = []

for expPath in os.listdir(expsPath):
	if expPath.find('wiped') == -1 and \
	   expPath.find('time') == -1 and \
	   expPath.find('distinctDepFails') == -1 and \
	   expPath.find('depExps') == -1:
		isFailedExp = False
		fsn1 = -1
		fsn2 = -1
		fsn3 = -1
		for expSubPath in os.listdir(expsPath + '/' + expPath):
			if(expSubPath == 'EXPERIMENT-FAIL'):
				isFailedExp = True
			if(expSubPath.startswith('fsn1-')):
				fsn1 = int(expSubPath[6:-4])
			if(expSubPath.startswith('fsn2-')):	
				fsn2 = int(expSubPath[6:-4])
			if(expSubPath.startswith('fsn3-')):	
				fsn3 = int(expSubPath[6:-4])
		if isFailedExp:
			expPath = expPath[4:].lstrip('0')
			fsnsExps.append(str(fsn1) + '  ' + str(fsn2) + '\n')
			#fsnsExps.append(expPath + '  ' + str(fsn1) + '  ' + str(fsn2) + '\n')
			#fsnsExps.append(expPath + '\n')

f = open('fsnsExps', 'w')
f.writelines(fsnsExps)
f.close()

