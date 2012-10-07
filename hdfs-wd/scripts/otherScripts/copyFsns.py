#!/usr/bin/python

import sys
import os

if len(sys.argv) < 3:
	print('Correct usage: python copyFsns.py <input-dir> <output-dir>')
	sys.exit(0)

dirName = sys.argv[1]
outDirName = sys.argv[2]

for subDirName in os.listdir(dirName):
	subDirPath = os.path.join(dirName, subDirName)
	for fName in os.listdir(subDirPath):
		fPath = os.path.join(subDirPath, fName)
		if fName.startswith("fsn"):
			outputFPath = os.path.join(outDirName, fName[5:]) 
			os.system('cp ' + fPath + ' ' + outputFPath)
