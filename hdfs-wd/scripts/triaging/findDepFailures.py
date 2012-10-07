#!/usr/bin/python
import sys
import os

expsPath = "/Users/pallavi/Research/faultInjection/workload-trunk/failPtsWrite/failPtsBruteCrashFail2Rep3Nodes5"

if len(sys.argv) < 2:
	print('Correct usage: python findDepFailures.py <file which has the distinct last FIDs>')
	sys.exit(0)
	
f = open(sys.argv[1], 'r')
lastFIds = f.readlines()
f.close()

independentExps = []

for expPath in os.listdir(expsPath):
	if expPath.find('wiped') == -1 and \
	   expPath.find('time') == -1 and \
	   expPath.find('distinctDepFails') == -1 and \
	   expPath.find('distinctIndepFails') == -1 and \
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
			if(((str(fsn2)+'\n') not in lastFIds)):
			#if(((str(fsn2)+'\n') in lastFIds)):
				expPath = expPath[4:].lstrip('0')
				#independentExps.append(str(fsn1) + '  ' + str(fsn2) + '\n')
				independentExps.append(expPath + '  ' + str(fsn1) + '  ' + str(fsn2) + '\n')
				#independentExps.append(expPath + '\n')
				#os.system('cp -r ' + expsPath + '/' + expPath + ' ' + expsPath + '/indep/' + expPath)

f = open('indepExps', 'w')
f.writelines(independentExps)
f.close()

