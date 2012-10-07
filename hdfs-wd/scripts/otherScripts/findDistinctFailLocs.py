#!/usr/bin/python
import sys
import os
from sets import Set

#expsPath = "/Users/pallavi/Research/faultInjection/workload-trunk/failPtsAppend/failPtsWOPriotCrashFail2Rep3"
expsPath = "/tmp/fi/expResult" 

def getFailLoc(fName):
	f = open(fName, 'r')
	fLines = f.readlines()
	f.close()
	for fLine in fLines:
		if(fLine.find('SourceLoc:') != -1):
			fLine = fLine.strip()
			fLine = fLine[11:]
			fLineParts = fLine.split('/')
			return fLineParts[-1]
	return ''	

indepExpIds = []

if len(sys.argv) > 1:	
	f = open(sys.argv[1], 'r')
	indepExpIds = f.readlines()
	f.close()

distinctFailPts = Set([])

for expPath in os.listdir(expsPath):
	if expPath.find('wiped') == -1 and expPath.find('time') == -1 and expPath.find('distinctDepFails') == -1:
		fsn1 = ''
		fsn2 = ''
		if (indepExpIds == []) or ((expPath+'\n') in indepExpIds):
			for expSubPath in os.listdir(expsPath + '/' + expPath):
				if(expSubPath.startswith('fsn1-')):
					fsn1 = getFailLoc(expsPath + '/' + expPath + '/' + expSubPath)
				if(expSubPath.startswith('fsn2-')):
					fsn2 = getFailLoc(expsPath + '/' + expPath + '/' + expSubPath)
			distinctFailPts.add(fsn1 + '\t' + fsn2 + '\n')		

f = open('distinctFailPts', 'w')
f.writelines(list(distinctFailPts))
f.close()
