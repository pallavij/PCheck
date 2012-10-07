#!/usr/bin/python
import os
import sys
import math
from sets import Set

def getSetOfLNosInFile(fPath):
	s = Set([])
	if os.path.exists(fPath):
		f = open(fPath, "r")
		lines = f.readlines()
		for l in lines:
			s.add(l)
		f.close()

	return s	


def writeSetToFile(s, fPath):
	f = open(fPath, "w")
	f.writelines(list(s))
	f.close()



if len(sys.argv) != 3:
	sys.exit("Must provide the dir name that has the histogram coverage data and the file that has the lines that are impossible to cover")

histDataPDir = sys.argv[1]
histDataPath = os.path.join(histDataPDir, "histData")

lNosImposToCover = sys.argv[2]
sOfLNosImpos = getSetOfLNosInFile(lNosImposToCover)

if(os.path.exists(histDataPath)):
	for hDir in os.listdir(histDataPath):
		hPath = os.path.join(histDataPath, hDir)
		if os.path.isdir(hPath):
			ulinesPath = os.path.join(hPath, "uncoveredLines.txt")
			if os.path.exists(ulinesPath):
				s1 = getSetOfLNosInFile(ulinesPath)
				s2 = s1.difference(sOfLNosImpos)				
				writeSetToFile(s2, os.path.join(hPath, "ul.txt"))

